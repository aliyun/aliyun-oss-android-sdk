package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/15.
 */
public class DeleteBucketRequest extends OSSRequest {

    private String bucketName;

    /**
     * 构造bucket删除请求
     * @param bucketName
     */
    public DeleteBucketRequest(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 设置要删除的bucketName
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 返回要删除的bucketName
     * @return
     */
    public String getBucketName() {
        return bucketName;
    }

}
