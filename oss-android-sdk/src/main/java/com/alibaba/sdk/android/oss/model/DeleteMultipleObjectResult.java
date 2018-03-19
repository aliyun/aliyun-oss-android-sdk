package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjie on 17/11/28.
 */

public class DeleteMultipleObjectResult extends OSSResult {

    private List<String> deletedObjects;
    private List<String> failedObjects;
    private boolean isQuiet;

    public void clear() {
        if (deletedObjects != null) {
            deletedObjects.clear();
        }
        if (failedObjects != null) {
            failedObjects.clear();
        }
    }

    public void addDeletedObject(String object) {
        if (deletedObjects == null) {
            deletedObjects = new ArrayList<String>();
        }
        deletedObjects.add(object);
    }

    public void addFailedObjects(String object) {
        if (failedObjects == null) {
            failedObjects = new ArrayList<String>();
        }
        failedObjects.add(object);
    }

    public List<String> getDeletedObjects() {
        return deletedObjects;
    }

    public List<String> getFailedObjects() {
        return failedObjects;
    }

    public boolean getQuiet() {
        return isQuiet;
    }

    public void setQuiet(boolean isQuiet) {
        this.isQuiet = isQuiet;
    }

}
