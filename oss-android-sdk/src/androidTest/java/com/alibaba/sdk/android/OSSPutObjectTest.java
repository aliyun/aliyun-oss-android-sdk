package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.callback.OSSRetryCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSPutObjectTest extends AndroidTestCase {

    OSS oss;
    private final static String BUCKET_NAME = "oss-android-put-object-test";

    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
            try {
                CreateBucketRequest request = new CreateBucketRequest(BUCKET_NAME);
                oss.createBucket(request);
                OSSTestConfig.initLocalFile();
                OSSTestConfig.initDemoFile("guihua.zip");
                OSSTestConfig.initDemoFile("demo.pdf");
            } catch (Exception e) {
            }
        }

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, BUCKET_NAME);
        } catch (Exception e) {
        }
    }

    public void testPutObject() throws Exception{
        putObjectFromFileTest();
        putObjectFromByteArrayTest();
        putObjectFromByteArrayWithExpirationTest();
        putObjectDefaultContentTypeTest();
        putObjectCheckContentMd5Test();
        syncAppendObjectWithFileTest();
        appendObjectWithFileTest();
        appendObjectWithByteTest();
        putObjectWithServerCallbackTest();
        serverCallbackFailedTest();
        putObjectWithWrongUserMetaDataTest();
        putObjectWithNotExistFileTest();
        putObjectWithNotExistBucketTest();
        putObjectWithInvalidBucketNameTest();
        putObjectWithInvalidObjectKeyTest();
        putObjectWithNullObjectKeyTest();
        putObjectToCoverOldFileTest();
        putObjectWithInitiativeCancelTest();
        putObjectWithExceptionTest();
        putObjectCancelTest();
        putObjectIsCompletedJudgementTest();
        putObjectWithRetryCallbackTest();
    }

    public void putObjectFromFileTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m.jpg",
                OSSTestConfig.FILE_DIR + "/file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        ObjectMetadata metadata = new ObjectMetadata();
        Map<String, String> userMetadata = new HashMap<String, String>();
        userMetadata.put("userVar1", "value");
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        // Content-Disposition
        metadata.setContentDisposition("attachment;filename="
                + OSSTestConfig.FILE_DIR + "/file1m");
        metadata.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        metadata.setCacheControl("no-cache");
        metadata.setContentEncoding("gzip");
        metadata.setUserMetadata(userMetadata);
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "file1m.jpg");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals("image/jpeg", headResult.getMetadata().getContentType());
    }

    public void putObjectFromByteArrayTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "byteData",
                "TestData".getBytes());

        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "byteData");
        HeadObjectResult headResult = oss.headObject(head);

        OSSLog.logDebug("headResult", headResult.getMetadata().toString());
        assertEquals("application/octet-stream", headResult.getMetadata().getContentType());
    }

    public void putObjectFromByteArrayWithExpirationTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "byteData",
                "TestData".getBytes());

        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        metadata.setContentLength("TestData".getBytes().length);
        metadata.setExpirationTime(new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000)));
        metadata.setLastModified(new Date());
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "byteData");
        HeadObjectResult headResult = oss.headObject(head);

        OSSLog.logDebug("headResult", headResult.getMetadata().toString());
        assertEquals("application/octet-stream", headResult.getMetadata().getContentType());
    }

    public void putObjectDefaultContentTypeTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "test_upload.apk",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "test_upload.apk");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals("application/vnd.android.package-archive", headResult.getMetadata().getContentType());
    }


    public void putObjectCheckContentMd5Test() throws Exception {
        String fileName = "demo.pdf";
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, fileName,
                OSSTestConfig.FILE_DIR + fileName);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        ObjectMetadata metadata = new ObjectMetadata();
        String base64Md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(OSSTestConfig.FILE_DIR + fileName));
        metadata.setContentMD5(base64Md5);

        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
    }

    public void syncAppendObjectWithFileTest() throws Exception {

        DeleteObjectRequest delete = new DeleteObjectRequest(BUCKET_NAME, "append_file1m");
        oss.deleteObject(delete);

        AppendObjectRequest append = new AppendObjectRequest(BUCKET_NAME, "append_file1m",
                OSSTestConfig.FILE_DIR + "file1m");

        append.setProgressCallback(new OSSProgressCallback<AppendObjectRequest>() {
            @Override
            public void onProgress(AppendObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        // first append
        AppendObjectResult result1 = oss.appendObject(append);

        assertEquals(200, result1.getStatusCode());

        append.setPosition(1024 * 1000);

        // second append
        AppendObjectResult result2 = oss.appendObject(append);

        assertEquals(200, result2.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "append_file1m");
        HeadObjectResult result = oss.headObject(head);

        assertEquals(200, result.getStatusCode());
        assertEquals(1024 * 1000 * 2, result.getMetadata().getContentLength());
    }

    public void appendObjectWithFileTest() throws Exception {

        DeleteObjectRequest delete = new DeleteObjectRequest(BUCKET_NAME, "append_file1m");
        oss.deleteObject(delete);

        AppendObjectRequest append = new AppendObjectRequest(BUCKET_NAME, "append_file1m",
                OSSTestConfig.FILE_DIR + "file1m");

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

        append.setPosition(1024 * 1000);

        appendCallback = new OSSTestConfig.TestAppendCallback();

        // second append
        task = oss.asyncAppendObject(append, appendCallback);
        task.waitUntilFinished();

        assertEquals(200, appendCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "append_file1m");
        HeadObjectResult result = oss.headObject(head);

        assertEquals(200, result.getStatusCode());
        assertEquals(1024 * 1000 * 2, result.getMetadata().getContentLength());
    }

    public void appendObjectWithByteTest() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(BUCKET_NAME, "append_file1m");
        oss.deleteObject(delete);

        File file = new File(OSSTestConfig.FILE_DIR + "file1m");
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        byte[] b = new byte[4096];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
        in.close();
        out.close();
        byte[] ret = out.toByteArray();
        AppendObjectRequest append = new AppendObjectRequest(BUCKET_NAME, "append_file1m",
                ret);

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

        append.setPosition(1024 * 1000);

        appendCallback = new OSSTestConfig.TestAppendCallback();

        // second append
        task = oss.asyncAppendObject(append, appendCallback);
        task.waitUntilFinished();

        assertEquals(200, appendCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "append_file1m");
        HeadObjectResult result = oss.headObject(head);

        assertEquals(200, result.getStatusCode());
        assertEquals(1024 * 1000 * 2, result.getMetadata().getContentLength());
    }

    public void putObjectWithServerCallbackTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("x-oss-meta-Key2", "Value2");
        put.setMetadata(metadata);

        put.setCallbackParam(new HashMap<String, String>() {{
            put("callbackUrl", OSSTestConfig.CALLBACK_SERVER);
            put("callbackBody", "test");
        }});

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        assertNotNull(putCallback.result.getServerCallbackReturnBody());
        OSSLog.logError("-------------- serverCallback: " + putCallback.result.getServerCallbackReturnBody());
    }

    public void serverCallbackFailedTest() throws Exception {
        final PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        put.setMetadata(metadata);

        put.setCallbackParam(new HashMap<String, String>() {{
            put("callbackUrl", "110.75.82.106/mbaas/test");
            put("callbackBody", "test");
        }});

        put.setCallbackVars(new HashMap<String, String>() {{
            put("x:var1", "value");
        }});

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.serviceException);
        OSSLog.logError("--------------- serviceException: " + putCallback.serviceException.toString());
    }


    public void putObjectWithWrongUserMetaDataTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("x-oss-wrong-key", "Value1");
        put.setMetadata(metadata);
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "file1m");
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(0, headResult.getMetadata().getUserMetadata().size());
    }

    public void putObjectWithNotExistFileTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "nofile",
                OSSTestConfig.FILE_DIR + "nofile");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        OSSLog.logError("clientException: " + putCallback.clientException.toString());
    }

    public void putObjectWithNotExistBucketTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest("wrong-bucket", "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.serviceException);
        assertEquals(404, putCallback.serviceException.getStatusCode());
        OSSLog.logError("serviceException" + putCallback.serviceException.toString());
    }

    public void putObjectWithInvalidBucketNameTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest("#bucketName", "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("The bucket name is invalid"));
    }

    public void putObjectWithInvalidObjectKeyTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "//file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void putObjectWithNullObjectKeyTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, null,
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void putObjectToCoverOldFileTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        // pre upload
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "file1m");
        HeadObjectResult headResult = oss.headObject(head);
        Date preModifiedDate = headResult.getMetadata().getLastModified();
        long preFileSize = headResult.getMetadata().getContentLength();
        OSSLog.logInfo("pre upload file modified date: " + preModifiedDate.toString() + "file size: " + preFileSize);

        Thread.sleep(2 * 1000);

        // using the same file_key to cover the old one
        put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file100k");
        task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        head = new HeadObjectRequest(BUCKET_NAME, "file1m");
        headResult = oss.headObject(head);
        Date afterModifiedDate = headResult.getMetadata().getLastModified();
        long afterFileSize = headResult.getMetadata().getContentLength();
        OSSLog.logInfo("pre upload file modified date: " + afterModifiedDate.toString() + "file size: " + afterFileSize);
        assertEquals(true, preModifiedDate.before(afterModifiedDate) && preFileSize > afterFileSize);
    }

    public void putObjectWithInitiativeCancelTest() throws Exception {
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file10m",
                            OSSTestConfig.FILE_DIR + "file10m");
                    OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
                    put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                        @Override
                        public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                            OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
                        }
                    });
                    OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
                    latch1.await();
                    task.cancel();
                    task.waitUntilFinished();
                    assertNotNull(putCallback.clientException);
                    assertTrue(task.isCanceled());
                    OSSLog.logDebug(putCallback.clientException.getMessage(), false);
                    assertTrue(putCallback.clientException.getMessage().contains("Cancel")
                            || putCallback.clientException.getMessage().contains("cancel"));
                    latch2.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        latch1.countDown();
        latch2.await();
        OSSLog.logDebug("testPutObjectWithInitiativeCancel success!", false);
    }

    public void putObjectWithExceptionTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testPutObjectWithException] - " + currentSize + " " + totalSize, false);
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

    public void putObjectCancelTest() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file10m",
                OSSTestConfig.FILE_DIR + "file10m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testPutObjectCancel] - " + currentSize + " " + totalSize, false);
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

    public void putObjectIsCompletedJudgementTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testPutObjectIsCompletedJudgement] - " + currentSize + " " + totalSize, false);
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        assertFalse(task.isCompleted());
        task.waitUntilFinished();
        assertTrue(task.isCompleted());
        assertEquals(200, task.getResult().getStatusCode());
    }

    public void putObjectWithRetryCallbackTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testPutObjectWithRetryCallback] - " + currentSize + " " + totalSize, false);
                if (currentSize > totalSize / 2) {
                    throw new RuntimeException("Make you failed!");
                }
            }
        });
        put.setRetryCallback(new OSSRetryCallback() {
            @Override
            public void onRetryCallback() {
                OSSLog.logDebug("[testPutObjectWithRetryCallback] - onRetryCallback", false);
            }
        });
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("Make you failed!"));
    }
}


