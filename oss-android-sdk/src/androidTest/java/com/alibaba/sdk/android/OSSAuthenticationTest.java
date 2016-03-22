package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by LK on 15/12/2.
 */
public class OSSAuthenticationTest extends AndroidTestCase {
    private OSS oss;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (oss == null) {
            OSSLog.enableLog();
            Thread.sleep(5 * 1000); // for logcat initialization
        }
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
    }

    public void testCustomSignCredentialProvider() throws Exception {
        final OSSCredentialProvider credentialProvider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                String signature = "";
                try {
                    signature = OSSUtils.sign(OSSTestConfig.AK.trim(), OSSTestConfig.SK.trim(), content);
                    assertNotNull(signature);
                    OSSLog.logD(signature);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return signature;
            }
        };
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, credentialProvider);
        assertNotNull(oss);
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask putTask = oss.asyncPutObject(put, putCallback);
        putTask.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
    }

    public void testNullCredentialProvider() throws Exception {
        boolean thrown = false;
        try {
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, null);
        } catch (Exception e) {
            thrown = true;
            assertTrue(e instanceof IllegalArgumentException);
        }
        assertTrue(thrown);
    }

    public void testPutToPublicBucket() throws Exception {
        OSSClient tempClient = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, new OSSPlainTextAKSKCredentialProvider("", ""));

        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.PUBLIC_READ_WRITE_BUCKET, "put.dat", "piece of data".getBytes());
        put.setIsAuthorizationRequired(false);
        PutObjectResult putResult = tempClient.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        DeleteObjectRequest delete = new DeleteObjectRequest(OSSTestConfig.PUBLIC_READ_WRITE_BUCKET, "put.dat");
        delete.setIsAuthorizationRequired(false);
        DeleteObjectResult deleteResult = tempClient.deleteObject(delete);
        assertEquals(204, deleteResult.getStatusCode());
    }

    public void testValidCustomSignCredentialProvider() throws Exception {
        final OSSCredentialProvider credentialProvider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                String signature = "";
                try {
                    signature = OSSUtils.sign("wrong-AK", "wrong-SK", content);
                    assertNotNull(signature);
                    OSSLog.logD(signature);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return signature;
            }
        };
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, credentialProvider);
        assertNotNull(oss);
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask putTask = oss.asyncPutObject(put, putCallback);
        putTask.waitUntilFinished();
        assertNotNull(putCallback.serviceException);
        assertEquals(403, putCallback.serviceException.getStatusCode());
    }

    public void testPutObjectWithNullFederationCredentialProvider() throws Exception {
        final OSSCredentialProvider credentialProvider = new OSSFederationCredentialProvider() {
        @Override
        public OSSFederationToken getFederationToken() {
            return null;
        }
    };
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, credentialProvider);
        assertNotNull(oss);
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask putTask = oss.asyncPutObject(put, putCallback);
        putTask.waitUntilFinished();
        assertNull(putCallback.result);
        assertNotNull(putCallback.clientException);
        assertTrue(putCallback.clientException.getMessage().contains("Can't get a federation token"));
    }

    public void testPutObjectWithWrongAKSKCredentiaProvider() throws Exception {
        final String AK = "wrongAK";
        final String SK = "wrongSK";
        final OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(AK, SK);
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, credentialProvider);
        assertNotNull(oss);
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask putTask = oss.asyncPutObject(put, putCallback);
        putTask.waitUntilFinished();
        assertNull(putCallback.result);
        assertEquals(403, putCallback.serviceException.getStatusCode());
    }



    public void testPresignObjectURL() throws Exception {
        String url = oss.presignConstrainedObjectURL(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m", 15 * 60);

        OSSLog.logD("[testPresignConstrainedObjectURL] - " + url);
        Request request = new Request.Builder().url(url).build();
        Response resp = new OkHttpClient().newCall(request).execute();

        assertEquals(200, resp.code());
        assertEquals(1024 * 1000, resp.body().contentLength());
    }

    public void testPresignPublicObjectURL() throws Exception {
        String url = oss.presignPublicObjectURL(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        OSSLog.logD("[testPresignPublicObjectURL] - " + url);
    }

    public void testPresignObjectURLWithWrongBucket() throws Exception {
        try {
            String url = oss.presignConstrainedObjectURL("wrong-bucket", "file1m", 15 * 60);
            Request request = new Request.Builder().url(url).build();
            Response response = new OkHttpClient().newCall(request).execute();
            assertEquals(404, response.code());
            assertEquals("Not Found", response.message());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testPresignObjectURLWithWrongObjectKey() throws Exception {
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT,
                new OSSPlainTextAKSKCredentialProvider(OSSTestConfig.AK, OSSTestConfig.SK));
        try {
            String url = oss.presignConstrainedObjectURL(OSSTestConfig.ANDROID_TEST_BUCKET, "wrong-key", 15 * 60);
            Request request = new Request.Builder().url(url).build();
            Response response = new OkHttpClient().newCall(request).execute();
            OSSLog.logE(response.body().string());
            assertEquals(404, response.code());
            assertEquals("Not Found", response.message());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testPresignObjectURLWithExpiredTime() throws Exception {
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT,
                new OSSPlainTextAKSKCredentialProvider(OSSTestConfig.AK, OSSTestConfig.SK));
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = oss.presignConstrainedObjectURL(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m", 1);
                    latch1.await();
                    Request request = new Request.Builder().url(url).build();
                    Response response = new OkHttpClient().newCall(request).execute();
                    assertEquals(403, response.code());
                    assertEquals("Forbidden", response.message());
                    latch2.countDown();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        latch2.await(20, TimeUnit.SECONDS);
        latch1.countDown();
        latch2.await();
        OSSLog.logD("testPresignObjectURLWithExpiredTime success.");
    }

    public void testTimeTooSkewedAndAutoFix() throws Exception {

        DateUtil.setCurrentServerTime(0);

        assertTrue(DateUtil.getFixedSkewedTimeMillis() < 1000);

        GetObjectRequest get = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);

        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        assertTrue(Math.abs(DateUtil.getFixedSkewedTimeMillis() - System.currentTimeMillis()) < 5 * 60 * 1000);
    }
}
