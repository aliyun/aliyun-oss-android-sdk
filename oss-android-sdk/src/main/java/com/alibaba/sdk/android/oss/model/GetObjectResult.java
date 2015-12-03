package com.alibaba.sdk.android.oss.model;

import java.io.InputStream;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class GetObjectResult extends OSSResult {

    // object的元信息
    private ObjectMetadata metadata = new ObjectMetadata();

    // 文件长度
    private long contentLength;

    // 文件内容的输入流
    private InputStream objectContent;

    /**
     * 返回文件的元信息
     * @return 文件元信息
     */
    public ObjectMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * 返回文件内容的输入流
     * @return 文件内容的输入流
     */
    public InputStream getObjectContent() {
        return objectContent;
    }

    public void setObjectContent(InputStream objectContent) {
        this.objectContent = objectContent;
    }

    /**
     * 返回文件长度
     * @return 文件长度
     */
    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
}
