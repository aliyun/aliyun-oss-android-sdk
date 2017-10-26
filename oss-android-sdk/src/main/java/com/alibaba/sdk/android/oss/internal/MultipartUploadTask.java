package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jingdan on 2017/10/19.
 * multipart upload support concurrent thread work
 */
public class MultipartUploadTask implements Callable<CompleteMultipartUploadResult> {

    private final int CPU_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private final int MAX_CORE_POOL_SIZE = CPU_SIZE < 5 ? CPU_SIZE : 5;
    private final int MAX_IMUM_POOL_SIZE = CPU_SIZE;
    private final int KEEP_ALIVE_TIME = 3000;
    private final int MAX_QUEUE_SIZE = 1000;
    private ThreadPoolExecutor mPoolExecutor =
            new ThreadPoolExecutor(MAX_CORE_POOL_SIZE,MAX_IMUM_POOL_SIZE,KEEP_ALIVE_TIME,
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(MAX_QUEUE_SIZE));
    private List<PartETag> mPartETags = new ArrayList<PartETag>();
    private Object mLock = new Object();
    private OSSCompletedCallback<MultipartUploadRequest, CompleteMultipartUploadResult> mCompletedCallback;
    private OSSProgressCallback<MultipartUploadRequest> mProgressCallback;
    private InternalRequestOperation mApiOperation;
    private MultipartUploadRequest mRequest;
    private ExecutionContext mContext;
    private Exception mUploadException;
    private File mUploadFile;
    private String mUploadId;
    private long mFileLength;

    public MultipartUploadTask(InternalRequestOperation operation, MultipartUploadRequest request,
                               OSSCompletedCallback<MultipartUploadRequest, CompleteMultipartUploadResult> completedCallback,
                               ExecutionContext context){
        mApiOperation = operation;
        mRequest = request;
        mProgressCallback = request.getProgressCallback();
        mCompletedCallback = completedCallback;
        mContext = context;
    }

    @Override
    public CompleteMultipartUploadResult call() throws Exception {
        try {
            initMultipartUploadId();
            CompleteMultipartUploadResult result = doMultipartUpload();

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
            ClientException temp = new ClientException(e.toString(), e);
            if (mCompletedCallback != null) {
                mCompletedCallback.onFailure(mRequest, temp, null);
            }
            throw temp;
        }
    }

    private void initMultipartUploadId() throws IOException, ServiceException, ClientException {
        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(
                mRequest.getBucketName(), mRequest.getObjectKey(), mRequest.getMetadata());

        InitiateMultipartUploadResult initResult = mApiOperation.initMultipartUpload(init, null).getResult();

        mUploadId = initResult.getUploadId();
    }

    private CompleteMultipartUploadResult doMultipartUpload() throws IOException, ServiceException, ClientException, InterruptedException {
        checkMultipartUploadCancel();
        mUploadFile = new File(mRequest.getUploadFilePath());
        mFileLength = mUploadFile.length();
        int[] partAttr = new int[2];
        checkPartSize(partAttr);
        int readByte = partAttr[0];
        final int partNumber = partAttr[1];
        int currentLength = 0;
        OSSLog.logDebug("multipartupload part start", false);
        for (int i = 0; i < partNumber; i++) {
            checkException();
            if(mPoolExecutor != null) {
                //need read byte
                if (i == partNumber - 1) {
                    readByte = (int) Math.min(readByte, mFileLength - currentLength);
                }
                final int byteCount = readByte;
                final int readIndex = i;
                currentLength += byteCount;

                mPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        OSSLog.logDebug("thread name:= "+Thread.currentThread().getName(), false);
                        RandomAccessFile raf = null;
                        try {
                            checkMultipartUploadCancel();
                            raf  = new RandomAccessFile(mUploadFile, "r");
                            UploadPartRequest uploadPart = new UploadPartRequest(
                                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId, readIndex + 1);

                            long skip = readIndex * byteCount;
                            byte[] partContent = new byte[byteCount];
                            raf.seek(skip);
                            raf.readFully(partContent, 0, byteCount);
                            uploadPart.setPartContent(partContent);
                            uploadPart.setMd5Digest(BinaryUtil.calculateBase64Md5(partContent));
                            UploadPartResult uploadPartResult = mApiOperation.uploadPart(uploadPart, null).getResult();
                            mPartETags.add(new PartETag(uploadPart.getPartNumber(), uploadPartResult.getETag()));
                            //check isComplete
                            if (mPartETags.size() == partNumber) {
                                if(mProgressCallback != null) {
                                    mProgressCallback.onProgress(mRequest, mFileLength, mFileLength);
                                }
                                notifyMultipartThread();
                            } else {
                                onProgressCallback(mRequest, mPartETags.size() * byteCount, mFileLength);
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
                });
            }
        }

        if(checkWaitCondition(partNumber)) {
            synchronized (mLock) {
                mLock.wait();
            }
        }
        OSSLog.logDebug("multipartupload part end", false);

        checkException();
        //complete sort
        CompleteMultipartUploadResult completeResult = null;
        if(mPartETags.size() > 0) {
            OSSLog.logDebug("sort start", false);
            Collections.sort(mPartETags, new Comparator<PartETag>() {
                @Override
                public int compare(PartETag lhs, PartETag rhs) {
                    if (lhs.getPartNumber() < rhs.getPartNumber()) {
                        return -1;
                    }
                    return 0;
                }
            });
            OSSLog.logDebug("sort end", false);


            CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId, mPartETags);
            complete.setMetadata(mRequest.getMetadata());
            if (mRequest.getCallbackParam() != null) {
                complete.setCallbackParam(mRequest.getCallbackParam());
            }
            if (mRequest.getCallbackVars() != null) {
                complete.setCallbackVars(mRequest.getCallbackVars());
            }
            completeResult = mApiOperation.completeMultipartUpload(complete, null).getResult();
        }
        return completeResult;
    }

    private void processException(Exception e){
        synchronized (mLock) {
            if (mUploadException == null) {
                mUploadException = e;
                OSSLog.logThrowable2Local(e);
                stopUpload();
                mLock.notify();
            }
        }
    }

    private void checkMultipartUploadCancel() throws ClientException {
        if (mContext.getCancellationHandler().isCancelled()) {
            abortThisMultipartUpload();
            IOException e = new IOException("multipart cancel");
            throw new ClientException(e.getMessage(), e);
        }
    }

    private boolean checkWaitCondition(int partNum){
        if(mPartETags.size() == partNum){
            return false;
        }
        return true;
    }

    private void onProgressCallback(MultipartUploadRequest request, long currentSize, long totalSize){
        if(mProgressCallback != null){
            synchronized (mLock) {
                mProgressCallback.onProgress(request, currentSize, totalSize);
            }
        }
    }

    private void notifyMultipartThread(){
        synchronized (mLock) {
            mLock.notify();
        }
    }

    private void checkException() throws IOException, ServiceException, ClientException{
        if (mUploadException != null) {
            if(mUploadException instanceof IOException) {
                throw (IOException) mUploadException;
            }else if(mUploadException instanceof ServiceException) {
                throw (ServiceException) mUploadException;
            }else if(mUploadException instanceof ClientException) {
                throw (ClientException) mUploadException;
            }else{
                ClientException clientException =
                        new ClientException(mUploadException.getMessage(),mUploadException);
                throw clientException;
            }
        }
    }

    private void checkPartSize(int[] partAttr){
        long partSize = mRequest.getPartSize();
        int partNumber = (int) (mFileLength / partSize);
        if(mFileLength % partSize != 0){
            partNumber = partNumber + 1;
        }
        if(partNumber > 1000){
            partSize = mFileLength / 1000;
        }

        partAttr[0] = (int) partSize;
        partAttr[1] = partNumber;
    }

    private void abortThisMultipartUpload() {
        if (mUploadId != null) {
            AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId);
            mApiOperation.abortMultipartUpload(abort, null).waitUntilFinished();
        }
    }

    private void stopUpload(){
        if(mPoolExecutor != null){
            mPoolExecutor.getQueue().clear();
            mPoolExecutor.shutdownNow();
            mPoolExecutor = null;
        }
        abortThisMultipartUpload();
    }

}
