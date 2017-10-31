package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
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

    public ResumableUploadTask(ResumableUploadRequest request,
                               OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback,
                               ExecutionContext context, InternalRequestOperation apiOperation) {
        super(apiOperation, request, completedCallback, context);
    }

    @Override
    protected void initMultipartUploadId() throws IOException, ClientException, ServiceException {
        String uploadFilePath = mRequest.getUploadFilePath();

        if (mRequest.getRecordDirectory() != null) {
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
                    for (PartSummary part : task.getResult().getParts()) {
                        mPartETags.add(new PartETag(part.getPartNumber(), part.getETag()));
                        mAlreadyUploadIndex.add(part.getPartNumber());
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

        if (mRecordFile != null) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(mRecordFile));
            bw.write(mUploadId);
            bw.close();
        }
    }

    @Override
    protected ResumableUploadResult doMultipartUpload() throws IOException, ClientException, ServiceException, InterruptedException {

        checkCancel();

        mUploadFile = new File(mRequest.getUploadFilePath());
        mFileLength = mUploadFile.length();
        if(mFileLength == 0){
            throw new ClientException("file length must not be 0");
        }
        int[] partAttr = new int[2];
        checkPartSize(partAttr);
        int readByte = partAttr[0];
        final int partNumber = partAttr[1];
        int currentLength = mAlreadyUploadIndex.size() * readByte;
        for (int i = 0; i < partNumber; i++) {

            checkException();

            if(mAlreadyUploadIndex.size()!=0 && mAlreadyUploadIndex.contains(i+1)){
                continue;
            }

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
                        uploadPart(readIndex, byteCount, partNumber);
                    }
                });
            }
        }

        if(checkWaitCondition(partNumber)) {
            synchronized (mLock) {
                mLock.wait();
            }
        }

        checkException();
        //complete sort
        CompleteMultipartUploadResult completeResult = completeMultipartUploadResult();

        if (mRecordFile != null) {
            mRecordFile.delete();
        }

        releasePool();
        return new ResumableUploadResult(completeResult);
    }

    @Override
    protected void checkCancel() throws ClientException {
        if (mContext.getCancellationHandler().isCancelled()) {
            if (mRequest.deleteUploadOnCancelling()) {
                abortThisUpload();
                if (mRecordFile != null) {
                    mRecordFile.delete();
                }
            }
            IOException e = new IOException();
            throw new ClientException(e.getMessage(), e);
        }
    }

    @Override
    protected void abortThisUpload() {
        if (mUploadId != null) {
            AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(
                    mRequest.getBucketName(), mRequest.getObjectKey(), mUploadId);
            mApiOperation.abortMultipartUpload(abort, null).waitUntilFinished();
        }
    }

}
