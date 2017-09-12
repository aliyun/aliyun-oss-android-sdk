package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;

import java.io.File;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/27/15.
 *
 * The resumable upload request class definition
 *
 * Resumable upload is implemented by the OSS multipart upload with local checkpoint information.
 * When the network condition in mobile device is poor, resumable upload is the best to use.
 * It will retry the failed parts as long as you retry with the same parameters (the upload file path,
 * target object and the part size) and the checkpoint information is stored.
 *
 */
public class ResumableUploadRequest extends OSSRequest {
    private String bucketName;
    private String objectKey;

    private Boolean deleteUploadOnCancelling = true;

    private String uploadFilePath;
    private String recordDirectory;
    private long partSize = 256 * 1024;

    private ObjectMetadata metadata;

    private Map<String, String> callbackParam;
    private Map<String, String> callbackVars;

    private OSSProgressCallback<ResumableUploadRequest> progressCallback;

    /**
     * Constructor
     * @param bucketName The target object's bucket name
     * @param objectKey The target object's key
     * @param uploadFilePath The local path of the file to upload
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath) {
        this(bucketName,objectKey,uploadFilePath,null,null);
    }

    /**
     * Constructor
     * @param bucketName The target object's bucket name
     * @param objectKey The target object's key
     * @param uploadFilePath The local path of the file to upload
     * @param metadata The metadata of the target object
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
        this(bucketName,objectKey,uploadFilePath,metadata,null);
    }

    /**
     * Constructor
     * @param bucketName The target object's bucket name
     * @param objectKey The target object's key
     * @param uploadFilePath The local path of the file to upload
     * @param recordDirectory The checkpoint files' directory. Here it needs to be the absolute local path.
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, String recordDirectory) {
        this(bucketName,objectKey,uploadFilePath,null,recordDirectory);
    }

    /**
     * Constructor
     * @param bucketName The target object's bucket name
     * @param objectKey The target object's key
     * @param uploadFilePath The local path of the file to upload
     * @param metadata The metadata of the target object
     * @param recordDirectory The checkpoint files' directory. Here it needs to be the absolute local path.
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata, String recordDirectory) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
        setUploadFilePath(uploadFilePath);
        setMetadata(metadata);
        setRecordDirectory(recordDirectory);
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the OSS bucket name
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
     * @param uploadFilePath the local path of the file to upload
     */
    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public String getRecordDirectory() {
        return recordDirectory;
    }

    /**
     * Sets the checkpoint files' directory (the directory must exist and is absolute directory path)
     * @param recordDirectory the checkpoint files' directory
     */
    public void setRecordDirectory(String recordDirectory) {
        if(recordDirectory!=null) {
            File file = new File(recordDirectory);
            if (!file.exists() || !file.isDirectory()) {
                throw new IllegalArgumentException("Record directory must exist, and it should be a directory!");
            }
        }
        this.recordDirectory = recordDirectory;
    }

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the target object
     * @param metadata The metadata
     */
    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    public OSSProgressCallback<ResumableUploadRequest> getProgressCallback() {
        return progressCallback;
    }

    /**
     * Sets the upload progress callback
     */
    public void setProgressCallback(OSSProgressCallback<ResumableUploadRequest> progressCallback) {
        this.progressCallback = progressCallback;
    }

    public long getPartSize() {
        return partSize;
    }

    /**
     * Sets the part size, by default it's 256KB and the minimal value is 100KB
     * @param partSize size in byte
     */
    public void setPartSize(long partSize) throws IllegalArgumentException{
        if (partSize < OSSConstants.MIN_PART_SIZE_LIMIT) {
            throw new IllegalArgumentException("Part size must be greater than or equal to 100KB!");
        }
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

    public Boolean deleteUploadOnCancelling() {
        return deleteUploadOnCancelling;
    }

    public void setDeleteUploadOnCancelling(Boolean deleteUploadOnCancelling) {
        this.deleteUploadOnCancelling = deleteUploadOnCancelling;
    }
}
