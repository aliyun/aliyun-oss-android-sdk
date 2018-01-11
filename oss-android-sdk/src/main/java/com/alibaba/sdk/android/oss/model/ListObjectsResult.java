package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class ListObjectsResult extends OSSResult {

    /**
     * A list of summary information describing the objects stored in the bucket
     */
    private List<OSSObjectSummary> objectSummaries = new ArrayList<OSSObjectSummary>();

    private List<String> commonPrefixes = new ArrayList<String>();

    private String bucketName;

    private String nextMarker;

    private boolean isTruncated;

    private String prefix;

    private String marker;

    private int maxKeys;

    private String delimiter;

    private String encodingType;

    public List<OSSObjectSummary> getObjectSummaries() {
        return objectSummaries;
    }

    public void addObjectSummary(OSSObjectSummary objectSummary) {
        this.objectSummaries.add(objectSummary);
    }

    public void clearObjectSummaries() {
        this.objectSummaries.clear();
    }

    public List<String> getCommonPrefixes() {
        return commonPrefixes;
    }

    public void addCommonPrefix(String commonPrefix) {
        this.commonPrefixes.add(commonPrefix);
    }

    public void clearCommonPrefixes() {
        this.commonPrefixes.clear();
    }

    public String getNextMarker() {
        return nextMarker;
    }

    public void setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public int getMaxKeys() {
        return maxKeys;
    }

    public void setMaxKeys(int maxKeys) {
        this.maxKeys = maxKeys;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getEncodingType() {
        return encodingType;
    }

    public void setEncodingType(String encodingType) {
        this.encodingType = encodingType;
    }

    public boolean isTruncated() {
        return isTruncated;
    }

    public void setTruncated(boolean isTruncated) {
        this.isTruncated = isTruncated;
    }
}
