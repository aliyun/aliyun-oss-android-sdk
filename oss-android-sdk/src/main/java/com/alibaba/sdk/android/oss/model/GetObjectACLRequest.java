package com.alibaba.sdk.android.oss.model;

/**
 * Created by chenjie on 17/11/25.
 */

public class GetObjectACLRequest extends OSSRequest {

    private String bucketName;
    private String objectKey;

    /**
     * Creates the request to get the bucket ACL
     *
     * @param bucketName
     */
    public GetObjectACLRequest(String bucketName, String objectKey) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
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

    /**
     * Gets the object key
     *
     * @return
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the object key
     *
     * @param objectKey
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

}
