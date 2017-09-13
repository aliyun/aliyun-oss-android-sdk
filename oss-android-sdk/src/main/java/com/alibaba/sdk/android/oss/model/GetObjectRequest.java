package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class GetObjectRequest extends OSSRequest {
    // Object Located bucketName
    private String bucketName;

    // Object Key
    private String objectKey;

    // Object range
    private Range range;

    // process img params
    private String xOssProcess;

    // progress callback run with not ui thread
    private OSSProgressCallback progressListener;

    /**
     * 构造新的Object下载请求
     * @param bucketName Bucket名字
     * @param objectKey Object名字
     */
    public GetObjectRequest(String bucketName, String objectKey) {
        setBucketName(bucketName);
        setObjectKey(objectKey);
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * 设置需要下载的Object所在的Bucket
     * @param bucketName Bucket名字
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置需要下载的Object的名字
     * @param objectKey Object名
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public Range getRange() {
        return range;
    }

    /**
     * 范围下载
     * @param range 指定下载范围
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

    public void setProgressListener(OSSProgressCallback progressListener) {
        this.progressListener = progressListener;
    }
}
