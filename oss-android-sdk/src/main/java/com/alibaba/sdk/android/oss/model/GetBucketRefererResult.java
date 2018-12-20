package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;

public class GetBucketRefererResult extends OSSResult {
    private String mAllowEmpty;
    private ArrayList<String> mReferers;

    public String getAllowEmpty() {
        return mAllowEmpty;
    }

    public void setAllowEmpty(String allowEmpty) {
        this.mAllowEmpty = allowEmpty;
    }

    public ArrayList<String> getReferers() {
        return mReferers;
    }

    public void setReferers(ArrayList<String> referers) {
        this.mReferers = referers;
    }

    public void addReferer(String object) {
        if (mReferers == null) {
            mReferers = new ArrayList<String>();
        }
        mReferers.add(object);
    }
}
