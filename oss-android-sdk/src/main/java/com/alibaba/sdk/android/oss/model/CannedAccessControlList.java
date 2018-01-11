package com.alibaba.sdk.android.oss.model;

/**
 * bucket ACL enum definition
 * Created by LK on 15/12/17.
 */
public enum CannedAccessControlList {

    Private("private"),

    PublicRead("public-read"),

    PublicReadWrite("public-read-write");

    private String ACLString;

    CannedAccessControlList(String acl) {
        this.ACLString = acl;
    }

    @Override
    public String toString() {
        return this.ACLString;
    }

    public static CannedAccessControlList parseACL(String aclStr) {
        CannedAccessControlList currentAcl = null;
        for (CannedAccessControlList acl : CannedAccessControlList.values()) {
            if (acl.toString().equals(aclStr)) {
                currentAcl = acl;
                break;
            }
        }
        return currentAcl;
    }
}
