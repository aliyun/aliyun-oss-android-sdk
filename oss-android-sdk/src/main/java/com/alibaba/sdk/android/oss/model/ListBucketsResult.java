package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjie on 17/12/6.
 */

public class ListBucketsResult extends OSSResult {

    private String prefix;

    private String marker;

    private int maxKeys;

    private boolean isTruncated;

    private String nextMarker;

    private String ownerId;

    private String ownerDisplayName;

    private List<OSSBucketSummary> buckets = new ArrayList<OSSBucketSummary>();


    public void setPrefix (String prefix) { this.prefix = prefix; }

    public void setMarker (String marker) { this.marker = marker; }

    public void setMaxKeys (int maxKeys) { this.maxKeys = maxKeys; }

    public void setTruncated (boolean isTruncated) { this.isTruncated = isTruncated; }

    public void setNextMarker (String nextMarker) { this.nextMarker = nextMarker; }

    public void setOwnerId (String ownerId) { this.ownerId = ownerId; }

    public void setOwnerDisplayName (String ownerDisplayName) { this.ownerDisplayName = ownerDisplayName; }

    public void setBuckets (List<OSSBucketSummary> buckets) { this.buckets = buckets; }

    public void addBucket(OSSBucketSummary bucket) {
        this.buckets.add(bucket);
    }

    public String getPrefix () { return prefix; }

    public String getMarker () { return marker; }

    public int getMaxKeys() { return maxKeys; }

    public boolean getTruncated () { return isTruncated; }

    public String getNextMarker () { return nextMarker; }

    public String getOwnerId () { return ownerId; }

    public String getOwnerDisplayName () { return ownerDisplayName; }

    public List<OSSBucketSummary> getBuckets () { return buckets; }

    public void clearBucketList() {
        buckets.clear();
    }
}
