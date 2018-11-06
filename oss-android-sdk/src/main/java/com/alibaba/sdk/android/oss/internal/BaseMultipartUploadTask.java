package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.TaskCancelException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jingdan on 2017/10/30.
 * multipart base task
 */

public abstract class BaseMultipartUploadTask<Request extends MultipartUploadRequest,
        Result extends CompleteMultipartUploadResult> implements Callable<Result> {

    protected final int CPU_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    protected final int MAX_CORE_POOL_SIZE = CPU_SIZE < 5 ? CPU_SIZE : 5;
    protected final int MAX_IMUM_POOL_SIZE = CPU_SIZE;
    protected final int KEEP_ALIVE_TIME = 3000;
    protected final int MAX_QUEUE_SIZE = 5000;
    protected ThreadPoolExecutor mPoolExecutor =
            new ThreadPoolExecutor(MAX_CORE_POOL_SIZE, MAX_IMUM_POOL_SIZE, KEEP_ALIVE_TIME,
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "oss-android-multipart-thread");
                }
            });
    protected List<PartETag> mPartETags = new ArrayList<PartETag>();
    protected Object mLock = new Object();
    protected InternalRequestOperation mApiOperation;
    protected ExecutionContext mContext;
    protected Exception mUploadException;
    protected boolean mIsCancel;
    protected File mUploadFile;
    protected String mUploadId;
    protected long mFileLength;
    protected int mPartExceptionCount;
    protected int mRunPartTaskCount;
    protected long mUploadedLength = 0;
    protected boolean mCheckCRC64 = false;
    protected Request mRequest;
    protected OSSCompletedCallback<Request, Result> mCompletedCallback;
    protected OSSProgressCallback<Request> mProgressCallback;
    protected int[] mPartAttr = new int[2];
    protected String mUploadFilePath;

    public BaseMultipartUploadTask(InternalRequestOperation operation, Request request,
                                   OSSCompletedCallback<Request, Result> completedCallback,
                                   ExecutionContext context) {
        mApiOperation = operation;
        mRequest = request;
        mProgressCallback = request.getProgressCallback();
        mCompletedCallback = completedCallback;
        mContext = context;
        mCheckCRC64 = (request.getCRC64() == OSSRequest.CRC64Config.YES);
    }

    /**
     * abort upload
     */
    protected abstract void abortThisUpload();

    /**
     * init multipart upload id
     *
     * @throws IOException
     * @throws ClientException
     * @throws ServiceException
     */
    protected abstract void initMultipartUploadId() throws IOException, ClientException, ServiceException;

    /**
     * do multipart upload task
     *
     * @return
     * @throws IOException
     * @throws ServiceException
     * @throws ClientException
     * @throws InterruptedException
     */
    protected abstract Result doMultipartUpload() throws IOException, ServiceException, ClientException, InterruptedException;

    /**
     * check is or not cancel
     *
     * @throws ClientException
     */
    protected void checkCancel() throws ClientException {
        if (mContext.getCancellationHandler().isCancelled()) {
            TaskCancelException e = new TaskCancelException("multipart cancel");
            throw new ClientException(e.getMessage(), e, true);
        }
    }


    protected void preUploadPart(int readIndex, int byteCount, int partNumber) throws Exception {

    }

    protected void uploadPartFinish(PartETag partETag) throws Exception {
    }

    @Override
    public Result call() throws Exception {
        try {
            checkInitData();
            initMultipartUploadId();
            Result result = doMultipartUpload();

            if (mCompletedCallback != null) {
                mCompletedCallback.onSuccess(mRequest, result);
            }
            return result;
        } catch (ServiceException e) {
            if (mCompletedCallback != null) {
                mCompletedCallback.onFailure(mRequest, null, e);
            }
            throw e;
        } catch (Exception e) {
            ClientException temp;
            if (e instanceof ClientException) {
                temp = (ClientException) e;
            } else {
                temp = new ClientException(e.toString(), e);
            }
            if (mCompletedCallback != null) {
                mCompletedCallback.onFailure(mRequest, temp, null);
            }
            throw temp;
        }
    }

    protected void checkInitData() throws ClientException {
        mUploadFilePath = mRequest.getUploadFilePath();
        mUploadedLength = 0;
        mUploadFile = new File(mUploadFilePath);
        mFileLength = mUploadFile.length();
        if (mFileLength == 0) {
            throw new ClientException("file length must not be 0");
        }
        checkPartSize(mPartAttr);

        final long partSize = mRequest.getPartSize();
        final int partNumber = mPartAttr[1];

        OSSLog.logDebug("[checkInitData] - partNumber : " + partNumber);
        OSSLog.logDebug("[checkInitData] - partSize : " + partSize);


        if (partNumber > 1 && partSize < 102400) {
            throw new ClientException("Part size must be greater than or equal to 100KB!");
        }
    }

    protected void uploadPart(int readIndex, int byteCount, int partNumber) {

        RandomAccessFile raf = null;
        try {

            if (mContext.getCancellationHandler().isCancelled()) {
                mPoolExecutor.getQueue().clear();
                return;
            }

            synchronized (mLock) {
                mRunPartTaskCount++;
            }

            preUploadPart(readIndex, byteCount, partNumber);

            raf = new RandomAccessFile(mUploadFile, "r");
            UploadPartRequest uploadPart = new UploadPartRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId, readIndex + 1);
            long skip = readIndex * mRequest.getPartSize();
            byte[] partContent = new byte[byteCount];
            raf.seek(skip);
            raf.readFully(partContent, 0, byteCount);
            uploadPart.setPartContent(partContent);
            uploadPart.setMd5Digest(BinaryUtil.calculateBase64Md5(partContent));
            uploadPart.setCRC64(mRequest.getCRC64());
            UploadPartResult uploadPartResult = mApiOperation.syncUploadPart(uploadPart);
            //check isComplete
            synchronized (mLock) {
                PartETag partETag = new PartETag(uploadPart.getPartNumber(), uploadPartResult.getETag());
                partETag.setPartSize(byteCount);
                if (mCheckCRC64) {
                    partETag.setCRC64(uploadPartResult.getClientCRC());
                }

                mPartETags.add(partETag);
                mUploadedLength += byteCount;

                uploadPartFinish(partETag);

                if (mContext.getCancellationHandler().isCancelled()) {
                    if (mPartETags.size() == (mRunPartTaskCount - mPartExceptionCount)) {
                        TaskCancelException e = new TaskCancelException("multipart cancel");

                        throw new ClientException(e.getMessage(), e, true);
                    }
                } else {
                    if (mPartETags.size() == (partNumber - mPartExceptionCount)) {
                        notifyMultipartThread();
                    }
                    onProgressCallback(mRequest, mUploadedLength, mFileLength);
                }

            }

        } catch (Exception e) {
            processException(e);
        } finally {
            try {
                if (raf != null)
                    raf.close();
            } catch (IOException e) {
                OSSLog.logThrowable2Local(e);
            }
        }
    }

    abstract protected void processException(Exception e);

    /**
     * complete multipart upload
     *
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    protected CompleteMultipartUploadResult completeMultipartUploadResult() throws ClientException, ServiceException {
        //complete sort
        CompleteMultipartUploadResult completeResult = null;
        if (mPartETags.size() > 0) {
            Collections.sort(mPartETags, new Comparator<PartETag>() {
                @Override
                public int compare(PartETag lhs, PartETag rhs) {
                    if (lhs.getPartNumber() < rhs.getPartNumber()) {
                        return -1;
                    } else if (lhs.getPartNumber() > rhs.getPartNumber()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId, mPartETags);
            complete.setMetadata(mRequest.getMetadata());
            if (mRequest.getCallbackParam() != null) {
                complete.setCallbackParam(mRequest.getCallbackParam());
            }
            if (mRequest.getCallbackVars() != null) {
                complete.setCallbackVars(mRequest.getCallbackVars());
            }
            complete.setCRC64(mRequest.getCRC64());
            completeResult = mApiOperation.syncCompleteMultipartUpload(complete);
        }
        mUploadedLength = 0;
        return completeResult;
    }

    protected void releasePool() {
        if (mPoolExecutor != null) {
            mPoolExecutor.getQueue().clear();
            mPoolExecutor.shutdown();
        }
    }

    protected void checkException() throws IOException, ServiceException, ClientException {
        if (mUploadException != null) {
            releasePool();
            if (mUploadException instanceof IOException) {
                throw (IOException) mUploadException;
            } else if (mUploadException instanceof ServiceException) {
                throw (ServiceException) mUploadException;
            } else if (mUploadException instanceof ClientException) {
                throw (ClientException) mUploadException;
            } else {
                ClientException clientException =
                        new ClientException(mUploadException.getMessage(), mUploadException);
                throw clientException;
            }
        }
    }

    protected boolean checkWaitCondition(int partNum) {
        if (mPartETags.size() == partNum) {
            return false;
        }
        return true;
    }

    /**
     * notify wait thread
     */
    protected void notifyMultipartThread() {
        mLock.notify();
        mPartExceptionCount = 0;
    }

    /**
     * check part size
     *
     * @param partAttr
     */
    protected void checkPartSize(int[] partAttr) {
        long partSize = mRequest.getPartSize();
        OSSLog.logDebug("[checkPartSize] - mFileLength : " + mFileLength);
        OSSLog.logDebug("[checkPartSize] - partSize : " + partSize);
        int partNumber = (int) (mFileLength / partSize);
        if (mFileLength % partSize != 0) {
            partNumber = partNumber + 1;
        }
        int MAX_PART_NUM = 5000;
        if (partNumber == 1) {
            partSize = mFileLength;
        } else if (partNumber > MAX_PART_NUM) {
            partSize = mFileLength / MAX_PART_NUM;
            partNumber = MAX_PART_NUM;
        }
        partAttr[0] = (int) partSize;
        partAttr[1] = partNumber;
        mRequest.setPartSize((int) partSize);

        OSSLog.logDebug("[checkPartSize] - partNumber : " + partNumber);
        OSSLog.logDebug("[checkPartSize] - partSize : " + (int) partSize);
    }

    /**
     * progress callback
     *
     * @param request
     * @param currentSize
     * @param totalSize
     */
    protected void onProgressCallback(Request request, long currentSize, long totalSize) {
        if (mProgressCallback != null) {
            mProgressCallback.onProgress(request, currentSize, totalSize);
        }
    }

}
