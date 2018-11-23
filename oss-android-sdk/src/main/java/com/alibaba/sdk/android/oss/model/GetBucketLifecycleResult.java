package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;

public class GetBucketLifecycleResult extends OSSResult {
    private ArrayList<BucketLifecycleRule> mLifecycleRules;

    public ArrayList<BucketLifecycleRule> getlifecycleRules() {
        return mLifecycleRules;
    }

    public void setLifecycleRules(ArrayList<BucketLifecycleRule> lifecycleRules) {
        this.mLifecycleRules = lifecycleRules;
    }

    public void addLifecycleRule(BucketLifecycleRule lifecycleRule) {
        if (mLifecycleRules == null) {
            mLifecycleRules = new ArrayList<BucketLifecycleRule>();
        }

        mLifecycleRules.add(lifecycleRule);
    }
}
