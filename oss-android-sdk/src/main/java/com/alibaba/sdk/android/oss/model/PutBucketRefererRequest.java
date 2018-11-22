package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;

public class PutBucketRefererRequest extends OSSRequest {
    private String mBucketName;
    private boolean mAllowEmpty;
    private ArrayList<String> mReferers;

    public String getBucketName() {
        return mBucketName;
    }

    public void setBucketName(String bucketName) {
        this.mBucketName = bucketName;
    }

    public boolean isAllowEmpty() {
        return mAllowEmpty;
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.mAllowEmpty = allowEmpty;
    }

    public ArrayList<String> getReferers() {
        return mReferers;
    }

    public void setReferers(ArrayList<String> referers) {
        this.mReferers = referers;
    }
}
