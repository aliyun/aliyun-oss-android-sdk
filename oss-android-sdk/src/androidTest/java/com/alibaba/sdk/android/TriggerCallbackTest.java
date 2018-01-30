package com.alibaba.sdk.android;

import android.test.AndroidTestCase;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.TriggerCallbackRequest;
import com.alibaba.sdk.android.oss.model.TriggerCallbackResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huaixu on 2018/1/30.
 */

public class TriggerCallbackTest extends AndroidTestCase {
    private String mObjectKey = "objectKey";
    private String mBucketName = "bucketName";
    private String mEndpoint = "end-point";
    private Map<String, String> mParams;
    private Map<String, String> mVars;
    private String mAK = "AK";
    private String mSK = "SK";
    private OSSClient mClient;

    private OSS oss;

    @Override
    protected void setUp() throws Exception {

        mParams = new HashMap<String, String>();
        mParams.put("callbackUrl", "121.43.113.8:23450");
        mParams.put("callbackBody", "test");

        mVars = new HashMap<String, String>();
        mVars.put("key1", "value1");
        mVars.put("key2", "value2");

        OSSPlainTextAKSKCredentialProvider provider = new OSSPlainTextAKSKCredentialProvider(mAK, mSK);
        mClient = new OSSClient(getContext(), mEndpoint, provider);

    }

    public void testTriggerCallback() throws Exception {
        TriggerCallbackRequest request = new TriggerCallbackRequest(mBucketName, mObjectKey, mParams, mVars);

        OSSAsyncTask task = mClient.asyncTriggerCallback(request, new OSSCompletedCallback<TriggerCallbackRequest, TriggerCallbackResult>() {
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

    public void testTriggerCallbackWithoutVars() throws Exception {
        TriggerCallbackRequest request = new TriggerCallbackRequest(mBucketName, mObjectKey, mParams, null);

        OSSAsyncTask task = mClient.asyncTriggerCallback(request, new OSSCompletedCallback<TriggerCallbackRequest, TriggerCallbackResult>() {
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

    public void testTriggerCallbackWithoutParams() throws Exception {
        TriggerCallbackRequest request = new TriggerCallbackRequest(mBucketName, mObjectKey, null, mVars);

        OSSAsyncTask task = mClient.asyncTriggerCallback(request, new OSSCompletedCallback<TriggerCallbackRequest, TriggerCallbackResult>() {
            @Override
            public void onSuccess(TriggerCallbackRequest request, TriggerCallbackResult result) {
                assertNull(result.getServerCallbackReturnBody());
            }

            @Override
            public void onFailure(TriggerCallbackRequest request, ClientException clientException, ServiceException serviceException) {
                assertNotNull(serviceException);
            }
        });
        task.waitUntilFinished();
        assertNotNull(task.getResult());
    }
}
