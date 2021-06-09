package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;

import java.util.Map;

public class ResumableDownloadRequest extends OSSRequest {

    //  Object bucket's name
    private String bucketName;

    // Object Key
    private String objectKey;

    // Gets the range of the object to return (starting from 0 to the object length -1)
    private Range range;

    // progress callback run with not ui thread
    private OSSProgressCallback progressListener;

    //
    private String downloadToFilePath;

    private Boolean enableCheckPoint = false;
    private String checkPointFilePath;

    private long partSize = 256 * 1024;

    private Map<String, String> requestHeader;

    /**
     * Constructor
     *
     * @param bucketName     The target object's bucket name
     * @param objectKey      The target object's key
     * @param downloadToFilePath The local path of the file to download
     */
    public ResumableDownloadRequest(String bucketName, String objectKey, String downloadToFilePath) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.downloadToFilePath = downloadToFilePath;
    }

    /**
     * Constructor
     *
     * @param bucketName     The target object's bucket name
     * @param objectKey      The target object's key
     * @param downloadToFilePath The local path of the file to download
     * @param checkPointFilePath The checkpoint files' directory
     */
    public ResumableDownloadRequest(String bucketName, String objectKey, String downloadToFilePath, String checkPointFilePath) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.downloadToFilePath = downloadToFilePath;
        this.enableCheckPoint = true;
        this.checkPointFilePath = checkPointFilePath;
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

    public Range getRange() {
        return range;
    }

    /**
     * Sets the range to download
     *
     * @param range The range to download (starting from 0 to the length -1)
     */
    public void setRange(Range range) {
        this.range = range;
    }

    public OSSProgressCallback getProgressListener() {
        return progressListener;
    }

    /**
     * Sets the upload progress callback
     */
    public void setProgressListener(OSSProgressCallback progressListener) {
        this.progressListener = progressListener;
    }

    public String getDownloadToFilePath() {
        return downloadToFilePath;
    }

    /**
     * Sets the local path of the file to download
     *
     * @param downloadToFilePath the local path of the file to upload
     */
    public void setDownloadToFilePath(String downloadToFilePath) {
        this.downloadToFilePath = downloadToFilePath;
    }

    public Boolean getEnableCheckPoint() {
        return enableCheckPoint;
    }


    public void setEnableCheckPoint(Boolean enableCheckPoint) {
        this.enableCheckPoint = enableCheckPoint;
    }

    public String getCheckPointFilePath() {
        return checkPointFilePath;
    }

    /**
     * Sets the checkpoint files' directory (the directory must exist and is absolute directory path)
     *
     * @param checkPointFilePath the checkpoint files' directory
     */
    public void setCheckPointFilePath(String checkPointFilePath) {
        this.checkPointFilePath = checkPointFilePath;
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

    public String getTempFilePath() {
        return downloadToFilePath + ".tmp";
    }

    public Map<String, String> getRequestHeader() {
        return requestHeader;
    }

    /**
     * Sets the request headers
     *
     * @param requestHeader
     */
    public void setRequestHeader(Map<String, String> requestHeader) {
        this.requestHeader = requestHeader;
    }
}
