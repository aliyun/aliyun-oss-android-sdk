package com.alibaba.sdk.android.oss.model;

/**
 * Created by chenjie on 17/11/25.
 */

public class GetObjectACLResult extends OSSResult {

    // object owner
    private Owner objectOwner;

    // object's ACL
    private CannedAccessControlList objectACL;

    public GetObjectACLResult() {
        objectOwner = new Owner();
    }

    public Owner getOwner() {
        return objectOwner;
    }

    /**
     * Gets the object owner
     *
     * @return
     */
    public String getObjectOwner() {
        return objectOwner.getDisplayName();
    }

    /**
     * Sets the object owner
     *
     * @param ownerName
     */
    public void setObjectOwner(String ownerName) {
        this.objectOwner.setDisplayName(ownerName);
    }

    /**
     * Gets object owner Id
     *
     * @return
     */
    public String getObjectOwnerID() {
        return objectOwner.getId();
    }

    /**
     * Sets the object owner Id
     *
     * @param id
     */
    public void setObjectOwnerID(String id) {
        this.objectOwner.setId(id);
    }

    /**
     * Gets object ACL
     *
     * @return
     */
    public String getObjectACL() {
        String acl = null;
        if (objectACL != null) {
            acl = objectACL.toString();
        }
        return acl;
    }

    /**
     * Sets object ACL
     *
     * @param objectACL
     */
    public void setObjectACL(String objectACL) {
        this.objectACL = CannedAccessControlList.parseACL(objectACL);
    }

}
