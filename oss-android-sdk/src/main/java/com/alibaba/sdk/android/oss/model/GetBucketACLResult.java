package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/18.
 */
public class GetBucketACLResult extends OSSResult {

    // bucket拥有者
    private Owner bucketOwner;

    // bucket的ACL权限
    private CannedAccessControlList bucketACL;

    public GetBucketACLResult() {
        bucketOwner = new Owner();
    }

    /**
     * 设置bucket拥有者名称
     * @param ownerName
     */
    public void setBucketOwner(String ownerName) {
        this.bucketOwner.setDisplayName(ownerName);
    }

    /**
     * 返回bucket拥有者名称
     * @return
     */
    public String getBucketOwner() {
        return bucketOwner.getDisplayName();
    }

    /**
     * 设置bucket拥有者ID
     * @param id
     */
    public void setBucketOwnerID(String id) {
        this.bucketOwner.setId(id);
    }

    /**
     * 返回bucket拥有者ID
     * @return
     */
    public String getBucketOwnerID() {
        return bucketOwner.getId();
    }

    /**
     * 设置bucket ACL权限
     * @param bucketACL
     */
    public void setBucketACL(String bucketACL) {
        this.bucketACL = CannedAccessControlList.parseACL(bucketACL);
    }

    /**
     * 返回bucket ACL权限
     * @return
     */
    public String getBucketACL() {
        return bucketACL.toString();
    }
}
