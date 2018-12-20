package com.alibaba.sdk.android.oss.model;

public class GetBucketLoggingResult extends OSSResult {
    private String mTargetBucketName;
    private String mTargetPrefix;
    private boolean mLoggingEnabled = false;

    public String getTargetBucketName() {
        return mTargetBucketName;
    }

    public void setTargetBucketName(String targetBucketName) {
        this.mTargetBucketName = targetBucketName;
    }

    public String getTargetPrefix() {
        return mTargetPrefix;
    }

    public void setTargetPrefix(String targetPrefix) {
        this.mTargetPrefix = targetPrefix;
    }

    public boolean loggingEnabled() {
        return mLoggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.mLoggingEnabled = loggingEnabled;
    }
}
