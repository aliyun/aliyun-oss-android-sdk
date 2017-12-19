package com.alibaba.sdk.android.oss.exception;

import java.io.IOException;

/**
 * Created by jingdan on 2017/11/29.
 */

public class InconsistentException extends IOException {

    private Long clientChecksum;
    private Long serverChecksum;
    private String requestId;

    public InconsistentException(Long clientChecksum, Long serverChecksum, String requestId) {
        super();
        this.clientChecksum = clientChecksum;
        this.serverChecksum = serverChecksum;
        this.requestId = requestId;
    }

    @Override
    public String getMessage() {
        return "InconsistentException: inconsistent object"
                + "\n[RequestId]: " + requestId
                + "\n[ClientChecksum]: " + clientChecksum
                + "\n[ServerChecksum]: " + serverChecksum;
    }
}
