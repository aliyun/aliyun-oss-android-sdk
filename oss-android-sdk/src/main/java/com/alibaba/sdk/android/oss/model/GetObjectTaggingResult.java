package com.alibaba.sdk.android.oss.model;

import java.util.Map;

public class GetObjectTaggingResult extends OSSResult {
    private Map<String, String> tags;

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
