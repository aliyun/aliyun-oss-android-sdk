package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/27/15.
 */
public class ResumableUploadResult extends CompleteMultipartUploadResult {

    public ResumableUploadResult(CompleteMultipartUploadResult completeResult) {
        this.setBucketName(completeResult.getBucketName());
        this.setObjectKey(completeResult.getObjectKey());
        this.setETag(completeResult.getETag());
        this.setLocation(completeResult.getLocation());
        this.setRequestId(completeResult.getRequestId());
        this.setResponseHeader(completeResult.getResponseHeader());
        this.setStatusCode(completeResult.getStatusCode());
        this.setServerCallbackReturnBody(completeResult.getServerCallbackReturnBody());
    }
}
