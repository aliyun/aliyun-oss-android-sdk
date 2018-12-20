package com.alibaba.sdk.android.oss.model;

public class GetBucketLifecycleRequest extends OSSRequest {
    private String mBucketName;

    public String getBucketName() {
        return mBucketName;
    }

    public void setBucketName(String bucketName) {
        this.mBucketName = bucketName;
    }
}
