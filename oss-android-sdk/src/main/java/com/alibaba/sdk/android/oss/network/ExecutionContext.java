package com.alibaba.sdk.android.oss.network;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.callback.OSSRetryCallback;
import com.alibaba.sdk.android.oss.model.OSSRequest;

import okhttp3.OkHttpClient;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class ExecutionContext<T extends OSSRequest> {

    private T request;
    private OkHttpClient client;
    private CancellationHandler cancellationHandler = new CancellationHandler();
    private Context applicationContext;
    private OSSCompletedCallback completedCallback;
    private OSSProgressCallback progressCallback;
    private OSSRetryCallback retryCallback;
    private ClientConfiguration conf;

    public ExecutionContext(OkHttpClient client, T request) {
        this(client,request,null);
    }

    public ExecutionContext(OkHttpClient client, T request, Context applicationContext) {
        setClient(client);
        setRequest(request);
        this.applicationContext = applicationContext;
    }

    public Context getApplicationContext() {
        return applicationContext;
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

    public OSSCompletedCallback getCompletedCallback() {
        return completedCallback;
    }

    public void setCompletedCallback(OSSCompletedCallback completedCallback) {
        this.completedCallback = completedCallback;
    }

    public OSSProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(OSSProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public OSSRetryCallback getRetryCallback() {
        return retryCallback;
    }

    public void setRetryCallback(OSSRetryCallback retryCallback) {
        this.retryCallback = retryCallback;
    }

    public ClientConfiguration getConfig() {
        return conf;
    }

    public void setConfig(ClientConfiguration conf) {
        this.conf = conf;
    }
}
