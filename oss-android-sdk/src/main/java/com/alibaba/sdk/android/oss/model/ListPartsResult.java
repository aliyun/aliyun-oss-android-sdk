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

    private Integer maxParts;

    private Integer partNumberMarker;

    private String storageClass;

    private boolean isTruncated;

    private Integer nextPartNumberMarker;

    private List<PartSummary> parts = new ArrayList<PartSummary>();

    /**
     * 返回Bucket名称。
     * @return Bucket名称。
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 设置Bucket名称。
     * @param bucketName
     *          Bucket名称。
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 返回OSSObject key。
     * @return Object key。
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置OSSObject key。
     * @param key
     *          Object key。
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 返回标识Multipart上传事件的Upload ID。
     * @return 标识Multipart上传事件的Upload ID。
     */
    public String getUploadId() {
        return uploadId;
    }

    /**
     * 设置标识Multipart上传事件的Upload ID。
     * @param uploadId
     *          标识Multipart上传事件的Upload ID。
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
     * 返回请求中给定的{@link ListPartsRequest#getPartNumberMarker()}。
     * @return Part number marker。
     */
    public Integer getPartNumberMarker() {
        return partNumberMarker;
    }

    /**
     * 设置请求中给定的{@link ListPartsRequest#getPartNumberMarker()}。
     * @param partNumberMarker
     *          Part number marker。
     */
    public void setPartNumberMarker(int partNumberMarker) {
        this.partNumberMarker = partNumberMarker;
    }

    /**
     * 返回一个值表示如果返回结果被截取，那么下一个Part的号码是多少。
     * @return 值表示如果返回结果被截取，那么下一个Part的号码是多少。
     */
    public Integer getNextPartNumberMarker() {
        return nextPartNumberMarker;
    }

    /**
     * 设置一个值值表示如果返回结果被截取，那么下一个Part的号码是多少。
     * @param nextPartNumberMarker
     *          值表示如果返回结果被截取，那么下一个Part的号码是多少。
     */
    public void setNextPartNumberMarker(int nextPartNumberMarker) {
        this.nextPartNumberMarker = nextPartNumberMarker;
    }

    /**
     * 返回请求中指定返回Part的最大个数（{@link ListPartsRequest#getMaxParts()}）。
     * @return 返回Part的最大个数。
     */
    public Integer getMaxParts() {
        return maxParts;
    }

    /**
     * 设置请求中指定返回Part的最大个数（{@link ListPartsRequest#getMaxParts()}）。
     * @param maxParts
     *          返回Part的最大个数。返回Part的最大个数。
     */
    public void setMaxParts(int maxParts) {
        this.maxParts = maxParts;
    }

    /**
     * 返回一个值表示返回结果是否被截取，即是否还有其他记录没有返回。
     * @return 返回结果是否被截取，即是否还有其他记录没有返回。
     */
    public boolean isTruncated() {
        return isTruncated;
    }

    /**
     * 设置一个值表示返回结果是否被截取，即是否还有其他记录没有返回。
     * @param isTruncated
     *          返回结果是否被截取，即是否还有其他记录没有返回。
     */
    public void setTruncated(boolean isTruncated) {
        this.isTruncated = isTruncated;
    }

    /**
     * 返回PartSummary的列表。
     * @return PartSummary的列表。
     */
    public List<PartSummary> getParts() {
        return parts;
    }

    /**
     * 设置PartSummary的列表。
     * @param parts
     *      PartSummary的列表。
     */
    public void setParts(List<PartSummary> parts) {
        this.parts.clear();
        if (parts != null && !parts.isEmpty()) {
            this.parts.addAll(parts);
        }
    }

    /**
     * 添加PartSummary实例。
     * @param partSummary
     *      PartSummary实例。
     */
    public void addPart(PartSummary partSummary) {
        this.parts.add(partSummary);
    }
}
