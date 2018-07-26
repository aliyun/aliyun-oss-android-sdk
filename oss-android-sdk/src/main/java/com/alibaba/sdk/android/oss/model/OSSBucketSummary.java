package com.alibaba.sdk.android.oss.model;

import java.util.Date;

/**
 * Created by chenjie on 17/12/6.
 */

public class OSSBucketSummary {

    // Bucket name
    public String name;

    // Bucket owner
    public Owner owner;

    // Created date.
    public Date createDate;

    // Bucket location
    public String location;

    // External endpoint.It could be accessed from anywhere.
    public String extranetEndpoint;

    // Internal endpoint. It could be accessed within AliCloud under the same
    // location.
    public String intranetEndpoint;

    // Storage class (Standard, IA, Archive)
    public String storageClass;

    private CannedAccessControlList acl;

    /**
     * Gets bucket ACL
     *
     * @return
     */
    public String getAcl() {
        String bucketAcl = null;
        if (acl != null) {
            bucketAcl = acl.toString();
        }
        return bucketAcl;
    }

    /**
     * Sets bucket ACL
     *
     * @param aclString
     */
    public void setAcl(String aclString) {
        this.acl = CannedAccessControlList.parseACL(aclString);
    }

    @Override
    public String toString() {
        if (storageClass == null) {
            return "OSSBucket [name=" + name + ", creationDate=" + createDate + ", owner=" + owner.toString()
                    + ", location=" + location + "]";
        } else {
            return "OSSBucket [name=" + name + ", creationDate=" + createDate + ", owner=" + owner.toString()
                    + ", location=" + location + ", storageClass=" + storageClass + "]";
        }
    }
}
