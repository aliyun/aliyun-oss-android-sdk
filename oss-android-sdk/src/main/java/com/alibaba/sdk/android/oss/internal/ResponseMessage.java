package com.alibaba.sdk.android.oss.internal;

import okhttp3.Response;

/**
 * Created by jingdan on 2017/11/27.
 */

public class ResponseMessage extends HttpMessage{

    private Response response;
    private RequestMessage request;
    private int statusCode;
    private boolean checkCRC64;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public RequestMessage getRequest() {
        return request;
    }

    public void setRequest(RequestMessage request) {
        this.request = request;
    }

    public boolean isCheckCRC64() {
        return checkCRC64;
    }

    public void setCheckCRC64(boolean checkCRC64) {
        this.checkCRC64 = checkCRC64;
    }
}
