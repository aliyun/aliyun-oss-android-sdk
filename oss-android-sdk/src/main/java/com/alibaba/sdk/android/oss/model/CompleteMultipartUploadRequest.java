package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class CompleteMultipartUploadRequest extends OSSRequest {

    /**
     * The name of the bucket containing the multipart upload to complete
     */
    private String bucketName;

    /**
     * The objectKey of the multipart upload to complete
     */
    private String objectKey;

    /**
     * The ID of the multipart upload to complete
     */
    private String uploadId;

    /**
     * The list of part numbers and ETags to use when completing the multipart upload
     */
    private List<PartETag> partETags = new ArrayList<PartETag>();

    private Map<String, String> callbackParam;

    private Map<String, String> callbackVars;

    private ObjectMetadata metadata;

    /**
     * Constructor of CompleteMultipartUploadRequest
     *
     * @param bucketName bucket name
     * @param objectKey  Object objectKey.
     * @param uploadId   Mutlipart upload ID.
     * @param partETags  The list of PartETag instances
     */
    public CompleteMultipartUploadRequest(String bucketName, String objectKey, String uploadId, List<PartETag> partETags) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadId(uploadId);
        setPartETags(partETags);
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
     * Gets OSSObject.
     *
     * @return Object objectKey。
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets objectKey.
     *
     * @param objectKey Object objectKey。
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Gets the multipart upload Id
     *
     * @return the multipart upload Id
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * Sets the multipart upload Id
     *
     * @param uploadId the multipart upload Id
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    /**
     * Gets the list of PartETag instances
     *
     * @return The list of PartETag instances
     */
    public List<PartETag> getPartETags() {
        return partETags;
    }

    /**
     * Sets the list of PartETag instances
     *
     * @param partETags The list of PartETag instances
     */
    public void setPartETags(List<PartETag> partETags) {
        this.partETags = partETags;
    }

    public Map<String, String> getCallbackParam() {
        return callbackParam;
    }

    /**
     * Sets the servercallback parameters
     */
    public void setCallbackParam(Map<String, String> callbackParam) {
        this.callbackParam = callbackParam;
    }

    public Map<String, String> getCallbackVars() {
        return callbackVars;
    }

    /**
     * Sets the servercallback custom variables
     */
    public void setCallbackVars(Map<String, String> callbackVars) {
        this.callbackVars = callbackVars;
    }

    /**
     * Sets the medatadata
     */
    public ObjectMetadata getMetadata() {
        return metadata;
    }

    /**
     * Gets the metadata
     */
    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }
}
