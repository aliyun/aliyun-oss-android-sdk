package com.alibaba.sdk.android.oss.model;

/**
 * Created by LK on 15/12/18.
 */
public class GetBucketACLResult extends OSSResult {

    // bucket owner
    private Owner bucketOwner;

    // bucket's ACL
    private CannedAccessControlList bucketACL;

    public GetBucketACLResult() {
        bucketOwner = new Owner();
    }

    /**
     * Sets the bucket owner
     * @param ownerName
     */
    public void setBucketOwner(String ownerName) {
        this.bucketOwner.setDisplayName(ownerName);
    }

    /**
     * Gets the bucket owner
     * @return
     */
    public String getBucketOwner() {
        return bucketOwner.getDisplayName();
    }

    /**
     * Sets the bucket owner Id
     * @param id
     */
    public void setBucketOwnerID(String id) {
        this.bucketOwner.setId(id);
    }

    /**
     * Gets bucket owner Id
     * @return
     */
    public String getBucketOwnerID() {
        return bucketOwner.getId();
    }

    /**
     * Sets bucket ACL
     * @param bucketACL
     */
    public void setBucketACL(String bucketACL) {
        this.bucketACL = CannedAccessControlList.parseACL(bucketACL);
    }

    /**
     * Gets bucket ACL
     * @return
     */
    public String getBucketACL() {
        return bucketACL.toString();
    }
}
