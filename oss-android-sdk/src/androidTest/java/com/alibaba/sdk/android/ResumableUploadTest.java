package com.alibaba.sdk.android;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
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

    @Override
    public void setUp() throws Exception {
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
    }

    public void testResumableUpload() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "/file1m", getContext().getFilesDir().getAbsolutePath());

        ObjectMetadata meta = new ObjectMetadata();
        meta.setHeader("x-oss-object-acl", "public-read-write");
        rq.setMetadata(meta);
        rq.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file1m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        ResumableUploadResult result = oss.resumableUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());

        GetObjectRequest getRq = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        getRq.setIsAuthorizationRequired(false);
        GetObjectResult getRs = oss.getObject(getRq);
        assertNotNull(getRs);
        assertEquals(200, getRs.getStatusCode());
    }

    public void testResumableUploadWithServerCallback() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "/file1m", getContext().getFilesDir().getAbsolutePath());
        rq.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", "110.75.82.106/mbaas/callback");
                put("callbackBody", "test");
            }
        });
        rq.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file1m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        ResumableUploadResult result = oss.resumableUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertNotNull(result.getServerCallbackReturnBody());
    }

    public void testResumableUploadAsync() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "/file1m");

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file1m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertEquals("file1m", callback.request.getObjectKey());
        assertEquals(200, callback.result.getStatusCode());
    }

    public void testResumableUploadAsyncWithInvalidBucket() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest("#bucketName", "file1m",
                OSSTestConfig.FILE_DIR + "/file1m");

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file1m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("The bucket name is invalid"));
    }

    public void testResumableUploadAsyncWithInvalidObjectKey() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "//file1m",
                OSSTestConfig.FILE_DIR + "/file1m");

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file1m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void testResumableUploadAsyncWithNullObjectKey() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, null,
                OSSTestConfig.FILE_DIR + "/file1m");

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file1m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void testResumableUploadCancel() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "/file1m");

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file1m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        Thread.sleep(500);
        task.cancel();
        task.waitUntilFinished();

        assertNotNull(callback.clientException);
        callback.clientException.printStackTrace();
    }

    private void resumableUpload10mToFile(final String fileName) throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, fileName,
                OSSTestConfig.FILE_DIR + "/file10m");

        request.setPartSize(300 * 1024);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(fileName, request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
                if (!makeFailed && currentSize > totalSize / 2) {
                    makeFailed = true;
                    throw new RuntimeException("Make you failed!");
                }
                if (makeFailed) {
                    assertTrue(currentSize > totalSize / 3);
                }
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);
    }

    public void testResumableUploadResume() throws Exception {
        resumableUpload10mToFile("file10m");
    }

    public void testConcurrentResumableUpload() throws Exception {
        final CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            final int index= i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        resumableUpload10mToFile("file10m" + index);
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
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "/file1m", getContext().getFilesDir().getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file1m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertEquals("file1m", callback.request.getObjectKey());
        assertEquals(200, callback.result.getStatusCode());
    }

    public void testResumableUploadAbort() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "/file10m", getContext().getFilesDir().getAbsolutePath());
        request.setDeleteUploadOnCancelling(false);

        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file10m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
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

        oss.abortResumableUpload(request);

        request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "/file10m", getContext().getFilesDir().getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file10m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
                if (currentSize < totalSize / 3) {
                    throw new RuntimeException("Make you failed!");
                }
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);
    }

    public void testResumableUploadCancelledAndResume() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "/file10m", getContext().getFilesDir().getAbsolutePath());
        request.setDeleteUploadOnCancelling(false);

        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file10m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
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

        request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "/file10m", getContext().getFilesDir().getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file10m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
                assertTrue(currentSize > totalSize / 3);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);
    }

    public void testResumableUploadFailedAndResume() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "/file10m", getContext().getFilesDir().getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file10m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
                if (currentSize > totalSize / 2) {
                    makeFailed = true;
                    throw new RuntimeException("Make you failed!");
                }
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);

        request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "/file10m", getContext().getFilesDir().getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file10m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
                assertTrue(currentSize > totalSize / 3);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);
    }

    public void testResumableUploadWithRecordDirCancel() throws Exception {

        File recordDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/oss_record/");
        if (!recordDir.exists()) {
            recordDir.mkdirs();
        }

        OSSLog.logD("recorddir - " + recordDir.getAbsolutePath());

        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "/file10m", recordDir.getAbsolutePath());

        final CountDownLatch latch = new CountDownLatch(1);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file10m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
                if (currentSize > totalSize / 2) {
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

        request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "/file10m", getContext().getFilesDir().getAbsolutePath());

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            private boolean makeFailed = false;
            private long startCount = Long.MAX_VALUE;
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("file10m", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
                startCount = Math.min(startCount, currentSize);
                assertTrue(startCount == 2048);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);
    }

    public void testResumableUploadMithSpecifiedMeta() throws Exception {
        ResumableUploadRequest request = new ResumableUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "multipart",
                OSSTestConfig.FILE_DIR + "/file1m");

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals("multipart", request.getObjectKey());
                OSSLog.logD("[testResumableUpload] - " + currentSize + " " + totalSize);
            }
        });

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("application/binary-stream");
        meta.addUserMetadata("x-oss-meta-name3", "value3");
        request.setMetadata(meta);

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncResumableUpload(request, callback);

        task.waitUntilFinished();

        assertEquals("multipart", callback.request.getObjectKey());
        assertEquals(200, callback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "multipart");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals(200, headResult.getStatusCode());

        meta = headResult.getMetadata();
        assertEquals("application/binary-stream", meta.getContentType());
        assertEquals("value3", meta.getUserMetadata().get("x-oss-meta-name3"));
    }
}
