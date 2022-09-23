package com.alibaba.sdk.android.oss.model;

public class GetObjectMetaResult extends OSSResult {
    // object metadata
    private ObjectMetadata metadata = new ObjectMetadata();

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }
}
