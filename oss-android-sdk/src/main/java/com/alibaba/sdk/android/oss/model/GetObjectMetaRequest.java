package com.alibaba.sdk.android.oss.model;

public class GetObjectMetaRequest extends OSSRequest {

    private String bucketName;

    private String objectKey;

    public GetObjectMetaRequest(String bucketName, String objectKey) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
    }

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
}
