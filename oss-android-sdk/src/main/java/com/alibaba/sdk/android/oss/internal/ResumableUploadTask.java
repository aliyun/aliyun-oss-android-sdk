package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PartSummary;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by jingdan on 2017/10/30.
 */

public class ResumableUploadTask extends BaseMultipartUploadTask<ResumableUploadRequest,
        ResumableUploadResult> implements Callable<ResumableUploadResult> {

    private File mRecordFile;
    private List<Integer> mAlreadyUploadIndex = new ArrayList<Integer>();
    private long mFirstPartSize;

    public ResumableUploadTask(ResumableUploadRequest request,
                               OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback,
                               ExecutionContext context, InternalRequestOperation apiOperation) {
        super(apiOperation, request, completedCallback, context);
    }

    @Override
    protected void initMultipartUploadId() throws IOException, ClientException, ServiceException {
        String uploadFilePath = mRequest.getUploadFilePath();
        mUploadedLength = 0;
        mUploadFile = new File(uploadFilePath);
        mFileLength = mUploadFile.length();
        if (mFileLength == 0) {
            throw new ClientException("file length must not be 0");
        }

        if (!OSSUtils.isEmptyString(mRequest.getRecordDirectory())) {
            String fileMd5 = BinaryUtil.calculateMd5Str(uploadFilePath);
            String recordFileName = BinaryUtil.calculateMd5Str((fileMd5 + mRequest.getBucketName()
                    + mRequest.getObjectKey() + String.valueOf(mRequest.getPartSize())).getBytes());
            String recordPath = mRequest.getRecordDirectory() + "/" + recordFileName;
            mRecordFile = new File(recordPath);
            if (mRecordFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(mRecordFile));
                mUploadId = br.readLine();
                br.close();

                OSSLog.logDebug("[initUploadId] - Found record file, uploadid: " + mUploadId);
                ListPartsRequest listParts = new ListPartsRequest(mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId);
                OSSAsyncTask<ListPartsResult> task = mApiOperation.listParts(listParts, null);
                try {
                    List<PartSummary> parts = task.getResult().getParts();
                    for (int i = 0; i < parts.size(); i++) {
                        PartSummary part = parts.get(i);
                        PartETag partETag = new PartETag(part.getPartNumber(), part.getETag());
                        mPartETags.add(partETag);
                        mUploadedLength += part.getSize();
                        mAlreadyUploadIndex.add(part.getPartNumber());
                        if (i == 0) {
                            mFirstPartSize = part.getSize();
                        }
                    }
                    return;
                } catch (ServiceException e) {
                    if (e.getStatusCode() == 404) {
                        mUploadId = null;
                    } else {
                        throw e;
                    }
                } catch (ClientException e) {
                    throw e;
                }
                task.waitUntilFinished();
            }

            if (!mRecordFile.exists() && !mRecordFile.createNewFile()) {
                throw new ClientException("Can't create file at path: " + mRecordFile.getAbsolutePath()
                        + "\nPlease make sure the directory exist!");
            }
        }

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(
                mRequest.getBucketName(), mRequest.getObjectKey(), mRequest.getMetadata());

        InitiateMultipartUploadResult initResult = mApiOperation.initMultipartUpload(init, null).getResult();

        mUploadId = initResult.getUploadId();
        mRequest.setUploadId(mUploadId);
        if (mRecordFile != null) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(mRecordFile));
            bw.write(mUploadId);
            bw.close();
        }
    }

    @Override
    protected ResumableUploadResult doMultipartUpload() throws IOException, ClientException, ServiceException, InterruptedException {

        long tempUploadedLength = mUploadedLength;

        checkCancel();

        int[] partAttr = new int[2];
        checkPartSize(partAttr);
        int readByte = partAttr[0];
        final int partNumber = partAttr[1];

        if (mPartETags.size() > 0 && mAlreadyUploadIndex.size() > 0) { //revert progress
            if (mUploadedLength > mFileLength) {
                throw new ClientException("The uploading file is inconsistent with before");
            }

            if (mFirstPartSize != readByte) {
                throw new ClientException("The part size setting is inconsistent with before");
            }

            if (mProgressCallback != null) {
                mProgressCallback.onProgress(mRequest, mUploadedLength, mFileLength);
            }
        }

        for (int i = 0; i < partNumber; i++) {

            if (mAlreadyUploadIndex.size() != 0 && mAlreadyUploadIndex.contains(i + 1)) {
                continue;
            }

            if (mPoolExecutor != null) {
                //need read byte
                if (i == partNumber - 1) {
                    readByte = (int) Math.min(readByte, mFileLength - tempUploadedLength);
                }
                final int byteCount = readByte;
                final int readIndex = i;
                tempUploadedLength += byteCount;
                mPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        uploadPart(readIndex, byteCount, partNumber);
                    }
                });
            }
        }

        if (checkWaitCondition(partNumber)) {
            synchronized (mLock) {
                mLock.wait();
            }
        }

        checkException();
        //complete sort
        CompleteMultipartUploadResult completeResult = completeMultipartUploadResult();

        if (mRecordFile != null && completeResult != null) {
            mRecordFile.delete();
        }

        releasePool();
        return new ResumableUploadResult(completeResult);
    }

    @Override
    protected void checkException() throws IOException, ServiceException, ClientException {
        if (mContext.getCancellationHandler().isCancelled()) {
            if (mRequest.deleteUploadOnCancelling()) {
                abortThisUpload();
                if (mRecordFile != null) {
                    mRecordFile.delete();
                }
            }
        }
        super.checkException();
    }

    @Override
    protected void abortThisUpload() {
        if (mUploadId != null) {
            AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId);
            mApiOperation.abortMultipartUpload(abort, null).waitUntilFinished();
        }
    }

    @Override
    protected void processException(Exception e) {
        synchronized (mLock) {
            mPartExceptionCount++;
            if (mUploadException == null || !e.getMessage().equals(mUploadException.getMessage())) {
                mUploadException = e;
            }
            OSSLog.logThrowable2Local(e);
            if (mContext.getCancellationHandler().isCancelled()) {
                if (!mIsCancel) {
                    mIsCancel = true;
                    stopUpload();
                    mLock.notify();
                }
            }
        }
    }
}
