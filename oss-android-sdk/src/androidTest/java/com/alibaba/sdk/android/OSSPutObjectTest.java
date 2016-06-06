package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSPutObjectTest extends AndroidTestCase {

    OSS oss;

    @Override
    public void setUp() throws Exception {
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
    }

    public void testPutObjectFromFile() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m.jpg",
                OSSTestConfig.FILE_DIR + "/file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logD("onProgress - " + currentSize + " " + totalSize);
            }
        });

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m.jpg");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals("image/jpeg", headResult.getMetadata().getContentType());
    }

    public void testPutObjectFromByteArray() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "byteData",
                "TestData".getBytes());

        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logD("onProgress - " + currentSize + " " + totalSize);
            }
        });

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals("application/octet-stream", headResult.getMetadata().getContentType());
    }

    public void testPutObjectDefaultContentType() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "test_upload.apk",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logD("onProgress - " + currentSize + " " + totalSize);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "test_upload.apk");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals("application/vnd.android.package-archive", headResult.getMetadata().getContentType());
    }

    public void testPutObjectCheckContentMd5() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logD("onProgress - " + currentSize + " " + totalSize);
            }
        });

        ObjectMetadata metadata = new ObjectMetadata();
        String base64Md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(OSSTestConfig.FILE_DIR + "file1m"));
        metadata.setContentMD5(base64Md5);

        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
    }

    public void testAppendObject() throws Exception {

        DeleteObjectRequest delete = new DeleteObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "append_file1m");
        oss.deleteObject(delete);

        AppendObjectRequest append = new AppendObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "append_file1m",
                OSSTestConfig.FILE_DIR + "file1m");

        append.setProgressCallback(new OSSProgressCallback<AppendObjectRequest>() {
            @Override
            public void onProgress(AppendObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logD("onProgress - " + currentSize + " " + totalSize);
            }
        });

        OSSTestConfig.TestAppendCallback appendCallback = new OSSTestConfig.TestAppendCallback();

        // first append
        OSSAsyncTask task = oss.asyncAppendObject(append, appendCallback);
        task.waitUntilFinished();

        assertEquals(200, appendCallback.result.getStatusCode());

        append.setPosition(1024 * 1000);

        appendCallback = new OSSTestConfig.TestAppendCallback();

        // second append
        task = oss.asyncAppendObject(append, appendCallback);
        task.waitUntilFinished();

        assertEquals(200, appendCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "append_file1m");
        HeadObjectResult result = oss.headObject(head);

        assertEquals(200, result.getStatusCode());
        assertEquals(1024 * 1000 * 2, result.getMetadata().getContentLength());
    }

    public void testPutObjectWithServerCallback() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        put.setMetadata(metadata);

        put.setCallbackParam(new HashMap<String, String>() {{
            put("callbackUrl", "110.75.82.106/mbaas/callback");
            put("callbackBody", "test");
        }});

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        assertNotNull(putCallback.result.getServerCallbackReturnBody());
        OSSLog.logE("-------------- serverCallback: " + putCallback.result.getServerCallbackReturnBody());
    }

    public void testServerCallbackFailed() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        put.setMetadata(metadata);

        put.setCallbackParam(new HashMap<String, String>() {{
            put("callbackUrl", "110.75.82.106/mbaas/test");
            put("callbackBody", "test");
        }});

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.serviceException);
        OSSLog.logE("--------------- serviceException: " + putCallback.serviceException.toString());
    }


    public void testPutObjectWithWrongUserMetaData() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("x-oss-wrong-key", "Value1");
        put.setMetadata(metadata);;
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(0, headResult.getMetadata().getUserMetadata().size());
    }

    public void testPutObjectWithNotExistFile() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "nofile",
                OSSTestConfig.FILE_DIR + "nofile");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        OSSLog.logE("clientException: " + putCallback.clientException.toString());
    }

    public void testPutObjectWithNotExistBucket() throws Exception {
        PutObjectRequest put = new PutObjectRequest("wrong-bucket", "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.serviceException);
        assertEquals(404, putCallback.serviceException.getStatusCode());
        OSSLog.logE("serviceException" + putCallback.serviceException.toString());
    }

    public void testPutObjectWithInvalidBucketName() throws Exception {
        PutObjectRequest put = new PutObjectRequest("#bucketName", "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("The bucket name is invalid"));
    }

    public void testPutObjectWithInvalidObjectKey() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "//file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void testPutObjectWithNullObjectKey() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, null,
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void testPutObjectToCoverOldFile() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        // pre upload
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        HeadObjectResult headResult = oss.headObject(head);
        Date preModifiedDate = headResult.getMetadata().getLastModified();
        long preFileSize = headResult.getMetadata().getContentLength();
        OSSLog.logI("pre upload file modified date: " + preModifiedDate.toString() + "file size: " + preFileSize);

        Thread.sleep(2 * 1000);

        // using the same file_key to cover the old one
        put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file100k");
        task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        headResult = oss.headObject(head);
        Date afterModifiedDate = headResult.getMetadata().getLastModified();
        long afterFileSize = headResult.getMetadata().getContentLength();
        OSSLog.logI("pre upload file modified date: " + afterModifiedDate.toString() + "file size: " + afterFileSize);
        assertEquals(true, preModifiedDate.before(afterModifiedDate) && preFileSize > afterFileSize);
    }

    public void testPutObjectWithInitiativeCancel() throws Exception {
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                            OSSTestConfig.FILE_DIR + "file10m");
                    OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
                    put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                        @Override
                        public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                            OSSLog.logD("onProgress - " + currentSize + " " + totalSize);
                        }
                    });
                    OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
                    latch1.await();
                    task.cancel();
                    task.waitUntilFinished();
                    assertNotNull(putCallback.clientException);
                    assertTrue(task.isCanceled());
                    OSSLog.logD(putCallback.clientException.getMessage());
                    assertTrue(putCallback.clientException.getMessage().contains("Cancel")
                        || putCallback.clientException.getMessage().contains("cancel"));
                    latch2.countDown();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        latch1.countDown();
        latch2.await();
        OSSLog.logD("testPutObjectWithInitiativeCancel success!");
    }

    public void testConcurrentPutObject() throws Exception {
        final String fileNameArr[] = {"file1k", "file10k", "file100k", "file1m", "file10m"};
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(5);
        for(int i = 0; i < 5; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, fileNameArr[index],
                                OSSTestConfig.FILE_DIR + fileNameArr[index]);
                        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
                        latch1.await();
                        OSSAsyncTask putTask = oss.asyncPutObject(put, putCallback);
                        putTask.waitUntilFinished();
                        assertEquals(200, putTask.getResult().getStatusCode());
                        latch2.countDown();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        latch1.countDown();
        latch2.await();
        OSSLog.logD("testConcurrentPutObject success!");
    }

    public void testPutObjectWithException() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logD("[testPutObjectWithException] - " + currentSize + " " + totalSize);
                if (currentSize > totalSize / 2) {
                    throw new RuntimeException("Make you failed!");
                }
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("Make you failed!"));
    }

    public void testPutObjectCancel() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "file10m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logD("[testPutObjectCancel] - " + currentSize + " " + totalSize);
                if (currentSize > totalSize / 2) {
                    latch.countDown();
                }
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        assertFalse(task.isCompleted());
        latch.await();
        assertFalse(task.isCompleted());
        task.cancel();
        task.waitUntilFinished();
        assertTrue(task.isCompleted());
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("cancel"));
    }

    public void testPutObjectIsCompletedJudgement() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file10m",
                OSSTestConfig.FILE_DIR + "file10m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logD("[testPutObjectIsCompletedJudgement] - " + currentSize + " " + totalSize);
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        assertFalse(task.isCompleted());
        task.waitUntilFinished();
        assertTrue(task.isCompleted());
        assertEquals(200, task.getResult().getStatusCode());
    }
}


