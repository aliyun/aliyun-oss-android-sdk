package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;

import java.util.Map;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class GetObjectRequest extends OSSRequest {
    //  Object bucket's name
    private String bucketName;

    // Object Key
    private String objectKey;

    // Gets the range of the object to return (starting from 0 to the object length -1)
    private Range range;

    // image processing parameters
    private String xOssProcess;

    // progress callback run with not ui thread
    private OSSProgressCallback progressListener;

    // request headers
    private Map<String, String> requestHeaders;

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    /**
     * Creates the new request to get the specified object
     *
     * @param bucketName Bucket name
     * @param objectKey  Object key
     */
    public GetObjectRequest(String bucketName, String objectKey) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the bucket name
     *
     * @param bucketName Bucket name
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the object to download
     *
     * @param objectKey Object key
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

    public String getxOssProcess() {
        return xOssProcess;
    }

    public void setxOssProcess(String xOssProcess) {
        this.xOssProcess = xOssProcess;
    }

    public OSSProgressCallback getProgressListener() {
        return progressListener;
    }

    public void setProgressListener(OSSProgressCallback<GetObjectRequest> progressListener) {
        this.progressListener = progressListener;
    }
}
