package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class GetObjectRequest extends OSSRequest {
    // Object所在的Bucket的名称
    private String bucketName;

    // Object Key
    private String objectKey;

    // 指定返回Object内容的字节范围。
    private Range range;

    // 处理图片的参数
    private String xOssProcess;

    /**
     * 构造新的Object下载请求
     * @param bucketName Bucket名字
     * @param objectKey Object名字
     */
    public GetObjectRequest(String bucketName, String objectKey) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
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
}
