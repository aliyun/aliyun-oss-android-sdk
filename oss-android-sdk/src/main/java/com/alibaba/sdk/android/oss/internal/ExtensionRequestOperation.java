package com.alibaba.sdk.android.oss.internal;

import android.os.Environment;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by zhouzhuo on 11/27/15.
 */
public class ExtensionRequestOperation {

    private static ExecutorService executorService =
            Executors.newFixedThreadPool(OSSConstants.DEFAULT_BASE_THREAD_POOL_SIZE, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "oss-android-extensionapi-thread");
                }
            });
    private InternalRequestOperation apiOperation;

    public ExtensionRequestOperation(InternalRequestOperation apiOperation) {
        this.apiOperation = apiOperation;
    }

    public boolean doesObjectExist(String bucketName, String objectKey)
            throws ClientException, ServiceException {

        try {
            HeadObjectRequest head = new HeadObjectRequest(bucketName, objectKey);
            apiOperation.headObject(head, null).getResult();
            return true;
        } catch (ServiceException e) {
            if (e.getStatusCode() == 404) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public void abortResumableUpload(ResumableUploadRequest request) throws IOException {
        setCRC64(request);
        String uploadFilePath = request.getUploadFilePath();

        if (!OSSUtils.isEmptyString(request.getRecordDirectory())) {
            String fileMd5 = BinaryUtil.calculateMd5Str(uploadFilePath);
            String recordFileName = BinaryUtil.calculateMd5Str((fileMd5 + request.getBucketName()
                    + request.getObjectKey() + String.valueOf(request.getPartSize())).getBytes());
            String recordPath = request.getRecordDirectory() + "/" + recordFileName;
            File recordFile = new File(recordPath);

            if (recordFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(recordFile));
                String uploadId = br.readLine();
                br.close();

                OSSLog.logDebug("[initUploadId] - Found record file, uploadid: " + uploadId);

                if (request.getCRC64() == OSSRequest.CRC64Config.YES) {
                    String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "oss" + File.separator + uploadId;
                    File file = new File(filePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }

                AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(
                        request.getBucketName(), request.getObjectKey(), uploadId);
                apiOperation.abortMultipartUpload(abort, null);
            }

            if (recordFile != null) {
                recordFile.delete();
            }
        }
    }

    public OSSAsyncTask<ResumableUploadResult> resumableUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest
            , ResumableUploadResult> completedCallback) {
        setCRC64(request);
        ExecutionContext<ResumableUploadRequest, ResumableUploadResult> executionContext =
                new ExecutionContext(apiOperation.getInnerClient(), request, apiOperation.getApplicationContext());

        return OSSAsyncTask.wrapRequestTask(executorService.submit(new ResumableUploadTask(request,
                completedCallback, executionContext, apiOperation)), executionContext);
    }

    public OSSAsyncTask<ResumableUploadResult> sequenceUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest
            , ResumableUploadResult> completedCallback) {
        setCRC64(request);
        ExecutionContext<ResumableUploadRequest, ResumableUploadResult> executionContext =
                new ExecutionContext(apiOperation.getInnerClient(), request, apiOperation.getApplicationContext());

        SequenceUploadTask task = new SequenceUploadTask(request,
                completedCallback, executionContext, apiOperation);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(task), executionContext);
    }


    public OSSAsyncTask<CompleteMultipartUploadResult> multipartUpload(MultipartUploadRequest request
            , OSSCompletedCallback<MultipartUploadRequest
            , CompleteMultipartUploadResult> completedCallback) {
        setCRC64(request);
        ExecutionContext<MultipartUploadRequest, CompleteMultipartUploadResult> executionContext =
                new ExecutionContext(apiOperation.getInnerClient(), request, apiOperation.getApplicationContext());

        return OSSAsyncTask.wrapRequestTask(executorService.submit(new MultipartUploadTask(apiOperation
                , request, completedCallback, executionContext)), executionContext);
    }

    private void setCRC64(OSSRequest request) {
        Enum crc64 = request.getCRC64() != OSSRequest.CRC64Config.NULL ? request.getCRC64() :
                (apiOperation.getConf().isCheckCRC64() ? OSSRequest.CRC64Config.YES : OSSRequest.CRC64Config.NO);
        request.setCRC64(crc64);
    }
}
