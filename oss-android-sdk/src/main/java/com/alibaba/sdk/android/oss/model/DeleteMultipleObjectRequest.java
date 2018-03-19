package com.alibaba.sdk.android.oss.model;

import java.util.List;

/**
 * Created by chenjie on 17/11/28.
 */

public class DeleteMultipleObjectRequest extends OSSRequest {

    private String bucketName;
    private List<String> objectKeys;
    private boolean isQuiet;

    public DeleteMultipleObjectRequest(String bucketName, List<String> objectKeys, Boolean isQuiet) {
        setBucketName(bucketName);
        setObjectKeys(objectKeys);
        setQuiet(isQuiet);
    }

    public String getBucketName() {
        return bucketName;
    }

    /**
     * Sets the object's bucket name to delete.
     *
     * @param bucketName
     */
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public List<String> getObjectKeys() {
        return objectKeys;
    }

    /**
     * Sets the object keys to delete
     *
     * @param objectKeys
     */
    public void setObjectKeys(List<String> objectKeys) {
        this.objectKeys = objectKeys;
    }

    public Boolean getQuiet() {
        return isQuiet;
    }

    public void setQuiet(Boolean isQuiet) {
        this.isQuiet = isQuiet;
    }

}
