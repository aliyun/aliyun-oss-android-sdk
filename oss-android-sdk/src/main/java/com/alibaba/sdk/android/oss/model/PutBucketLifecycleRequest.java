package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;

public class PutBucketLifecycleRequest extends OSSRequest {
    private String mBucketName;
    ArrayList<BucketLifecycleRule> lifecycleRules;

    public String getBucketName() {
        return mBucketName;
    }

    public void setBucketName(String bucketName) {
        this.mBucketName = bucketName;
    }

    public ArrayList<BucketLifecycleRule> getLifecycleRules() {
        return lifecycleRules;
    }

    public void setLifecycleRules(ArrayList<BucketLifecycleRule> lifecycleRules) {
        this.lifecycleRules = lifecycleRules;
    }
}
