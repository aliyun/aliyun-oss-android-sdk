package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class AbortMultipartUploadRequest extends OSSRequest {

    /**
     * The name of the bucket containing the multipart upload to abort
     */
    private String bucketName;

    /**
     * The objectKey of the multipart upload to abort
     */
    private String objectKey;

    /**
     * The ID of the multipart upload to abort
     */
    private String uploadId;

    /**
     * The constructor of AbortMultipartUploadRequest
     *
     * @param bucketName Bucket name
     * @param objectKey  Object object key
     * @param uploadId   Upload id of a Multipart upload
     */
    public AbortMultipartUploadRequest(String bucketName, String objectKey, String uploadId) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadId(uploadId);
    }

    /**
     * Gets Bucket name
     *
     * @return Bucket name
     */
    public String getBucketName() {
        return this.bucketName;
    }

    /**
     * Sets bucket name
     *
     * @param bucketName Bucket name
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets OSSObject objectKey。
     *
     * @return Object objectKey。
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets OSSObject objectKey。
     *
     * @param objectKey Object objectKey。
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Gets the upload Id of a Multipart upload
     *
     * @return The upload Id of a multipart upload
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * Sets the upload Id of a Multipart upload
     *
     * @param uploadId The upload Id of a multipart upload
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
