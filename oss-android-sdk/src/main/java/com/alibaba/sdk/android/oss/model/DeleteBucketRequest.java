package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/15.
 */
public class DeleteBucketRequest extends OSSRequest {

    private String bucketName;

    /**
     * Creates the request to delete the specified bucket
     *
     * @param bucketName
     */
    public DeleteBucketRequest(String bucketName) {
        setBucketName(bucketName);
    }

    /**
     * Gets the bucket name to delete
     *
     * @return
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the bucket name to delete
     *
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

}
