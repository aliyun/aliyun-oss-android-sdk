package com.alibaba.sdk.android;

import android.support.test.InstrumentationRegistry;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.TriggerCallbackRequest;
import com.alibaba.sdk.android.oss.model.TriggerCallbackResult;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by huaixu on 2018/1/30.
 */

public class TriggerCallbackTest extends BaseTestCase {
    private String mObjectKey = "objectKey";
    private String mEndpoint = "https://oss-cn-hangzhou.aliyuncs.com";
    private Map<String, String> mParams;
    private Map<String, String> mVars;

    @Override
    void initTestData() throws Exception {
        mParams = new HashMap<String, String>();
        mParams.put("callbackUrl", OSSTestConfig.CALLBACK_SERVER);
        mParams.put("callbackBody", "test");

        mVars = new HashMap<String, String>();
        mVars.put("key1", "value1");
        mVars.put("key2", "value2");
        OSSTestConfig.initLocalFile();
        PutObjectRequest file1k = new PutObjectRequest(mBucketName,
                mObjectKey, OSSTestConfig.FILE_DIR + "file1k");
        oss.putObject(file1k);
    }

    @Override
    protected void initOSSClient() {
        oss = new OSSClient(InstrumentationRegistry.getTargetContext(), mEndpoint, OSSTestConfig.credentialProvider);
    }

    @Test
    public void testTriggerCallback() throws Exception {
        TriggerCallbackRequest request = new TriggerCallbackRequest(mBucketName, mObjectKey, mParams, mVars);

        OSSAsyncTask task = oss.asyncTriggerCallback(request, new OSSCompletedCallback<TriggerCallbackRequest, TriggerCallbackResult>() {
            @Override
            public void onSuccess(TriggerCallbackRequest request, TriggerCallbackResult result) {
                assertNotNull(result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(TriggerCallbackRequest request, ClientException clientException, ServiceException serviceException) {
                assertNull(clientException);
                assertNull(serviceException);
            }
        });
        task.waitUntilFinished();
        assertNotNull(task.getResult());
    }


    @Test
    public void testTriggerZipCallback() throws Exception {
        HashMap params = new HashMap<String, String>();
        params.put("callbackUrl", OSSTestConfig.CALLBACK_SERVER);
        params.put("callbackBody", "test");

        TriggerCallbackRequest request = new TriggerCallbackRequest(mBucketName, mObjectKey, params, mVars);

        OSSAsyncTask task = oss.asyncTriggerCallback(request, new OSSCompletedCallback<TriggerCallbackRequest, TriggerCallbackResult>() {
            @Override
            public void onSuccess(TriggerCallbackRequest request, TriggerCallbackResult result) {
                assertNotNull(result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(TriggerCallbackRequest request, ClientException clientException, ServiceException serviceException) {
                assertNull(clientException);
                assertNull(serviceException);
            }
        });
        task.waitUntilFinished();
        assertNotNull(task.getResult());
    }

    @Test
    public void testTriggerCallbackWithoutVars() throws Exception {
        TriggerCallbackRequest request = new TriggerCallbackRequest(mBucketName, mObjectKey, mParams, null);

        OSSAsyncTask task = oss.asyncTriggerCallback(request, new OSSCompletedCallback<TriggerCallbackRequest, TriggerCallbackResult>() {
            @Override
            public void onSuccess(TriggerCallbackRequest request, TriggerCallbackResult result) {
                assertNotNull(result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(TriggerCallbackRequest request, ClientException clientException, ServiceException serviceException) {
                assertNull(clientException);
                assertNull(serviceException);
            }
        });
        task.waitUntilFinished();
        assertNotNull(task.getResult());
    }

    @Test
    public void testTriggerCallbackWithoutParams() throws Exception {
        TriggerCallbackRequest request = new TriggerCallbackRequest(mBucketName, mObjectKey, null, mVars);

        Exception serverException = null;
        try {
            TriggerCallbackResult triggerCallbackResult = oss.triggerCallback(request);
        } catch (Exception e) {
            serverException = e;
        }

        assertNotNull(serverException);
    }
}
