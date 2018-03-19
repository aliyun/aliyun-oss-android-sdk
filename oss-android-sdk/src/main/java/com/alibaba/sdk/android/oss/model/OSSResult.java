package com.alibaba.sdk.android.oss.model;

import java.util.Map;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class OSSResult {

    private int statusCode;

    private Map<String, String> responseHeader;

    private String requestId;

    //client crc64
    private Long clientCRC;
    //server crc64
    private Long serverCRC;

    /**
     * The HTTP status code
     *
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
     *
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
     *
     * @return The globally unique request Id
     */
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getClientCRC() {
        return clientCRC;
    }

    public void setClientCRC(Long clientCRC) {
        if (clientCRC != null && clientCRC != 0) {
            this.clientCRC = clientCRC;
        }
    }

    public Long getServerCRC() {
        return serverCRC;
    }

    public void setServerCRC(Long serverCRC) {
        if (serverCRC != null && serverCRC != 0) {
            this.serverCRC = serverCRC;
        }
    }

    @Override
    public String toString() {
        String desc = String.format("OSSResult<%s>: \nstatusCode:%d,\nresponseHeader:%s,\nrequestId:%s", super.toString(), statusCode, responseHeader.toString(), requestId);
        return desc;
    }
}
