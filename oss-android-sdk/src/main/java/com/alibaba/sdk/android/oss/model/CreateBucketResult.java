package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/15.
 */
public class CreateBucketResult extends OSSResult {
    // bucket's location
    private String bucketLocation;

    /**
     * Sets bucket location
     * @param location
     */
    public void setBucketLocation(String location) {
        this.bucketLocation = location;
    }

    /**
     * Gets the bucket location
     * @return
     */
    public String getBucketLocation() {
        return bucketLocation;
    }
}
