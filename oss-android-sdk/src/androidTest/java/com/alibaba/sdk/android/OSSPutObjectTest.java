package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.callback.OSSRetryCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteMultipleObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteMultipleObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSPutObjectTest extends BaseTestCase {

    @Override
    void initTestData() throws Exception {
        OSSTestConfig.initLocalFile();
        OSSTestConfig.initDemoFile("guihua.zip");
        OSSTestConfig.initDemoFile("demo.pdf");
    }

    public void testPutObjectFromFile() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m.jpg",
                OSSTestConfig.FILE_DIR + "file1m");
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
                + OSSTestConfig.FILE_DIR + "file1m");
        metadata.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        metadata.setCacheControl("no-cache");
        metadata.setContentEncoding("gzip");
        metadata.setUserMetadata(userMetadata);
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "file1m.jpg");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals("image/jpeg", headResult.getMetadata().getContentType());
    }

    public void testPutObjectFromByteArray() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "byteData",
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

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "byteData");
        HeadObjectResult headResult = oss.headObject(head);

        OSSLog.logDebug("headResult", headResult.getMetadata().toString());
        assertEquals("application/octet-stream", headResult.getMetadata().getContentType());
    }

    public void testPutObjectFromByteArrayWithExpiration() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "byteData",
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

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "byteData");
        HeadObjectResult headResult = oss.headObject(head);

        OSSLog.logDebug("headResult", headResult.getMetadata().toString());
        assertEquals("application/octet-stream", headResult.getMetadata().getContentType());
    }

    public void testPutObjectDefaultContentType() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "test_upload.apk",
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

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "test_upload.apk");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals("application/vnd.android.package-archive", headResult.getMetadata().getContentType());
    }


    public void testPutObjectCheckContentMd5() throws Exception {
        String fileName = "demo.pdf";
        PutObjectRequest put = new PutObjectRequest(mBucketName, fileName,
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

    public void testSyncAppendObjectWithFile() throws Exception {

        DeleteObjectRequest delete = new DeleteObjectRequest(mBucketName, "append_file1m");
        oss.deleteObject(delete);

        AppendObjectRequest append = new AppendObjectRequest(mBucketName, "append_file1m",
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

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "append_file1m");
        HeadObjectResult result = oss.headObject(head);

        assertEquals(200, result.getStatusCode());
        assertEquals(1024 * 1000 * 2, result.getMetadata().getContentLength());
    }

    public void testAppendObjectWithFile() throws Exception {

        DeleteObjectRequest delete = new DeleteObjectRequest(mBucketName, "append_file1m");
        oss.deleteObject(delete);

        AppendObjectRequest append = new AppendObjectRequest(mBucketName, "append_file1m",
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

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "append_file1m");
        HeadObjectResult result = oss.headObject(head);

        assertEquals(200, result.getStatusCode());
        assertEquals(1024 * 1000 * 2, result.getMetadata().getContentLength());
    }

    public void testAppendObjectWithByte() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(mBucketName, "append_file1m");
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
        AppendObjectRequest append = new AppendObjectRequest(mBucketName, "append_file1m",
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

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "append_file1m");
        HeadObjectResult result = oss.headObject(head);

        assertEquals(200, result.getStatusCode());
        assertEquals(1024 * 1000 * 2, result.getMetadata().getContentLength());
    }

    public void testPutObjectWithServerCallback() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
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


    public void testPutObjectWithLargeServerCallback() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setCallbackParam(new HashMap<String, String>() {{
            put("callbackUrl", "dc.pier39.cn/mts-mns/phpserver/shjcallback.php");
            put("callbackBody", "test");
        }});

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        assertNotNull(putCallback.result.getServerCallbackReturnBody());
        OSSLog.logError("-------------- serverCallback: " + putCallback.result.getServerCallbackReturnBody());
    }

    public void testServerCallbackFailed() throws Exception {
        final PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
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


    public void testPutObjectWithWrongUserMetaData() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("x-oss-wrong-key", "Value1");
        put.setMetadata(metadata);
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "file1m");
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(0, headResult.getMetadata().getUserMetadata().size());
    }

    public void testPutObjectWithNotExistFile() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "nofile",
                OSSTestConfig.FILE_DIR + "nofile");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        OSSLog.logError("clientException: " + putCallback.clientException.toString());
    }

    public void testPutObjectWithNotExistBucket() throws Exception {
        PutObjectRequest put = new PutObjectRequest("wrong-bucket", "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.serviceException);
        assertEquals(404, putCallback.serviceException.getStatusCode());
        OSSLog.logError("serviceException" + putCallback.serviceException.toString());
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
        PutObjectRequest put = new PutObjectRequest(mBucketName, "//file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void testPutObjectWithNullObjectKey() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, null,
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void testPutObjectToCoverOldFile() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        // pre upload
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "file1m");
        HeadObjectResult headResult = oss.headObject(head);
        Date preModifiedDate = headResult.getMetadata().getLastModified();
        long preFileSize = headResult.getMetadata().getContentLength();
        OSSLog.logInfo("pre upload file modified date: " + preModifiedDate.toString() + "file size: " + preFileSize);

        Thread.sleep(2 * 1000);

        // using the same file_key to cover the old one
        put = new PutObjectRequest(mBucketName, "file1m",
                OSSTestConfig.FILE_DIR + "file100k");
        task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        head = new HeadObjectRequest(mBucketName, "file1m");
        headResult = oss.headObject(head);
        Date afterModifiedDate = headResult.getMetadata().getLastModified();
        long afterFileSize = headResult.getMetadata().getContentLength();
        OSSLog.logInfo("pre upload file modified date: " + afterModifiedDate.toString() + "file size: " + afterFileSize);
        assertEquals(true, preModifiedDate.before(afterModifiedDate) && preFileSize > afterFileSize);
    }

    public void testPutObjectWithInitiativeCancel() throws Exception {
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PutObjectRequest put = new PutObjectRequest(mBucketName, "file10m",
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

    public void testPutObjectWithException() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
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

    public void testPutObjectCancel() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file10m",
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

    public void testPutObjectIsCompletedJudgement() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
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

    public void testPutObjectWithRetryCallback() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
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

    public void testDeleteMultipleObject() {
        String fimeName01 = "file1m";
        String objectkey01 = fimeName01;
        PutObjectRequest put01 = new PutObjectRequest(mBucketName, fimeName01,
                OSSTestConfig.FILE_DIR + objectkey01);

        String fimeName02 = "demo.pdf";
        String objectkey02 = fimeName02;
        PutObjectRequest put02 = new PutObjectRequest(mBucketName, fimeName02,
                OSSTestConfig.FILE_DIR + objectkey02);

        try {
            PutObjectResult result01 = oss.putObject(put01);
            assertEquals(200, result01.getStatusCode());
            PutObjectResult result02 = oss.putObject(put02);
            assertEquals(200, result02.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            assertNull(e);
        }

        List<String> objectKeys = new ArrayList<String>();
        objectKeys.add(objectkey01);
        objectKeys.add(objectkey02);

        DeleteMultipleObjectRequest request = new DeleteMultipleObjectRequest(mBucketName, objectKeys, false);
        try {
            DeleteMultipleObjectResult result = oss.deleteMultipleObject(request);
            assertEquals(200, result.getStatusCode());
            List<String> objects = result.getDeletedObjects();
            for (int i = 0; i < objects.size(); i++) {
                OSSLog.logDebug("delete object name : " + objects.get(i).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertNull(e);
        }

    }


}


