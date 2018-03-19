package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.common.utils.OSSUtils;

import java.io.File;

/**
 * Created by zhouzhuo on 11/27/15.
 * <p>
 * The resumable upload request class definition
 * <p>
 * Resumable upload is implemented by the OSS multipart upload with local checkpoint information.
 * When the network condition in mobile device is poor, resumable upload is the best to use.
 * It will retry the failed parts as long as you retry with the same parameters (the upload file path,
 * target object and the part size) and the checkpoint information is stored.
 */
public class ResumableUploadRequest extends MultipartUploadRequest {

    private Boolean deleteUploadOnCancelling = true;
    private String recordDirectory;

    /**
     * Constructor
     *
     * @param bucketName     The target object's bucket name
     * @param objectKey      The target object's key
     * @param uploadFilePath The local path of the file to upload
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath) {
        this(bucketName, objectKey, uploadFilePath, null, null);
    }

    /**
     * Constructor
     *
     * @param bucketName     The target object's bucket name
     * @param objectKey      The target object's key
     * @param uploadFilePath The local path of the file to upload
     * @param metadata       The metadata of the target object
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata) {
        this(bucketName, objectKey, uploadFilePath, metadata, null);
    }

    /**
     * Constructor
     *
     * @param bucketName      The target object's bucket name
     * @param objectKey       The target object's key
     * @param uploadFilePath  The local path of the file to upload
     * @param recordDirectory The checkpoint files' directory. Here it needs to be the absolute local path.
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, String recordDirectory) {
        this(bucketName, objectKey, uploadFilePath, null, recordDirectory);
    }

    /**
     * Constructor
     *
     * @param bucketName      The target object's bucket name
     * @param objectKey       The target object's key
     * @param uploadFilePath  The local path of the file to upload
     * @param metadata        The metadata of the target object
     * @param recordDirectory The checkpoint files' directory. Here it needs to be the absolute local path.
     */
    public ResumableUploadRequest(String bucketName, String objectKey, String uploadFilePath, ObjectMetadata metadata, String recordDirectory) {
        super(bucketName, objectKey, uploadFilePath, metadata);
        setRecordDirectory(recordDirectory);
    }


    public String getRecordDirectory() {
        return recordDirectory;
    }

    /**
     * Sets the checkpoint files' directory (the directory must exist and is absolute directory path)
     *
     * @param recordDirectory the checkpoint files' directory
     */
    public void setRecordDirectory(String recordDirectory) {
        if (!OSSUtils.isEmptyString(recordDirectory)) {
            File file = new File(recordDirectory);
            if (!file.exists() || !file.isDirectory()) {
                throw new IllegalArgumentException("Record directory must exist, and it should be a directory!");
            }
        }
        this.recordDirectory = recordDirectory;
    }


    public Boolean deleteUploadOnCancelling() {
        return deleteUploadOnCancelling;
    }

    public void setDeleteUploadOnCancelling(Boolean deleteUploadOnCancelling) {
        this.deleteUploadOnCancelling = deleteUploadOnCancelling;
    }
}
