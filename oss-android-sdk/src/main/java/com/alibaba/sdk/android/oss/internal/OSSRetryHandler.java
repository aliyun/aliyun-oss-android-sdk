package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;

import okhttp3.Response;

/**
 * Created by zhouzhuo on 11/6/15.
 */
public class OSSRetryHandler {

    private int maxRetryCount = 2;

    public OSSRetryHandler(int maxRetryCount) {
        setMaxRetryCount(maxRetryCount);
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public OSSRetryType shouldRetry(Exception e, Response response, int currentRetryCount) {
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
                OSSLog.logError("[shouldRetry] - is interrupted!");
                return OSSRetryType.OSSRetryTypeShouldNotRetry;
            } else if (localException instanceof IllegalArgumentException) {
                return OSSRetryType.OSSRetryTypeShouldNotRetry;
            }
            OSSLog.logDebug("shouldRetry - " + e.toString());
            e.getCause().printStackTrace();
            return OSSRetryType.OSSRetryTypeShouldRetry;
        } else if (e instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) e;
            if (serviceException.getErrorCode() != null && serviceException.getErrorCode().equalsIgnoreCase("RequestTimeTooSkewed")) {
                return OSSRetryType.OSSRetryTypeShouldFixedTimeSkewedAndRetry;
            } else if (serviceException.getErrorCode() != null &&
                    serviceException.getMessage() != null &&
                    serviceException.getErrorCode().equalsIgnoreCase("InvalidArgument") &&
                    serviceException.getMessage().equalsIgnoreCase("Invalid signing date in Authorization header.")) {
                try {
                    String responseDateString = response.headers().get(OSSHeaders.DATE);
                    long serverTime = DateUtil.parseRfc822Date(responseDateString).getTime();
                    long timeDifference = DateUtil.getFixedSkewedTimeMillis() - serverTime;
                    if (timeDifference > 15 * 60 * 1000 || timeDifference < -15 * 60 * 1000) {
                        return OSSRetryType.OSSRetryTypeShouldFixedTimeSkewedAndRetry;
                    } else {
                        return OSSRetryType.OSSRetryTypeShouldNotRetry;
                    }
                } catch (ParseException ex) {
                    OSSLog.logThrowable2Local(ex);
                    return OSSRetryType.OSSRetryTypeShouldNotRetry;
                }
            } else if (serviceException.getStatusCode() >= 500) {
                return OSSRetryType.OSSRetryTypeShouldRetry;
            } else {
                return OSSRetryType.OSSRetryTypeShouldNotRetry;
            }
        } else {
            return OSSRetryType.OSSRetryTypeShouldNotRetry;
        }
    }

    public long timeInterval(int currentRetryCount, OSSRetryType retryType) {
        switch (retryType) {
            case OSSRetryTypeShouldRetry:
                return (long)Math.pow(2, currentRetryCount) * 200;
            default:
                return 0;
        }
    }
}
