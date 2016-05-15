package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSLog;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

/**
 * Created by zhouzhuo on 11/6/15.
 */
public class OSSRetryHandler {

    private int maxRetryCount = 2;

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public OSSRetryHandler(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public  OSSRetryType shouldRetry(Exception e, int currentRetryCount) {
        if (currentRetryCount >= maxRetryCount) {
            return OSSRetryType.OSSRetryTypeShouldNotRetry;
        }

        if (e instanceof ClientException) {
            if (((ClientException) e).isCanceledException()) {
                return OSSRetryType.OSSRetryTypeShouldNotRetry;
            }

            Exception localException = (Exception) e.getCause();
            if (localException instanceof InterruptedIOException
                    && !(localException instanceof SocketTimeoutException)) {
                OSSLog.logE("[shouldRetry] - is interrupted!");
                return OSSRetryType.OSSRetryTypeShouldNotRetry;
            } else if (localException instanceof IllegalArgumentException) {
                return OSSRetryType.OSSRetryTypeShouldNotRetry;
            }
            OSSLog.logD("shouldRetry - " + e.toString());
            e.getCause().printStackTrace();
            return OSSRetryType.OSSRetryTypeShouldRetry;
        } else if (e instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) e;
            if (serviceException.getErrorCode() != null && serviceException.getErrorCode().equalsIgnoreCase("RequestTimeTooSkewed")) {
                return OSSRetryType.OSSRetryTypeShouldFixedTimeSkewedAndRetry;
            } else if (serviceException.getStatusCode() >= 500){
                return OSSRetryType.OSSRetryTypeShouldRetry;
            } else {
                return OSSRetryType.OSSRetryTypeShouldNotRetry;
            }
        } else {
            return OSSRetryType.OSSRetryTypeShouldNotRetry;
        }
    }
}
