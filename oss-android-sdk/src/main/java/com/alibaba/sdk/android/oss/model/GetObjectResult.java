package com.alibaba.sdk.android.oss.model;

import com.alibaba.sdk.android.oss.internal.CheckCRC64DownloadInputStream;

import java.io.InputStream;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class GetObjectResult extends OSSResult {

    // object metadata
    private ObjectMetadata metadata = new ObjectMetadata();

    // content length
    private long contentLength;

    // object's content
    private InputStream objectContent;

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

    /**
     * Gets the object content
     *
     * @return Object's content in the form of InoutStream
     */
    public InputStream getObjectContent() {
        return objectContent;
    }

    public void setObjectContent(InputStream objectContent) {
        this.objectContent = objectContent;
    }

    /**
     * Gets the object length
     *
     * @return object length
     */
    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public Long getClientCRC() {
        if (objectContent != null && (objectContent instanceof CheckCRC64DownloadInputStream)) {
            return ((CheckCRC64DownloadInputStream) objectContent).getClientCRC64();
        }
        return super.getClientCRC();
    }
}
