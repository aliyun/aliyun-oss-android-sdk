package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhouzhuo on 12/6/15.
 */
public class ConfigurationTest extends AndroidTestCase {

    private OSS oss;

    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
    }

    public void testUpdateCredentialProvider() throws Exception {

        oss.updateCredentialProvider(OSSTestConfig.plainTextAKSKcredentialProvider);

        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");

        HeadObjectResult headResult = oss.headObject(head);

        assertNotNull(headResult.getMetadata().getContentType());
        assertEquals(1024 * 1000, headResult.getMetadata().getContentLength());

        //revert
        oss.updateCredentialProvider(OSSTestConfig.credentialProvider);
    }

    public void testCnameSetting() throws Exception {

        oss = new OSSClient(getContext(), OSSTestConfig.ANDROID_TEST_CNAME, OSSTestConfig.credentialProvider);

        GetObjectRequest get = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m", OSSTestConfig.FILE_DIR + "/file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m", 30 * 60);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(OSSTestConfig.PUBLIC_READ_BUCKET, "file1m");
        request = new Request.Builder().url(url).build();
        response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());
    }

    public void testCustomExcludeCname() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

        GetObjectRequest get = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m", OSSTestConfig.FILE_DIR + "/file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m", 30 * 60);
        OSSLog.logDebug("Presiged constraintdd url: " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(OSSTestConfig.PUBLIC_READ_BUCKET, "file1m");
        assertEquals("http://" + OSSTestConfig.PUBLIC_READ_BUCKET + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m", url);
    }

    public void testCustomExcludeCnameWithHttp() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST_WITH_HTTP);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

        GetObjectRequest get = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());

        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m", OSSTestConfig.FILE_DIR + "/file1m");
        PutObjectResult putResult = oss.putObject(put);
        assertEquals(200, putResult.getStatusCode());

        String url = oss.presignConstrainedObjectURL(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m", 30 * 60);
        OSSLog.logDebug("Presiged constraintdd url: " + url);
        Request request = new Request.Builder().url(url).build();
        Response response = new OkHttpClient().newCall(request).execute();
        assertEquals(200, response.code());

        url = oss.presignPublicObjectURL(OSSTestConfig.PUBLIC_READ_BUCKET, "file1m");
        assertEquals("http://" + OSSTestConfig.PUBLIC_READ_BUCKET + "." + OSSTestConfig.EXCLUDE_HOST + "/file1m", url);
    }

    public void testCustomExcludeCnameError() {
        try {
            List cnameExcludeList = new ArrayList();
            ClientConfiguration conf = new ClientConfiguration();
            conf.setCustomCnameExcludeList(cnameExcludeList);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testProxySettings() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setProxyHost(OSSTestConfig.PROXY);//当前自己的机器地址
        conf.setProxyPort(OSSTestConfig.PROXY_PORT);
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    public void testCustomUserAgent() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setUserAgentMark("customUserAgent");
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    public void testHttpDnsEnable() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(true);
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    public void testHttpDnsEnableFalse() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(false);
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }
}
