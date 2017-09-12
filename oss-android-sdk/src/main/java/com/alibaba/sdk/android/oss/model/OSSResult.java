package com.alibaba.sdk.android.oss.model;

import java.util.Map;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class OSSResult {

    private int statusCode;

    private Map<String, String> responseHeader;

    private String requestId;

    /**
     * The HTTP status code
     * @return HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * The response header
     * @return ALl headers in the response
     */
    public Map<String, String> getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(Map<String, String> responseHeader) {
        this.responseHeader = responseHeader;
    }

    /**
     * The request Id---it's generated from OSS server side.
     * @return The globally unique request Id
     */
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
