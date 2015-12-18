package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/15.
 */
public class CreateBucketResult extends OSSResult {
    // bucket所在数据中心
    private String bucketLocation;

    /**
     * 设置bucket所在数据中心
     * @param location
     */
    public void setBucketLocation(String location) {
        this.bucketLocation = location;
    }

    /**
     * 返回bucket所在数据中心
     * @return
     */
    public String getBucketLocation() {
        return bucketLocation;
    }
}
