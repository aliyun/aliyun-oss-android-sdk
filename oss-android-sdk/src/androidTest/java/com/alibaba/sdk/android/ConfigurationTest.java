package com.alibaba.sdk.android;

import android.support.test.InstrumentationRegistry;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.junit.Assert.*;

/**
 * Created by zhouzhuo on 12/6/15.
 */
public class ConfigurationTest extends BaseTestCase {

    private static String SCHEME = "https://";
    private static String ENDPOINT = "oss-cn-hangzhou.aliyuncs.com";
    private static String CNAME_ENDPOINT = "oss.custom.com";
    private static String IP_ENDPOINT = "192.169.1.1:8080";
    private static String PATH = "/path";
    private static String PATH_ENDPOINT = ENDPOINT + "/path";

    private static String BUCKET_NAME = "invalid-bucket";

    @Override
    void initTestData() {
        OSSTestConfig.initLocalFile();
        try {
            PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m",
                    OSSTestConfig.FILE_DIR + "file1m");
            oss.putObject(put);
        } catch (Exception e) {
        }
    }

    @Test
    public void testUpdateCredentialProvider() throws Exception {

        oss.updateCredentialProvider(OSSTestConfig.credentialProvider);

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "file1m");

        HeadObjectResult headResult = oss.headObject(head);

        assertNotNull(headResult.getMetadata().getContentType());
        assertEquals(1024 * 1000, headResult.getMetadata().getContentLength());

        //revert
        oss.updateCredentialProvider(OSSTestConfig.credentialProvider);
    }

    @Test
    public void testCnameSetting() throws Exception {

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ANDROID_TEST_CNAME, OSSTestConfig.credentialProvider);

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);

        assertEquals(true, url.startsWith(OSSTestConfig.ANDROID_TEST_CNAME));
    }

    @Test
    public void testCustomExcludeCname() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m", OSSTestConfig.FILE_DIR + "file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        OSSLog.logDebug("Presiged constraintdd url: " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(mPublicBucketName, "file1m");
        assertEquals("http://" + mPublicBucketName + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m", url);
    }

    @Test
    public void testCustomExcludeCnameWithHttp() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST_WITH_HTTP);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m", OSSTestConfig.FILE_DIR + "/file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        OSSLog.logDebug("Presiged constraintdd url: " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(mPublicBucketName, "file1m");
        assertEquals("http://" + mPublicBucketName + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m", url);
    }

    @Test
    public void testCustomExcludeCnameError() {
        try {
            List cnameExcludeList = new ArrayList();
            ClientConfiguration conf = new ClientConfiguration();
            conf.setCustomCnameExcludeList(cnameExcludeList);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCustomUserAgent() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setUserAgentMark("customUserAgent");
        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    @Test
    public void testHttpDnsEnable() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(true);
        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    @Test
    public void testHttpDnsEnableFalse() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(false);
        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    @Test
    public void testDefault() throws Exception {
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        ClientConfiguration conf = new ClientConfiguration();
        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), SCHEME + ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();
        assertEquals(BUCKET_NAME + "." + ENDPOINT, getCallback.serviceException.getHostId());
        assertTrue(getCallback.serviceException.getRawMessage().contains("<BucketName>"+ BUCKET_NAME + "</BucketName>"));
    }

    @Test
    public void testPathStyleAccessEnable() throws Exception {
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        ClientConfiguration conf = new ClientConfiguration();
        conf.setPathStyleAccessEnable(true);
        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), SCHEME + ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();
        assertEquals(ENDPOINT, getCallback.serviceException.getHostId());
        assertTrue(getCallback.serviceException.getRawMessage().contains("<BucketName>"+ BUCKET_NAME + "</BucketName>"));
    }

    @Test
    public void testSupportCnameEnable() throws Exception {
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        List<String> cnameExcludeList = new ArrayList<String>();
        cnameExcludeList.add(ENDPOINT);

        ClientConfiguration conf = new ClientConfiguration();
        conf.setSupportCnameEnable(true);
        conf.setCustomCnameExcludeList(cnameExcludeList);
        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), SCHEME + ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();

        assertEquals(BUCKET_NAME + "." + ENDPOINT, getCallback.serviceException.getHostId());
        assertTrue(getCallback.serviceException.getRawMessage().contains("<BucketName>"+ BUCKET_NAME + "</BucketName>"));

        getCallback = new OSSTestConfig.TestGetCallback();
        cnameExcludeList = new ArrayList<String>();
        cnameExcludeList.add(CNAME_ENDPOINT);

        conf = new ClientConfiguration();
        conf.setSupportCnameEnable(true);
        conf.setCustomCnameExcludeList(cnameExcludeList);
        oss = new OSSClient(InstrumentationRegistry.getTargetContext(), SCHEME + ENDPOINT, OSSTestConfig.credentialProvider, conf);
        get = new GetObjectRequest(BUCKET_NAME, "file1m");
        task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();

        assertEquals(ENDPOINT, getCallback.serviceException.getHostId());
        assertTrue(getCallback.serviceException.getRawMessage().contains("<BucketName>file1m</BucketName>"));
    }

    @Test
    public void testSupportCnameEnableWithNullCnameExcludeList() throws Exception {
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        IllegalArgumentException exception = null;
        List<String> cnameExcludeList = new ArrayList<String>();

        ClientConfiguration conf = new ClientConfiguration();
        conf.setSupportCnameEnable(true);
        try {
            conf.setCustomCnameExcludeList(cnameExcludeList);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("cname exclude list should not be null."));
    }

    @Test
    public void testCustomPathPrefixEnable() throws Exception {

        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomPathPrefixEnable(true);
        conf.setSupportCnameEnable(true);
        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), SCHEME + ENDPOINT + "/" + BUCKET_NAME, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();
        assertEquals(ENDPOINT, getCallback.serviceException.getHostId());
        assertTrue(getCallback.serviceException.getRawMessage().contains("<BucketName>" + BUCKET_NAME +"</BucketName>"));
    }

}
