package com.alibaba.sdk.android.oss.model;

/**
 * Created by zhouzhuo on 11/23/15.
 */

/**
 * The result class of uploading an object
 */
public class PutObjectResult extends OSSResult {

    // Object ETag
    private String eTag;

    // The callback response if the servercallback is specified
    private String serverCallbackReturnBody;

    /**
     * Gets the Etag value of the target object
     */
    public String getETag() {
        return eTag;
    }

    /**
     * @param eTag target object's ETag value.
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Gets the callback response if the servercallback is specified
     *
     * @return The callback response in Json
     */
    public String getServerCallbackReturnBody() {
        return serverCallbackReturnBody;
    }

    public void setServerCallbackReturnBody(String serverCallbackReturnBody) {
        this.serverCallbackReturnBody = serverCallbackReturnBody;
    }
}
