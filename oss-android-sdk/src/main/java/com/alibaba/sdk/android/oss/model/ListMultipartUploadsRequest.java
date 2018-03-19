package com.alibaba.sdk.android.oss.model;

/**
 * Created by jingdan on 2018/2/13.
 */

public class ListMultipartUploadsRequest extends OSSRequest {

    private String bucketName;

    private String delimiter;

    private String prefix;

    private Integer maxUploads;

    private String keyMarker;

    private String uploadIdMarker;

    private String encodingType;

    /**
     * Constructor.
     *
     * @param bucketName Bucket name.
     */
    public ListMultipartUploadsRequest(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Gets the max number of uploads to return.
     *
     * @return The max number of uploads.
     */
    public Integer getMaxUploads() {
        return maxUploads;
    }

    /**
     * Sets the max number of uploads to return. The both max and default value
     * is 1000。
     *
     * @param maxUploads The max number of uploads.
     */
    public void setMaxUploads(Integer maxUploads) {
        this.maxUploads = maxUploads;
    }

    /**
     * Gets the key marker filter---all uploads returned whose target file's key
     * must be greater than the marker filter.
     *
     * @return The key marker filter.
     */
    public String getKeyMarker() {
        return keyMarker;
    }

    /**
     * Sets the key marker filter---all uploads returned whose target file's key
     * must be greater than the marker filter.
     *
     * @param keyMarker The key marker.
     */
    public void setKeyMarker(String keyMarker) {
        this.keyMarker = keyMarker;
    }

    /**
     * Gets the upload id marker--all uploads returned whose upload id must be
     * greater than the marker filter.
     *
     * @return The upload Id marker.
     */
    public String getUploadIdMarker() {
        return uploadIdMarker;
    }

    /**
     * Sets the upload id marker--all uploads returned whose upload id must be
     * greater than the marker filter.
     *
     * @param uploadIdMarker The upload Id marker.
     */
    public void setUploadIdMarker(String uploadIdMarker) {
        this.uploadIdMarker = uploadIdMarker;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the encoding type of the object in the response body.
     *
     * @return The encoding type of the object in the response body.
     */
    public String getEncodingType() {
        return encodingType;
    }

    /**
     * Sets the encoding type of the object in the response body.
     *
     * @param encodingType The encoding type of the object in the response body. Valid
     *                     value is either 'null' or 'url'.
     */
    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

}
