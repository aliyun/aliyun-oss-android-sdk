package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.ClientConfiguration;
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
public class ConfigurationTest extends BaseTestCase {

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

    public void testUpdateCredentialProvider() throws Exception {

        oss.updateCredentialProvider(OSSTestConfig.credentialProvider);

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "file1m");

        HeadObjectResult headResult = oss.headObject(head);

        assertNotNull(headResult.getMetadata().getContentType());
        assertEquals(1024 * 1000, headResult.getMetadata().getContentLength());

        //revert
        oss.updateCredentialProvider(OSSTestConfig.credentialProvider);
    }

    public void testCnameSetting() throws Exception {

        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ANDROID_TEST_CNAME, OSSTestConfig.credentialProvider);

        String url = oss.presignConstrainedObjectURL(mBucketName, "file1m", 30 * 60);

        assertEquals(true, url.startsWith(OSSTestConfig.ANDROID_TEST_CNAME));
    }

    public void testCustomExcludeCname() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

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

    public void testCustomExcludeCnameWithHttp() throws Exception {

        List cnameExcludeList = new ArrayList();
        cnameExcludeList.add(OSSTestConfig.EXCLUDE_HOST_WITH_HTTP);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCustomCnameExcludeList(cnameExcludeList);

        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);

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

    public void testCustomExcludeCnameError() {
        try {
            List cnameExcludeList = new ArrayList();
            ClientConfiguration conf = new ClientConfiguration();
            conf.setCustomCnameExcludeList(cnameExcludeList);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testCustomUserAgent() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setUserAgentMark("customUserAgent");
        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    public void testHttpDnsEnable() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(true);
        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }

    public void testHttpDnsEnableFalse() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(false);
        OSSClient oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        GetObjectRequest get = new GetObjectRequest(mBucketName, "file1m");
        GetObjectResult getResult = oss.getObject(get);
        assertEquals(200, getResult.getStatusCode());
    }
}
