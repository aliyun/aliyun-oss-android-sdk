package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class HeadObjectResult extends OSSResult {

    // object metadata
    private ObjectMetadata metadata = new ObjectMetadata();

    public ObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        String desc = String.format("HeadObjectResult<%s>:\n metadata:%s", super.toString(), metadata.toString());
        return desc;
    }
}
