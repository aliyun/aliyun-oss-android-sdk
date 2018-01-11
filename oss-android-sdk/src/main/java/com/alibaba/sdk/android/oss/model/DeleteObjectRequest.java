package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class DeleteObjectRequest extends OSSRequest {

    private String bucketName;

    private String objectKey;

    public DeleteObjectRequest(String bucketName, String objectKey) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the object's bucket name to delete.
     *
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the object key to delete
     *
     * @param objectKey
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
}
