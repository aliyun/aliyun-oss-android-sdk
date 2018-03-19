package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
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

public class CRC64Test extends BaseTestCase {

    private String testFile = "guihua.zip";

    @Override
    void initTestData() {
        OSSTestConfig.initLocalFile();
        OSSTestConfig.initDemoFile("guihua.zip");
        OSSTestConfig.initDemoFile("demo.pdf");
    }

    public void testCRC64GetObject() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, testFile,
                OSSTestConfig.FILE_DIR + "guihua.zip");
        oss.putObject(put);

        GetObjectRequest request = new GetObjectRequest(mBucketName, testFile);
        request.setCRC64(OSSRequest.CRC64Config.YES);
        request.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });

        GetObjectResult result = oss.getObject(request);

        OSSLog.logDebug("getObject CRC 64 before read : " + result.getClientCRC(), false);

        IOUtils.readStreamAsBytesArray(result.getObjectContent());

        OSSLog.logDebug("getObject CRC 64 after read : " + result.getClientCRC(), false);

        result.getObjectContent().close();
    }

    public void testCRC64PutObject() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, testFile,
                OSSTestConfig.FILE_DIR + testFile);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setCRC64(OSSRequest.CRC64Config.YES);
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

    }

    public void testCRC64AppendObject() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(mBucketName, "append_file1m");
        oss.deleteObject(delete);

        AppendObjectRequest append = new AppendObjectRequest(mBucketName, "append_file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        append.setInitCRC64(0L);
        append.setCRC64(OSSRequest.CRC64Config.YES);
        append.setProgressCallback(new OSSProgressCallback<AppendObjectRequest>() {
            @Override
            public void onProgress(AppendObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestAppendCallback appendCallback = new OSSTestConfig.TestAppendCallback();

        // first append
        OSSAsyncTask task = oss.asyncAppendObject(append, appendCallback);
        task.waitUntilFinished();

        assertEquals(200, appendCallback.result.getStatusCode());

        append.setInitCRC64(appendCallback.result.getClientCRC());
        append.setPosition(1024 * 1000);

        appendCallback = new OSSTestConfig.TestAppendCallback();

        // second append
        task = oss.asyncAppendObject(append, appendCallback);
        task.waitUntilFinished();

        assertEquals(200, appendCallback.result.getStatusCode());

    }

    public void testUploadPartAndCompleteCRC64() throws Exception {
        String objectKey = "multipart";
        List<PartETag> partETagList = new ArrayList<PartETag>();
        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(mBucketName, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        byte[] data = new byte[100 * 1024];
        UploadPartRequest uploadPart1 = new UploadPartRequest(mBucketName,
                objectKey, uploadId, 1);
        uploadPart1.setPartContent(data);
        uploadPart1.setCRC64(OSSRequest.CRC64Config.YES);
        UploadPartResult uploadPartResult1 = oss.uploadPart(uploadPart1);

        PartETag eTag1 = new PartETag(1, uploadPartResult1.getETag());
        eTag1.setPartSize(data.length);
        eTag1.setCRC64(uploadPartResult1.getClientCRC());
        partETagList.add(eTag1);

        UploadPartRequest uploadPart2 = new UploadPartRequest(mBucketName,
                objectKey, uploadId, 2);
        uploadPart2.setPartContent(data);
        uploadPart2.setCRC64(OSSRequest.CRC64Config.YES);
        UploadPartResult uploadPartResult2 = oss.uploadPart(uploadPart2);

        PartETag eTag2 = new PartETag(2, uploadPartResult1.getETag());
        eTag2.setPartSize(data.length);
        eTag2.setCRC64(uploadPartResult2.getClientCRC());
        partETagList.add(eTag2);


        CompleteMultipartUploadRequest complete
                = new CompleteMultipartUploadRequest(mBucketName, objectKey, uploadId, partETagList);
        complete.setCRC64(OSSRequest.CRC64Config.YES);
        oss.completeMultipartUpload(complete);

    }

    public void testCRC64Error() throws Exception {
        String objectKey = "multipart";
        List<PartETag> partETagList = new ArrayList<PartETag>();
        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(mBucketName, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        byte[] data = new byte[100 * 1024];
        UploadPartRequest uploadPart1 = new UploadPartRequest(mBucketName,
                objectKey, uploadId, 1);
        uploadPart1.setPartContent(data);
        uploadPart1.setCRC64(OSSRequest.CRC64Config.YES);
        UploadPartResult uploadPartResult1 = oss.uploadPart(uploadPart1);

        PartETag eTag1 = new PartETag(1, uploadPartResult1.getETag());
        eTag1.setPartSize(data.length);
        eTag1.setCRC64(uploadPartResult1.getClientCRC());
        partETagList.add(eTag1);

        UploadPartRequest uploadPart2 = new UploadPartRequest(mBucketName,
                objectKey, uploadId, 2);
        uploadPart2.setPartContent(data);
        uploadPart2.setCRC64(OSSRequest.CRC64Config.YES);
        UploadPartResult uploadPartResult2 = oss.uploadPart(uploadPart2);

        PartETag eTag2 = new PartETag(2, uploadPartResult1.getETag());
        eTag2.setPartSize(data.length);
        long wrongCRC64 = 120000333L;
        eTag2.setCRC64(wrongCRC64);
        partETagList.add(eTag2);

        CompleteMultipartUploadRequest complete
                = new CompleteMultipartUploadRequest(mBucketName, objectKey, uploadId, partETagList);
        complete.setCRC64(OSSRequest.CRC64Config.YES);
        try {
            oss.completeMultipartUpload(complete);
        } catch (ClientException e) {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof InconsistentException);
        }
    }

    public void testMultipartUploadWithCRC64() throws Exception {
        String filePath = OSSTestConfig.FILE_DIR.concat(testFile);
        String objectKey = "mul-" + testFile;
        MultipartUploadRequest request = new MultipartUploadRequest(mBucketName, objectKey, filePath);
        request.setCRC64(OSSRequest.CRC64Config.YES);
        request.setProgressCallback(new OSSProgressCallback() {
            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                OSSLog.logDebug("progress: " + " " + currentSize + " " + totalSize, false);
            }
        });
        OSSTestConfig.TestMultipartUploadCallback multipartCallback
                = new OSSTestConfig.TestMultipartUploadCallback();
        OSSAsyncTask<CompleteMultipartUploadResult> task = oss.asyncMultipartUpload(request, multipartCallback);
        task.waitUntilFinished();

    }

    public void testResumableMultipartUploadWithCRC64() throws Exception {
        String filePath = OSSTestConfig.FILE_DIR.concat(testFile);
        String objectKey = "mul-" + testFile;
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, objectKey, filePath);
        request.setCRC64(OSSRequest.CRC64Config.YES);
        request.setDeleteUploadOnCancelling(false);
        request.setProgressCallback(new OSSProgressCallback() {
            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                OSSLog.logDebug("progress: " + " " + currentSize + " " + totalSize, false);
            }
        });
        OSSTestConfig.TestResumableUploadCallback multipartCallback
                = new OSSTestConfig.TestResumableUploadCallback();
        OSSAsyncTask<ResumableUploadResult> task = oss.asyncResumableUpload(request, multipartCallback);
        task.waitUntilFinished();

    }

    public void testResumableMultipartUploadCancelWithCRC64() throws Exception {
        final String objectKey = "file10m";
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, objectKey,
                OSSTestConfig.FILE_DIR + objectKey, OSSTestConfig.FILE_DIR);
        request.setDeleteUploadOnCancelling(false);
        request.setCRC64(OSSRequest.CRC64Config.YES);
        request.setPartSize(256 * 1024);
        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(objectKey, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
                if (currentSize > totalSize / 2) {
                    needCancelled.set(true);
                }
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        while (!needCancelled.get()) {
            Thread.sleep(100);
        }
        task.cancel();
        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);

        Thread.sleep(1000l);

        request = new ResumableUploadRequest(mBucketName, objectKey,
                OSSTestConfig.FILE_DIR + objectKey, OSSTestConfig.FILE_DIR);
        request.setDeleteUploadOnCancelling(false);
        request.setCRC64(OSSRequest.CRC64Config.YES);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(objectKey, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
                assertTrue(currentSize > totalSize / 3);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

    }
}
