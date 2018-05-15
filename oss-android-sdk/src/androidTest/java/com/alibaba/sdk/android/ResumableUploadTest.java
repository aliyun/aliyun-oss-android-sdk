package com.alibaba.sdk.android;

import android.os.Environment;

import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhouzhuo on 11/27/15.
 */
public class ResumableUploadTest extends BaseTestCase {

    private final static String UPLOAD_DEFAULT_FILE = "guihua.zip";
    private final static String UPLOAD_FILE1M = "file1m";
    private final static String UPLOAD_BIGFILE = "bigfile.zip";

    @Override
    void initTestData() throws Exception {
        OSSTestConfig.initLocalFile();
        OSSTestConfig.initDemoFile(UPLOAD_DEFAULT_FILE);
        OSSTestConfig.initDemoFile("demo.pdf");
    }

    public void testResumableUpload() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(mBucketName, UPLOAD_DEFAULT_FILE,
                OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE, getContext().getFilesDir().getAbsolutePath());
        rq.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        ResumableUploadResult result = oss.resumableUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_DEFAULT_FILE, OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE);

    }

    public void testResumableUploadWithServerError() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M, getContext().getFilesDir().getAbsolutePath());
        rq.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });
        OSSCredentialProvider provider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                return "xxx";
            }
        };
        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, provider);
        ServiceException serviceException = null;
        try {
            ResumableUploadResult result = oss.resumableUpload(rq);
        } catch (ServiceException e) {
            serviceException = e;
        }

        assertTrue(serviceException != null);
    }

    public void testResumableUploadWithServerCallback() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M, getContext().getFilesDir().getAbsolutePath());
        rq.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", OSSTestConfig.CALLBACK_SERVER);
                put("callbackBody", "test");
            }
        });
        rq.setCallbackVars(new HashMap<String, String>() {
            {
                put("x:var1", "value");
            }
        });
        rq.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        ResumableUploadResult result = oss.resumableUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertNotNull(result.getServerCallbackReturnBody());

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    public void testResumableUploadAsync() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertEquals(UPLOAD_FILE1M, callback.request.getObjectKey());
        assertEquals(200, callback.result.getStatusCode());

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    public void testResumableUploadAsyncWithInvalidBucket() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest("#bucketName", UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("The bucket name is invalid"));
    }

    public void testResumableUploadAsyncWithInvalidObjectKey() throws Exception {
        ObjectMetadata meta = new ObjectMetadata();
        meta.addUserMetadata("x-oss-meta-name3", "value3");
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, "//file1m",
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M, meta);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void testResumableUploadAsyncWithNullObjectKey() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, null,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.clientException);
    }

    public void testResumableUploadCancel() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_DEFAULT_FILE,
                OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_DEFAULT_FILE, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        Thread.sleep(100);
        task.cancel();
        task.waitUntilFinished();

        assertNotNull(callback.clientException);
        callback.clientException.printStackTrace();
    }

    private void resumableUpload1mToFile(final String fileName) throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, fileName,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);

        request.setPartSize(100 * 1024);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(fileName, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestUtils.checkFileMd5(oss, mBucketName, fileName, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    public void testResumableUploadWithErrorParts() {
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);

        try {
            request.setPartSize(1024);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testResumableUploadWithErrorRecordDirectory() {
        try {
            ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                    OSSTestConfig.FILE_DIR + UPLOAD_FILE1M, "ErrorRecordDirectory");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testConcurrentResumableUpload() throws Exception {
        final CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        resumableUpload1mToFile("resumableUpload" + index);
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        assertTrue(false);
                        latch.countDown();
                    }
                }
            }).start();
        }
        latch.await();
    }

    public void testResumableUploadWithRecordDirSetting() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M, getContext().getFilesDir().getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertEquals(UPLOAD_FILE1M, callback.request.getObjectKey());
        assertEquals(200, callback.result.getStatusCode());

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    public void testResumableUploadAbort() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M, getContext().getFilesDir().getAbsolutePath());
        request.setDeleteUploadOnCancelling(false);
        request.setCRC64(OSSRequest.CRC64Config.YES);
        request.setPartSize(100 * 1024);
        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
                if (currentSize > totalSize / 3) {
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

        oss.abortResumableUpload(request);

        request = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M, getContext().getFilesDir().getAbsolutePath());
        request.setCRC64(OSSRequest.CRC64Config.YES);
        request.setPartSize(100 * 1024);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    public void testResumableUploadCancelledAndResume() throws Exception {
        final String objectKey = UPLOAD_DEFAULT_FILE;
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, objectKey,
                OSSTestConfig.FILE_DIR + objectKey, getContext().getFilesDir().getAbsolutePath());
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

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        while (!needCancelled.get()) {
            Thread.sleep(100);
        }
        task.cancel();
        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);

        request = new ResumableUploadRequest(mBucketName, objectKey,
                OSSTestConfig.FILE_DIR + objectKey, getContext().getFilesDir().getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(objectKey, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
                assertTrue(currentSize > totalSize / 4);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestUtils.checkFileMd5(oss, mBucketName, objectKey, OSSTestConfig.FILE_DIR + objectKey);
    }

    public void testResumableUploadWithRecordDirCancel() throws Exception {

        File recordDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/oss_record/");
        if (!recordDir.exists()) {
            recordDir.mkdirs();
        }

        OSSLog.logDebug("recorddir - " + recordDir.getAbsolutePath());

        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_DEFAULT_FILE,
                OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE, recordDir.getAbsolutePath());

        request.setDeleteUploadOnCancelling(false);

        final CountDownLatch latch = new CountDownLatch(1);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_DEFAULT_FILE, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] 001 - " + currentSize + " " + totalSize, false);
                if (currentSize > totalSize / 10) {
                    latch.countDown();
                }
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        latch.await();
        task.cancel();

        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);

        request = new ResumableUploadRequest(mBucketName, UPLOAD_DEFAULT_FILE,
                OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE, recordDir.getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_DEFAULT_FILE, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] 002 - " + currentSize + " " + totalSize, false);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_DEFAULT_FILE, OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE);
    }

    public void testResumableUploadMithSpecifiedMeta() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(UPLOAD_FILE1M, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("application/binary-stream");
        meta.addUserMetadata("x-oss-meta-name3", "value3");
        request.setMetadata(meta);

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertEquals(UPLOAD_FILE1M, callback.request.getObjectKey());
        assertEquals(200, callback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, UPLOAD_FILE1M);
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals(200, headResult.getStatusCode());

        meta = headResult.getMetadata();
        assertEquals("application/binary-stream", meta.getContentType());
        assertEquals("value3", meta.getUserMetadata().get("x-oss-meta-name3"));

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    public void testResumableUploadMore1000AndCancel() throws Exception {

        File recordDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/oss_record/");
        if (!recordDir.exists()) {
            recordDir.mkdirs();
        }

        final long partSize = 256 * 1024;

        OSSLog.logDebug("recorddir - " + recordDir.getAbsolutePath());

        ResumableUploadRequest request = new ResumableUploadRequest(mBucketName, UPLOAD_BIGFILE,
                OSSTestConfig.FILE_DIR + UPLOAD_BIGFILE, recordDir.getAbsolutePath());

        request.setDeleteUploadOnCancelling(false);
        request.setPartSize(partSize);

        final CountDownLatch latch = new CountDownLatch(1);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {


            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("bigfile progress 001 - " + currentSize + " " + totalSize + " index : " + (currentSize / partSize), false);
                if (currentSize / partSize > 1010) {
                    latch.countDown();
                }
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        latch.await();
        OSSLog.logDebug("bigfile progress cancel " , false);
        task.cancel();

        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);

        request = new ResumableUploadRequest(mBucketName, UPLOAD_BIGFILE,
                OSSTestConfig.FILE_DIR + UPLOAD_BIGFILE, recordDir.getAbsolutePath());

        request.setPartSize(partSize);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("bigfile progress 002 - " + currentSize + " " + totalSize + " index : " + (currentSize / partSize), false);
                assertTrue(currentSize / partSize > 1010);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_BIGFILE, OSSTestConfig.FILE_DIR + UPLOAD_BIGFILE);
    }
}
