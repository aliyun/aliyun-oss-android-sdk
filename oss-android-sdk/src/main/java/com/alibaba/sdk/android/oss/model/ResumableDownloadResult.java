package com.alibaba.sdk.android.oss.model;

import java.util.ArrayList;

public class ResumableDownloadResult extends OSSResult {

    private ObjectMetadata metadata;

    /**
     * Gets the metadata
     *
     * @return object metadata
     */
    public ObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }
}
