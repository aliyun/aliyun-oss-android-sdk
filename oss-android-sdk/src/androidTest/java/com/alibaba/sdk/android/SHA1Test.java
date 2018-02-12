package com.alibaba.sdk.android;

import android.test.AndroidTestCase;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.exception.InconsistentException;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by jingdan on 2017/11/29.
 */

public class SHA1Test extends AndroidTestCase {

    private String objectname = "sequence-object";
    private String testFile = "guihua.zip";

    private OSS oss;

    @Override
    protected void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
        OSSTestConfig.initLocalFile();
        OSSTestConfig.initDemoFile(testFile);
    }

    public void testPutObjectCheckSHA1() throws Exception {
        String fileName = testFile;
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectname,
                OSSTestConfig.FILE_DIR + fileName);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        ObjectMetadata metadata = new ObjectMetadata();
        String sha1Value = BinaryUtil.fileToSHA1(OSSTestConfig.FILE_DIR + fileName);
        metadata.setSHA1(sha1Value);
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
    }

    public void testPutObjectWithErrorSHA1() throws Exception {
        String fileName = testFile;
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectname,
                OSSTestConfig.FILE_DIR + fileName);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setSHA1("error sha1value");
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.serviceException);
        OSSLog.logError("serviceException: " + putCallback.serviceException.toString());
    }

    public void testSequenceUpload() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectname,
                OSSTestConfig.FILE_DIR + testFile);
        rq.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        ResumableUploadResult result = oss.sequenceUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());

        OSSTestConfig.checkFileMd5(oss, objectname, OSSTestConfig.FILE_DIR + testFile);
    }

    public void testSequenceUploadCancelledAndResume() throws Exception {
        final String objectKey = objectname;
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.FILE_DIR + testFile, OSSTestConfig.FILE_DIR);


        request.setDeleteUploadOnCancelling(false);
        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(objectKey, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
                if (currentSize > totalSize / 3) {
                    needCancelled.set(true);
                }
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncSequenceUpload(request, callback);

        while (!needCancelled.get()) {
            Thread.sleep(100);
        }
        task.cancel();
        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);
        OSSLog.logError("clientException: " + callback.clientException.toString());

        request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.FILE_DIR + testFile, OSSTestConfig.FILE_DIR);

        ObjectMetadata metadata = new ObjectMetadata();
        String sha1Value = BinaryUtil.fileToSHA1(OSSTestConfig.FILE_DIR + testFile);
        metadata.setSHA1(sha1Value);
        request.setMetadata(metadata);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {


            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(objectKey, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
                assertTrue(currentSize > totalSize / 3);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncSequenceUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestConfig.checkFileMd5(oss, objectKey, OSSTestConfig.FILE_DIR + testFile);
    }

}
