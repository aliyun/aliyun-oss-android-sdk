package com.alibaba.sdk.android.mns.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class PagingListResult<T> implements Serializable {
    private String marker;
    private List<T> result = new ArrayList<T>();

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

}
