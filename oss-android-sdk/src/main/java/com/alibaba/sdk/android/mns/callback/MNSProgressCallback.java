package com.alibaba.sdk.android.mns.callback;

/**
 * Created by pan.zengp on 2016/7/4.
 */
public interface MNSProgressCallback<T> {
    public void onProgress(T request, long currentSize, long totalSize);
}
