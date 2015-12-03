package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class CompleteMultipartUploadRequest extends OSSRequest {

    /** The name of the bucket containing the multipart upload to complete */
    private String bucketName;

    /** The objectKey of the multipart upload to complete */
    private String objectKey;

    /** The ID of the multipart upload to complete */
    private String uploadId;

    /** The list of part numbers and ETags to use when completing the multipart upload */
    private List<PartETag> partETags = new ArrayList<PartETag>();

    /**
     * 构造函数。
     * @param bucketName
     *          Bucket名称。
     * @param objectKey
     *          Object objectKey。
     * @param uploadId
     *          Mutlipart上传事件的Upload ID。
     * @param partETags
     *          标识上传Part结果的{@link PartETag}列表。
     */
    public CompleteMultipartUploadRequest(String bucketName, String objectKey, String uploadId, List<PartETag> partETags) {
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.uploadId = uploadId;
        this.partETags = partETags;
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
     * 返回OSSObject。
     * @return Object objectKey。
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置objectKey。
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
     * 返回标识上传Part结果的{@link PartETag}列表。
     * @return 标识上传Part结果的{@link PartETag}列表。
     */
    public List<PartETag> getPartETags() {
        return partETags;
    }

    /**
     * 设置标识上传Part结果的{@link PartETag}列表。
     * @param partETags
     *          标识上传Part结果的{@link PartETag}列表。
     */
    public void setPartETags(List<PartETag> partETags) {
        this.partETags = partETags;
    }
}
