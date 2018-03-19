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

    public Owner getOwner() {
        return bucketOwner;
    }

    /**
     * Gets the bucket owner
     *
     * @return
     */
    public String getBucketOwner() {
        return bucketOwner.getDisplayName();
    }

    /**
     * Sets the bucket owner
     *
     * @param ownerName
     */
    public void setBucketOwner(String ownerName) {
        this.bucketOwner.setDisplayName(ownerName);
    }

    /**
     * Gets bucket owner Id
     *
     * @return
     */
    public String getBucketOwnerID() {
        return bucketOwner.getId();
    }

    /**
     * Sets the bucket owner Id
     *
     * @param id
     */
    public void setBucketOwnerID(String id) {
        this.bucketOwner.setId(id);
    }

    /**
     * Gets bucket ACL
     *
     * @return
     */
    public String getBucketACL() {
        String acl = null;
        if (bucketACL != null) {
            acl = bucketACL.toString();
        }
        return acl;
    }

    /**
     * Sets bucket ACL
     *
     * @param bucketACL
     */
    public void setBucketACL(String bucketACL) {
        this.bucketACL = CannedAccessControlList.parseACL(bucketACL);
    }
}
