package com.alibaba.sdk.android;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
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
public class ResumableUploadTest extends AndroidTestCase {

    OSS oss;
    private final static String UPLOAD_DEFAULT_FILE = "guihua.zip";
    private final static String UPLOAD_FILE1M = "file1m";
    private final static String RESUMABLE_UPLOAD_TEST_BUCKET = "oss-android-resumable-test";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.plainTextAKSKcredentialProvider);
            try {
                CreateBucketRequest request = new CreateBucketRequest(RESUMABLE_UPLOAD_TEST_BUCKET);
                oss.createBucket(request);
            } catch (Exception e) {
            }

            OSSTestConfig.initLocalFile();
            OSSTestConfig.initDemoFile(UPLOAD_DEFAULT_FILE);
            OSSTestConfig.initDemoFile("demo.pdf");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, RESUMABLE_UPLOAD_TEST_BUCKET);
        } catch (Exception e) {
        }
    }

    public void testResumableMultipartUpload() throws Exception {
        resumableUploadTest();
        resumableUploadWithServerErrorTest();
        resumableUploadWithServerCallbackTest();
        resumableUploadAsyncTest();
        resumableUploadAsyncWithInvalidBucketTest();
        resumableUploadAsyncWithInvalidObjectKeyTest();
        resumableUploadAbortTest();
        resumableUploadAsyncWithNullObjectKeyTest();
        resumableUploadCancelledAndResumeTest();
        resumableUploadCancelTest();
        resumableUploadMithSpecifiedMetaTest();
        resumableUploadWithErrorPartsTest();
        resumableUploadWithErrorRecordDirectoryTest();
        resumableUploadWithRecordDirCancelTest();
        resumableUploadWithRecordDirSettingTest();
        concurrentResumableUploadTest();
    }

    private void resumableUploadTest() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_DEFAULT_FILE,
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

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_DEFAULT_FILE, OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE);

    }

    private void resumableUploadWithServerErrorTest() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
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

    private void resumableUploadWithServerCallbackTest() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
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

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    private void resumableUploadAsyncTest() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
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

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    private void resumableUploadAsyncWithInvalidBucketTest() throws Exception {
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

    private void resumableUploadAsyncWithInvalidObjectKeyTest() throws Exception {
        ObjectMetadata meta = new ObjectMetadata();
        meta.addUserMetadata("x-oss-meta-name3", "value3");
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, "//file1m",
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

    private void resumableUploadAsyncWithNullObjectKeyTest() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, null,
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

    private void resumableUploadCancelTest() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_DEFAULT_FILE,
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
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, fileName,
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

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, fileName, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    private void resumableUploadWithErrorPartsTest() {
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
                OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);

        try {
            request.setPartSize(1024);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    private void resumableUploadWithErrorRecordDirectoryTest() {
        try {
            ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
                    OSSTestConfig.FILE_DIR + UPLOAD_FILE1M, "ErrorRecordDirectory");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    private void concurrentResumableUploadTest() throws Exception {
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

    private void resumableUploadWithRecordDirSettingTest() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
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

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    private void resumableUploadAbortTest() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
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

        request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
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

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }

    private void resumableUploadCancelledAndResumeTest() throws Exception {
        final String objectKey = UPLOAD_DEFAULT_FILE;
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, objectKey,
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

        request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, objectKey,
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

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, objectKey, OSSTestConfig.FILE_DIR + objectKey);
    }

    private void resumableUploadWithRecordDirCancelTest() throws Exception {

        File recordDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/oss_record/");
        if (!recordDir.exists()) {
            recordDir.mkdirs();
        }

        OSSLog.logDebug("recorddir - " + recordDir.getAbsolutePath());

        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_DEFAULT_FILE,
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

        request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_DEFAULT_FILE,
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

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_DEFAULT_FILE, OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE);
    }

    private void resumableUploadMithSpecifiedMetaTest() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M,
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

        HeadObjectRequest head = new HeadObjectRequest(RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M);
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals(200, headResult.getStatusCode());

        meta = headResult.getMetadata();
        assertEquals("application/binary-stream", meta.getContentType());
        assertEquals("value3", meta.getUserMetadata().get("x-oss-meta-name3"));

        OSSTestUtils.checkFileMd5(oss, RESUMABLE_UPLOAD_TEST_BUCKET, UPLOAD_FILE1M, OSSTestConfig.FILE_DIR + UPLOAD_FILE1M);
    }
}
