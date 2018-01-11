package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class CompleteMultipartUploadResult extends OSSResult {

    /**
     * The name of the bucket containing the completed multipart upload.
     */
    private String bucketName;

    /**
     * The objectKey by which the object is stored.
     */
    private String objectKey;

    /**
     * The URL identifying the new multipart object.
     */
    private String location;

    private String eTag;

    private String serverCallbackReturnBody;

    /**
     * Gets the target object's location
     *
     * @return The target object's location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of target object
     *
     * @param location The location of target object
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the target object's bucket name
     *
     * @return bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the bucket name
     *
     * @param bucketName bucket name
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets the object key
     *
     * @return The target object's key
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the object key
     *
     * @param objectKey The target object's key
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Gets ETag value
     *
     * @return ETag value
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Sets ETag value
     *
     * @param etag ETag
     */
    public void setETag(String etag) {
        this.eTag = etag;
    }

    /**
     * If the serverCallback is set, the callback response is returned to client after the upload
     *
     * @return The callback response in json string
     */
    public String getServerCallbackReturnBody() {
        return serverCallbackReturnBody;
    }

    /**
     * Sets the servercallback result
     *
     * @param serverCallbackReturnBody
     */
    public void setServerCallbackReturnBody(String serverCallbackReturnBody) {
        this.serverCallbackReturnBody = serverCallbackReturnBody;
    }
}
