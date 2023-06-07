package com.alibaba.sdk.android.oss.common;

public enum HttpProtocol {
    HTTP("http"),
    HTTPS("https");
    private final String httpProtocol;

    private HttpProtocol(String protocol) {
        httpProtocol = protocol;
    }

    public String toString() {
        return httpProtocol;
    }
}
