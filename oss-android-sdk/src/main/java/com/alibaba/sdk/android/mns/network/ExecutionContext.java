package com.alibaba.sdk.android.mns.network;

import com.alibaba.sdk.android.common.CancellationHandler;
import com.alibaba.sdk.android.mns.callback.MNSCompletedCallback;
import com.alibaba.sdk.android.mns.callback.MNSProgressCallback;
import com.alibaba.sdk.android.mns.model.MNSRequest;


import okhttp3.OkHttpClient;
/**
 * Created by pan.zengp on 2016/7/4.
 */
public class ExecutionContext<T extends MNSRequest> {
    private T request;
    private OkHttpClient client;
    private CancellationHandler cancellationHandler = new CancellationHandler();

    private MNSCompletedCallback completedCallback;
    private MNSProgressCallback progressCallback;

    public ExecutionContext(OkHttpClient client, T request) {
        this.client = client;
        this.request = request;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public CancellationHandler getCancellationHandler() {
        return cancellationHandler;
    }

    public void setCancellationHandler(CancellationHandler cancellationHandler) {
        this.cancellationHandler = cancellationHandler;
    }

    public MNSCompletedCallback getCompletedCallback() {
        return completedCallback;
    }

    public void setCompletedCallback(MNSCompletedCallback completedCallback) {
        this.completedCallback = completedCallback;
    }

    public MNSProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(MNSProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }
}
