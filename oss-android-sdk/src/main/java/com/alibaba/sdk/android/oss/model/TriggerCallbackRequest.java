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

    public String getBucketName() {
        return mBucketName;
    }

    public void setBucketName(String bucketName) {
        this.mBucketName = bucketName;
    }

    public String getObjectKey() {
        return mObjectKey;
    }

    public void setObjectKey(String objectKey) {
        this.mObjectKey = objectKey;
    }

    public Map<String, String> getCallbackParam() {
        return mCallbackParam;
    }

    public void setCallbackParam(Map<String, String> callbackParam) {
        this.mCallbackParam = callbackParam;
    }

    public Map<String, String> getCallbackVars() {
        return mCallbackVars;
    }

    public void setCallbackVars(Map<String, String> callbackVars) {
        this.mCallbackVars = callbackVars;
    }

}
