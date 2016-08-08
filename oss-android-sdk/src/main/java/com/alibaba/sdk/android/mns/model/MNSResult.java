package com.alibaba.sdk.android.mns.model;

import java.util.Map;

/**
 * Created by pan.zengp on 2016/7/4.
 */
public class MNSResult {

    private int statusCode;

    private Map<String, String> responseHeader;

    private String requestId;

    /**
     * 响应结果的HTTP响应码
     * @return HTTP响应码
     */
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * 响应结果的HTTP响应头部
     * @return 所有HTTP响应头
     */
    public Map<String, String> getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(Map<String, String> responseHeader) {
        this.responseHeader = responseHeader;
    }

    /**
     * 成功请求的RequestId
     * @return 标识唯一OSS请求的RequestId
     */
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
