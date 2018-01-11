package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/23/15.
 */

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.callback.OSSRetryCallback;

import java.util.Map;

/**
 * The request class definition of uploading an object either from local file or in-memory data
 */
public class PutObjectRequest extends OSSRequest {

    private String bucketName;
    private String objectKey;

    private String uploadFilePath;

    private byte[] uploadData;

    private ObjectMetadata metadata;

    private Map<String, String> callbackParam;

    private Map<String, String> callbackVars;

    //run with not ui thread
    private OSSProgressCallback<PutObjectRequest> progressCallback;

    //run with not ui thread
    private OSSRetryCallback retryCallback;

    /**
     * Constructor
     *
     * @param bucketName     The bucket name
     * @param objectKey      The object key
     * @param uploadFilePath The local file path to upload from
     */
    public PutObjectRequest(String bucketName, String objectKey, String uploadFilePath) {
        this(bucketName, objectKey, uploadFilePath, null);
    }

    /**
     * Constructor
     *
     * @param bucketName     The bucket name
     * @param objectKey      The object key
     * @param uploadFilePath The local file path
     * @param metadata       The metadata information of the target object
     */
    public PutObjectRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadFilePath(uploadFilePath);
        setMetadata(metadata);
    }

    /**
     * Constructor
     *
     * @param bucketName The bucket name
     * @param objectKey  The object key
     * @param uploadData The in-memory data to upload
     */
    public PutObjectRequest(String bucketName, String objectKey, byte[] uploadData) {
        this(bucketName, objectKey, uploadData, null);
    }

    /**
     * Constructor
     *
     * @param bucketName The bucket name
     * @param objectKey  The object key
     * @param uploadData The in-memory data to upload
     * @param metadata   The metadata information of the target object
     */
    public PutObjectRequest(String bucketName, String objectKey, byte[] uploadData, ObjectMetadata metadata) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadData(uploadData);
        setMetadata(metadata);
    }

    /**
     * Gets the bucket name
     *
     * @return The bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the bucket name
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets the object key
     *
     * @return The object key
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the object key
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    /**
     * Sets the local upload file path
     *
     * @param uploadFilePath The local upload file path
     */
    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public byte[] getUploadData() {
        return uploadData;
    }

    /**
     * Sets the upload data
     *
     * @param uploadData
     */
    public void setUploadData(byte[] uploadData) {
        this.uploadData = uploadData;
    }

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the target object
     *
     * @param metadata the target object metadata
     */
    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    public OSSProgressCallback<PutObjectRequest> getProgressCallback() {
        return progressCallback;
    }

    /**
     * Sets the upload progress callback
     *
     * @param progressCallback
     */
    public void setProgressCallback(OSSProgressCallback<PutObjectRequest> progressCallback) {
        this.progressCallback = progressCallback;
    }

    public OSSRetryCallback getRetryCallback() {
        return retryCallback;
    }

    /**
     * Sets the upload retry request callback
     *
     * @param retryCallback
     */
    public void setRetryCallback(OSSRetryCallback retryCallback) {
        this.retryCallback = retryCallback;
    }

    public Map<String, String> getCallbackParam() {
        return callbackParam;
    }

    /**
     * Sets the callback parameters
     */
    public void setCallbackParam(Map<String, String> callbackParam) {
        this.callbackParam = callbackParam;
    }

    public Map<String, String> getCallbackVars() {
        return callbackVars;
    }

    /**
     * Sets the callback variables
     */
    public void setCallbackVars(Map<String, String> callbackVars) {
        this.callbackVars = callbackVars;
    }
}
