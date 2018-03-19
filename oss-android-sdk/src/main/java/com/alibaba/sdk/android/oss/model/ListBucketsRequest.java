package com.alibaba.sdk.android.oss.model;

/**
 * Created by chenjie on 17/12/6.
 */

public class ListBucketsRequest extends OSSRequest {

    private static final int MAX_RETURNED_KEYS_LIMIT = 1000;

    // prefix filter
    private String prefix;

    // maker filter--the returned bucket' keys must be greater than this value in lexicographic order.
    private String marker;

    // the max keys to return--by default it's 100
    private Integer maxKeys;

    public ListBucketsRequest() {
    }

    public ListBucketsRequest(String prefix) {
        this(prefix, null);
    }

    public ListBucketsRequest(String prefix, String marker) {
        this(prefix, marker, 100);
    }

    public ListBucketsRequest(String prefix, String marker, Integer maxKeys) {
        this.prefix = prefix;
        this.marker = marker;
        this.maxKeys = maxKeys;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getMarker() {
        return this.marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public Integer getMaxKeys() {
        return this.maxKeys;
    }

    public void setMaxKeys(Integer maxKeys) {
        this.maxKeys = maxKeys;
    }

}
