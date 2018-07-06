package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouzhuo on 11/25/15.
 */
public class ListPartsResult extends OSSResult {

    private String bucketName;

    private String key;

    private String uploadId;

    private int maxParts = 0;

    private int partNumberMarker = 0;

    private String storageClass;

    private boolean isTruncated = false;

    private int nextPartNumberMarker = 0;

    private List<PartSummary> parts = new ArrayList<PartSummary>();

    /**
     * Gets bucket name
     *
     * @return bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets bucket name
     *
     * @param bucketName bucket name
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets OSSObject key
     *
     * @return Object key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets OSSObject key
     *
     * @param key Object key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the Multipart Upload ID
     *
     * @return The Multipart Upload ID
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * Sets the Multipart Upload ID
     *
     * @param uploadId The Multipart Upload ID
     */
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    /**
     * Gets the part number marker --- it comes from {@link ListPartsRequest#getPartNumberMarker()}
     *
     * @return Part number marker.
     */
    public int getPartNumberMarker() {
        return partNumberMarker;
    }

    /**
     * Sets the part number marker---it comes from {@link ListPartsRequest#getPartNumberMarker()}。
     *
     * @param partNumberMarker Part number marker.
     */
    public void setPartNumberMarker(int partNumberMarker) {
        this.partNumberMarker = partNumberMarker;
    }

    /**
     * Gets the next part number marker
     *
     * @return the next part number marker
     */
    public int getNextPartNumberMarker() {
        return nextPartNumberMarker;
    }

    /**
     * Sets the next part number marker
     *
     * @param nextPartNumberMarker the next part number marker
     */
    public void setNextPartNumberMarker(int nextPartNumberMarker) {
        this.nextPartNumberMarker = nextPartNumberMarker;
    }

    /**
     * Gets the max parts count----it comes from ({@link ListPartsRequest#getMaxParts()})
     *
     * @return Max Part count
     */
    public int getMaxParts() {
        return maxParts;
    }

    /**
     * Sets the max parts count----it comes from ({@link ListPartsRequest#getMaxParts()}).
     *
     * @param maxParts Gets the max part count.
     */
    public void setMaxParts(int maxParts) {
        this.maxParts = maxParts;
    }

    /**
     * Gets the flag of truncation. If true, it means there's more data to return.
     *
     * @return The flag of truncation.
     */
    public boolean isTruncated() {
        return isTruncated;
    }

    /**
     * Sets the flag of truncation.If true, it means there's more data to return.
     *
     * @param isTruncated The flag of truncation.
     */
    public void setTruncated(boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    /**
     * Gets the list of PartSummary.
     *
     * @return The list of PartSummary
     */
    public List<PartSummary> getParts() {
        return parts;
    }

    /**
     * Sets the list of {@link PartSummary}.
     *
     * @param parts the list of {@link PartSummary}
     */
    public void setParts(List<PartSummary> parts) {
        this.parts.clear();
        if (parts != null && !parts.isEmpty()) {
            this.parts.addAll(parts);
        }
    }

}
