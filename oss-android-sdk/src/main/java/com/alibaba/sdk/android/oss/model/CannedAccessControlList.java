package com.alibaba.sdk.android.oss.model;

/** bucket ACL枚举类型
 * Created by LK on 15/12/17.
 */
public enum CannedAccessControlList {

    Private("private"),

    PublicRead("public-read"),

    PublicReadWrite("public-read-write");

    private String ACLString;

    private CannedAccessControlList(String acl) { this.ACLString = acl; }

    @Override
    public String toString() { return this.ACLString; }

    public static CannedAccessControlList parseACL(String aclStr) {
        for (CannedAccessControlList acl : CannedAccessControlList.values()) {
            if (acl.toString().equals(aclStr)) {
                return acl;
            }
        }
        throw new IllegalArgumentException("Unable to parse the provided acl " + aclStr);
    }
}
