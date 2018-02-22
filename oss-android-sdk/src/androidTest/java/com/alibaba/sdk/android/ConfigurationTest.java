package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.sdk.android.oss.model.CannedAccessControlList.PublicReadWrite;

/**
 * Created by zhouzhuo on 12/6/15.
 */
public class ConfigurationTest extends AndroidTestCase {

    private OSS oss;

    private final static String BUCKET_NAME = "oss-android-config-test";
    private final static String PUBLIC_BUCKET_NAME = "oss-android-config-public-test";

    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
            try {
                CreateBucketRequest request = new CreateBucketRequest(BUCKET_NAME);
                oss.createBucket(request);
            } catch (Exception e) {
            }
            try {
                CreateBucketRequest request = new CreateBucketRequest(PUBLIC_BUCKET_NAME);
                request.setBucketACL(PublicReadWrite);
                oss.createBucket(request);
                OSSTestConfig.initLocalFile();

                PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                        OSSTestConfig.FILE_DIR + "file1m");
                oss.putObject(put);
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, BUCKET_NAME);
            OSSTestUtils.cleanBucket(oss, PUBLIC_BUCKET_NAME);
        } catch (Exception e) {
        }
    }

    public void testConfiguration() throws Exception{
        updateCredentialProviderTest();
        cnameSettingTest();
        customExcludeCnameTest();
        customExcludeCnameWithHttpTest();
        customExcludeCnameErrorTest();
        customUserAgentTest();
        httpDnsEnableTest();
        httpDnsEnableFalseTest();
    }

    public void updateCredentialProviderTest() throws Exception {

        oss.updateCredentialProvider(OSSTestConfig.credentialProvider);

        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "file1m");

        HeadObjectResult headResult = oss.headObject(head);

        assertNotNull(headResult.getMetadata().getContentType());
        assertEquals(1024 * 1000, headResult.getMetadata().getContentLength());

        //revert
        oss.updateCredentialProvider(OSSTestConfig.credentialProvider);
    }

    public void cnameSettingTest() throws Exception {

        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ANDROID_TEST_CNAME, OSSTestConfig.credentialProvider);

        String url = oss.presignConstrainedObjectURL(BUCKET_NAME, "file1m", 30 * 60);

        assertEquals(true, url.startsWith(OSSTestConfig.ANDROID_TEST_CNAME));
    }

    public void customExcludeCnameTest() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m", OSSTestConfig.FILE_DIR + "file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(BUCKET_NAME, "file1m", 30 * 60);
        OSSLog.logDebug("Presiged constraintdd url: " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(PUBLIC_BUCKET_NAME, "file1m");
        assertEquals("http://" + PUBLIC_BUCKET_NAME + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m", url);
    }

    public void customExcludeCnameWithHttpTest() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST_WITH_HTTP);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m", OSSTestConfig.FILE_DIR + "/file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(BUCKET_NAME, "file1m", 30 * 60);
        OSSLog.logDebug("Presiged constraintdd url: " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(PUBLIC_BUCKET_NAME, "file1m");
        assertEquals("http://" + PUBLIC_BUCKET_NAME + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m", url);
    }

    public void customExcludeCnameErrorTest() {
        try {
            List cnameExcludeList = new ArrayList();
            ClientConfiguration conf = new ClientConfiguration();
            conf.setCustomCnameExcludeList(cnameExcludeList);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void customUserAgentTest() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setUserAgentMark("customUserAgent");
        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    public void httpDnsEnableTest() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(true);
        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    public void httpDnsEnableFalseTest() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(false);
        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }
}
