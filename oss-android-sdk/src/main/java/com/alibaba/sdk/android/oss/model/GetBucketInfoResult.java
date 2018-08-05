package com.alibaba.sdk.android.oss.model;

public class GetBucketInfoResult extends OSSResult {
    private OSSBucketSummary bucket;

    public OSSBucketSummary getBucket() {
        return this.bucket;
    }

    public void setBucket(OSSBucketSummary bucket) {
        this.bucket = bucket;
    }

    @Override
    public String toString() {
        return String.format("GetBucketInfoResult<%s>:\n bucket:%s", super.toString(), bucket.toString());
    }
}
