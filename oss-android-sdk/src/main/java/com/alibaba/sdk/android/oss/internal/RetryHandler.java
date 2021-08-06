package com.alibaba.sdk.android.oss.internal;

public interface RetryHandler {


    public OSSRetryType shouldRetry(Exception e, int currentRetryCount);

    public long timeInterval(int currentRetryCount, OSSRetryType retryType);
}
