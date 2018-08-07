package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;

import java.util.Map;

public class MultipartUploadRequest<T extends MultipartUploadRequest> extends OSSRequest {
    protected String bucketName;
    protected String objectKey;
    protected String uploadId;

    protected String uploadFilePath;
    protected long partSize = 256 * 1024;

    protected ObjectMetadata metadata;

    protected Map<String, String> callbackParam;
    protected Map<String, String> callbackVars;

    protected OSSProgressCallback<T> progressCallback;

    /**
     * Constructor
     *
     * @param bucketName     The target object's bucket name
     * @param objectKey      The target object's key
     * @param uploadFilePath The local path of the file to upload
     */
    public MultipartUploadRequest(String bucketName, String objectKey, String uploadFilePath) {
        this(bucketName, objectKey, uploadFilePath, null);
    }

    /**
     * Constructor
     *
     * @param bucketName     The target object's bucket name
     * @param objectKey      The target object's key
     * @param uploadFilePath The local path of the file to upload
     * @param metadata       The metadata of the target object
     */
    public MultipartUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadFilePath(uploadFilePath);
        setMetadata(metadata);
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the OSS bucket name
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
     * Sets the OSS object key
     *
     * @param objectKey
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    /**
     * Sets the local path of the file to upload
     *
     * @param uploadFilePath the local path of the file to upload
     */
    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the target object
     *
     * @param metadata The metadata
     */
    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    public OSSProgressCallback<T> getProgressCallback() {
        return progressCallback;
    }

    /**
     * Sets the upload progress callback
     */
    public void setProgressCallback(OSSProgressCallback<T> progressCallback) {
        this.progressCallback = progressCallback;
    }

    public long getPartSize() {
        return partSize;
    }

    /**
     * Sets the part size, by default it's 256KB and the minimal value is 100KB
     *
     * @param partSize size in byte
     */
    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public Map<String, String> getCallbackParam() {
        return callbackParam;
    }

    /**
     * Sets the server callback parameters
     */
    public void setCallbackParam(Map<String, String> callbackParam) {
        this.callbackParam = callbackParam;
    }

    public Map<String, String> getCallbackVars() {
        return callbackVars;
    }

    /**
     * Sets the server callback variables
     */
    public void setCallbackVars(Map<String, String> callbackVars) {
        this.callbackVars = callbackVars;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}
