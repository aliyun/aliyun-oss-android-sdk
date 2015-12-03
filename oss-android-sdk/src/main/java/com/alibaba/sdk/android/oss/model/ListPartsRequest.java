package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/25/15.
 */
public class ListPartsRequest extends OSSRequest {

    private String bucketName;

    private String objectKey;

    private String uploadId;

    private Integer maxParts;

    private Integer partNumberMarker;

    /**
     * 构造函数。
     * @param bucketName
     *          Bucket名称。
     * @param objectKey
     *          Object objectKey。
     * @param uploadId
     *          Mutlipart上传事件的Upload ID。
     */
    public ListPartsRequest(String bucketName, String objectKey, String uploadId) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadId = uploadId;
    }

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
     * 返回OSSObject objectKey。
     * @return Object objectKey。
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置OSSObject objectKey。
     * @param objectKey
     *          Object objectKey。
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
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

    /**
     * 返回一个值表示最大返回多少条记录。（默认值1000）
     * @return 最大返回多少条记录。
     */
    public Integer getMaxParts() {
        return maxParts;
    }

    /**
     * 设置一个值最大返回多少条记录。（可选）
     * 最大值和默认值均为1000。
     * @param maxParts
     *          最大返回多少条记录。
     */
    public void setMaxParts(int maxParts) {
        this.maxParts = maxParts;
    }

    /**
     * 返回一个值表示从哪个Part号码开始获取列表。
     * @return 表示从哪个Part号码开始获取列表。
     */
    public Integer getPartNumberMarker() {
        return partNumberMarker;
    }

    /**
     * 设置一个值表示从哪个Part号码开始获取列表。
     * @param partNumberMarker
     *          表示从哪个Part号码开始获取列表。
     */
    public void setPartNumberMarker(Integer partNumberMarker) {
        this.partNumberMarker = partNumberMarker;
    }
}
