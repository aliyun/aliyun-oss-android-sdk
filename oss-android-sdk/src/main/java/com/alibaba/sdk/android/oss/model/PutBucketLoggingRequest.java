package com.alibaba.sdk.android.oss.model;

public class PutBucketLoggingRequest extends OSSRequest {
    private String mBucketName;
    private String mTargetBucketName;
    private String mTargetPrefix;

    public String getBucketName() {
        return mBucketName;
    }

    public void setBucketName(String bucketName) {
        this.mBucketName = bucketName;
    }

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
}
