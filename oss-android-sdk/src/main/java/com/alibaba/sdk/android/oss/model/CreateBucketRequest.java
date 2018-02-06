package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/15.
 */
public class CreateBucketRequest extends OSSRequest {

    private String bucketName;
    private CannedAccessControlList bucketACL;
    private String locationConstraint;
    private StorageClass bucketStorageClass = StorageClass.Standard;

    public static final String TAB_LOCATIONCONSTRAINT = "LocationConstraint";
    public static final String TAB_STORAGECLASS = "StorageClass";

    /**
     * The constructor of CreateBucketRequest
     *
     * @param bucketName
     */
    public CreateBucketRequest(String bucketName) {
        setBucketName(bucketName);
    }

    /**
     * Sets the bucket name
     * bucketName is globally unique cross all OSS users in all regions. Otherwise returns 409.
     *
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /**
     * Gets the bucket name
     *
     * @return
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the location constraint.
     * Valid values：oss-cn-hangzhou、oss-cn-qingdao、oss-cn-beijing、oss-cn-hongkong、oss-cn-shenzhen、
     * oss-cn-shanghai、oss-us-west-1 、oss-ap-southeast-1
     * If it's not specified，the default value is oss-cn-hangzhou
     *
     * @param locationConstraint
     */
    public void setLocationConstraint(String locationConstraint) {
        this.locationConstraint = locationConstraint;
    }

    /**
     * Gets the bucket location's constraint.
     *
     * @return
     */
    public String getLocationConstraint() {
        return locationConstraint;
    }

    /**
     * Sets bucket ACL
     * For now there're three permissions of Bucket: private、public-read、public-read-write
     *
     * @param bucketACL
     */
    public void setBucketACL(CannedAccessControlList bucketACL) {
        this.bucketACL = bucketACL;
    }

    /**
     * Gets bucket ACL
     *
     * @return
     */
    public CannedAccessControlList getBucketACL() {
        return bucketACL;
    }

    /**
     * Set bucket storage class
     * @param storageClass
     */
    public void setBucketStorageClass(StorageClass storageClass) {
        this.bucketStorageClass = storageClass;
    }

    /**
     * Get bucket storage class
     * @return
     */
    public StorageClass getBucketStorageClass() { return bucketStorageClass; }
}
