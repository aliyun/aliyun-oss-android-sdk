package com.alibaba.sdk.android.oss.app;

/**
 * Created by jingdan on 2017/8/31.
 */

public interface ProgressCallback<T1,T2> extends Callback<T1,T2> {
    void onProgress(T1 request, long currentSize, long totalSize);
}
