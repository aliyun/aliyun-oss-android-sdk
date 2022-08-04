package com.alibaba.sdk.android.oss.model;

public class DeleteObjectTaggingRequest extends OSSRequest {
    private String bucketName;
    private String objectKey;

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public DeleteObjectTaggingRequest(String bucketName, String objectKey) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
    }
}
