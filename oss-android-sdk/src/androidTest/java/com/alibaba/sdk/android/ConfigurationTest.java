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
                    OSSTestConfig.EXTERNAL_FILE_DIR + "file1m");
            oss.putObject(put);
        } catch (Exception e) {
            e.printStackTrace();
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
        ClientConfiguration configuration = new ClientConfiguration();

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ANDROID_TEST_CNAME, OSSTestConfig.credentialProvider, configuration);

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        String canonicalUrl = OSSTestConfig.ANDROID_TEST_CNAME + "/file1m";
        assertEquals(true, url.startsWith(canonicalUrl));
    }

    @Test
    public void testCnameWithSetting() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add(OSSTestConfig.EXCLUDE_HOST);
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setCustomCnameExcludeList(list);

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.EXCLUDE_HOST, OSSTestConfig.credentialProvider, configuration);

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        String canonicalUrl = "http://" + mBucketName + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m";
        assertEquals(true, url.startsWith(canonicalUrl));
    }

    @Test
    public void testPathStyleSetting() throws Exception {
        List<String> cnameExcludeList = new ArrayList<String>();
        cnameExcludeList.add(OSSTestConfig.ANDROID_TEST_CNAME);

        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setPathStyleAccessEnable(true);
        configuration.setCustomCnameExcludeList(cnameExcludeList);

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ANDROID_TEST_CNAME, OSSTestConfig.credentialProvider, configuration);

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        String canonicalUrl = OSSTestConfig.ANDROID_TEST_CNAME + "/" + mBucketName + "/file1m";

        assertEquals(true, url.startsWith(canonicalUrl));
    }

    @Test
    public void testCustomPathSetting() throws Exception {
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setCustomPathPrefixEnable(true);

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ANDROID_TEST_CNAME + "/customPath", OSSTestConfig.credentialProvider, configuration);

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        String canonicalUrl = "http://" + OSSTestConfig.ANDROID_TEST_CNAME.replace("http://", "") + "/customPath/file1m";

        assertEquals(true, url.startsWith(canonicalUrl));

        List<String> list = new ArrayList<String>();
        list.add(OSSTestConfig.ANDROID_TEST_CNAME);

        configuration = new ClientConfiguration();
        configuration.setCustomPathPrefixEnable(true);
        configuration.setPathStyleAccessEnable(true);
        configuration.setCustomCnameExcludeList(list);

        oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ANDROID_TEST_CNAME + "/customPath", OSSTestConfig.credentialProvider, configuration);

        url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        canonicalUrl = OSSTestConfig.ANDROID_TEST_CNAME + "/customPath/" + mBucketName + "/file1m";

        assertEquals(true, url.startsWith(canonicalUrl));
    }

    @Test
    public void testIPSetting() throws Exception {
        ClientConfiguration configuration = new ClientConfiguration();

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), IP_ENDPOINT, OSSTestConfig.credentialProvider, configuration);

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        String canonicalUrl = "http://" + IP_ENDPOINT + "/" + mBucketName + "/file1m";

        assertEquals(true, url.startsWith(canonicalUrl));
    }

    @Test
    public void testCustomExcludeCname() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m", OSSTestConfig.EXTERNAL_FILE_DIR + "file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        OSSLog.logDebug("Presiged constraintdd url: " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(mPublicBucketName, "file1m");
        assertEquals("https://" + mPublicBucketName + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m", url);
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

        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m", OSSTestConfig.EXTERNAL_FILE_DIR + "/file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);
        OSSLog.logDebug("Presiged constraintdd url: " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(mPublicBucketName, "file1m");
        assertEquals("https://" + mPublicBucketName + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m", url);
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
        List<String> cnameExcludeList = new ArrayList<String>();
        cnameExcludeList.add(ENDPOINT);
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();

        ClientConfiguration conf = new ClientConfiguration();
        conf.setPathStyleAccessEnable(true);
        conf.setCustomCnameExcludeList(cnameExcludeList);
        OSSClient oss = new OSSClient(InstrumentationRegistry.getTargetContext(), SCHEME + ENDPOINT, OSSTestConfig.credentialProvider, conf);

        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();

        assertEquals(BUCKET_NAME + "." + ENDPOINT, getCallback.serviceException.getHostId());
        assertTrue(getCallback.serviceException.getRawMessage().contains("<BucketName>"+ BUCKET_NAME + "</BucketName>"));
    }

    @Test
    public void testSupportCnameEnable() throws Exception {
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        List<String> cnameExcludeList = new ArrayList<String>();
        cnameExcludeList.add(ENDPOINT);

        ClientConfiguration conf = new ClientConfiguration();
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
        conf.setCustomCnameExcludeList(cnameExcludeList);
        oss = new OSSClient(InstrumentationRegistry.getTargetContext(), SCHEME + ENDPOINT, OSSTestConfig.credentialProvider, conf);
        get = new GetObjectRequest(BUCKET_NAME, "file1m");
        task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();

        assertEquals(BUCKET_NAME + "." + ENDPOINT, getCallback.serviceException.getHostId());
        assertTrue(getCallback.serviceException.getRawMessage().contains("<BucketName>" + BUCKET_NAME +"</BucketName>"));
    }

    @Test
    public void testSupportCnameEnableWithNullCnameExcludeList() throws Exception {
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        IllegalArgumentException exception = null;
        List<String> cnameExcludeList = new ArrayList<String>();

        ClientConfiguration conf = new ClientConfiguration();
        try {
            conf.setCustomCnameExcludeList(cnameExcludeList);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("cname exclude list should not be null."));
    }


}
