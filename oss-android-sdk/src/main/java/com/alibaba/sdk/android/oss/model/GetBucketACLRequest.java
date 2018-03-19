package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/18.
 */
public class GetBucketACLRequest extends OSSRequest {

    private String bucketName;

    /**
     * Creates the request to get the bucket ACL
     *
     * @param bucketName
     */
    public GetBucketACLRequest(String bucketName) {
        setBucketName(bucketName);
    }

    /**
     * Gets the bucket name
     *
     * @return
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the bucket name
     *
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
