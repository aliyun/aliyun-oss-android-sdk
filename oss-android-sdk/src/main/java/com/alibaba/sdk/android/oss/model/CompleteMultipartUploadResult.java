package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class CompleteMultipartUploadResult extends OSSResult {

    /** The name of the bucket containing the completed multipart upload. */
    private String bucketName;

    /** The objectKey by which the object is stored. */
    private String objectKey;

    /** The URL identifying the new multipart object. */
    private String location;

    private String eTag;

    private String serverCallbackReturnBody;

    /**
     * 返回标识Multipart上传的OSSObject的URL地址。
     * @return 标识Multipart上传的OSSObject的URL地址。
     */
    public String getLocation() {
        return location;
    }

    /**
     * 设置标识Multipart上传的OSSObject的URL地址。
     * @param location
     *          标识Multipart上传的OSSObject的URL地址。
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * 返回包含Multipart上传的OSSObject的Bucket名称。
     * @return Bucket名称。
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 设置包含Multipart上传的OSSObject的Bucket名称。
     * @param bucketName
     *          Bucket名称。
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 返回新创建的OSSObject的Key。
     * @return 新创建的OSSObject的Key。
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * 设置新创建的OSSObject的Key。
     * @param objectKey
     *          新创建的OSSObject的Key。
     */
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * 返回ETag值。
     * @return ETag值。
     */
    public String getETag() {
        return eTag;
    }

    /**
     * 设置ETag值。
     * @param etag ETag值。
     */
    public void setETag(String etag) {
        this.eTag = etag;
    }

    /**
     * 如果设置了serverCallback，上传后OSS会返回回调结果
     * @return 回调结果的json串
     */
    public String getServerCallbackReturnBody() {
        return serverCallbackReturnBody;
    }

    /**
     * 设置serverCallback结果
     * @param serverCallbackReturnBody
     */
    public void setServerCallbackReturnBody(String serverCallbackReturnBody) {
        this.serverCallbackReturnBody = serverCallbackReturnBody;
    }
}
