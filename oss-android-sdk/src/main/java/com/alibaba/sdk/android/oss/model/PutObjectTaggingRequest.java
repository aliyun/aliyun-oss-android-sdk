package com.alibaba.sdk.android.oss.model;

import java.util.Map;

public class PutObjectTaggingRequest extends OSSRequest {
    private String bucketName;
    private String objectKey;
    private Map<String, String> tags;

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

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public PutObjectTaggingRequest(String bucketName, String objectKey, Map<String, String> tags) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.tags = tags;
    }
}
