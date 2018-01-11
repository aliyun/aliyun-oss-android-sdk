package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/25/15.
 */
public class ListPartsRequest extends OSSRequest {

    private String bucketName;

    private String objectKey;

    private String uploadId;

    private Integer maxParts;

    private Integer partNumberMarker;

    /**
     * Constructor
     *
     * @param bucketName bucket name
     * @param objectKey  Object objectKey。
     * @param uploadId   Mutlipart Upload ID。
     */
    public ListPartsRequest(String bucketName, String objectKey, String uploadId) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadId(uploadId);
    }

    /**
     * Gets bucket name
     *
     * @return bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets bucket name
     *
     * @param bucketName bucket name
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
     * Gets Multipart upload Id
     *
     * @return The Multipart upload Id
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * Sets the multipart upload Id
     *
     * @param uploadId The Multipart upload Id
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    /**
     * Gets the max parts to return (default is 1000)
     *
     * @return the max parts
     */
    public Integer getMaxParts() {
        return maxParts;
    }

    /**
     * Sets the max parts to return
     * Max and default is 1000.
     *
     * @param maxParts the max parts to return
     */
    public void setMaxParts(int maxParts) {
        this.maxParts = maxParts;
    }

    /**
     * Gets the part number marker filter
     *
     * @return The part number marker filter---it means the returned parts' part number must be greater than this value.
     */
    public Integer getPartNumberMarker() {
        return partNumberMarker;
    }

    /**
     * Sets the part number marker filter--it means the returned parts' part number must be greater than this value.
     *
     * @param partNumberMarker The part number marker filter
     */
    public void setPartNumberMarker(Integer partNumberMarker) {
        this.partNumberMarker = partNumberMarker;
    }
}
