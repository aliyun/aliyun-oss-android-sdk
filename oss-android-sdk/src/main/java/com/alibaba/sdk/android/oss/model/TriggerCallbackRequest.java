package com.alibaba.sdk.android.oss.model;

import java.util.Map;

/**
 * Created by huaixu on 2018/1/29.
 */

public class TriggerCallbackRequest extends OSSRequest {

    private String mBucketName;

    private String mObjectKey;

    private Map<String, String> mCallbackParam;

    private Map<String, String> mCallbackVars;

    public TriggerCallbackRequest(String bucketName, String objectKey, Map<String, String> callbackParam, Map<String, String> callbackVars) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setCallbackParam(callbackParam);
        setCallbackVars(callbackVars);
    }

    public void setBucketName(String bucketName) {
        this.mBucketName = bucketName;
    }

    public void setObjectKey(String objectKey) {
        this.mObjectKey = objectKey;
    }

    public void setCallbackParam(Map<String, String> callbackParam) {
        this.mCallbackParam = callbackParam;
    }

    public void setCallbackVars(Map<String, String> callbackVars) {
        this.mCallbackVars = callbackVars;
    }

    public String getBucketName() {
        return mBucketName;
    }

    public String getObjectKey() {
        return mObjectKey;
    }

    public Map<String, String> getCallbackParam() {
        return mCallbackParam;
    }

    public Map<String, String> getCallbackVars() {
        return mCallbackVars;
    }

}
