package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/15.
 */
public class CreateBucketRequest extends OSSRequest {

    private String bucketName;
    private CannedAccessControlList bucketACL;
    private String locationConstraint;


    /**
     * 构造bucket创建请求
     * @param bucketName
     */
    public CreateBucketRequest(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 设置要创建的bucketName
     * bucketName在全局是唯一的，与其他用户设置的bucektName也不能重名，否则会返回409 Conflict错误
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * 返回要创建的bucketName
     * @return
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * 设置bucket所在的数据中心
     * 合法值：oss-cn-hangzhou、oss-cn-qingdao、oss-cn-beijing、oss-cn-hongkong、oss-cn-shenzhen、
     *        oss-cn-shanghai、oss-us-west-1 、oss-ap-southeast-1
     * 如果不指定，默认值为：oss-cn-hangzhou
     * @param locationConstraint
     */
    public void setLocationConstraint(String locationConstraint) {
        this.locationConstraint = locationConstraint;
    }

    /**
     * 返回bucket所在数据中心
     * @return
     */
    public String getLocationConstraint() {
        return locationConstraint;
    }

    /**
     * 设置bucket ACL
     * 目前Bucket有三种访问权限：private、public-read、public-read-write
     * @param bucketACL
     */
    public void setBucketACL(CannedAccessControlList bucketACL) {
        this.bucketACL = bucketACL;
    }

    /**
     * 返回bucket ACL
     * @return
     */
    public CannedAccessControlList getBucketACL() {
        return bucketACL;
    }

}
