package com.alibaba.sdk.android.oss.network;

import android.content.Context;

import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.callback.OSSRetryCallback;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;

import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class ExecutionContext<Request extends OSSRequest, Result extends OSSResult> {

    private Request request;
    private OkHttpClient client;
    private CancellationHandler cancellationHandler = new CancellationHandler();
    private Context applicationContext;
    private WeakReference<OSSCompletedCallback> completedCallbackWeakRef;
//    private OSSCompletedCallback completedCallback;
    private WeakReference<OSSProgressCallback> progressCallbackWeakRef;
//    private OSSProgressCallback progressCallback;
    private WeakReference<OSSRetryCallback> retryCallbackWeakRef;
//    private OSSRetryCallback retryCallback;

    public ExecutionContext(OkHttpClient client, Request request) {
        this(client, request, null);
    }

    public ExecutionContext(OkHttpClient client, Request request, Context applicationContext) {
        setClient(client);
        setRequest(request);
        this.applicationContext = applicationContext;
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
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

    public OSSCompletedCallback<Request, Result> getCompletedCallback() {
        if (completedCallbackWeakRef == null){
            return null;
        }else{
            return completedCallbackWeakRef.get();
        }
    }

    public void setCompletedCallback(OSSCompletedCallback<Request, Result> completedCallback) {
        completedCallbackWeakRef = new WeakReference<OSSCompletedCallback>(completedCallback);
//        this.completedCallback = completedCallback;
    }

    public OSSProgressCallback getProgressCallback() {
        if (progressCallbackWeakRef == null){
            return null;
        }else{
            return progressCallbackWeakRef.get();
        }
//        return progressCallback;
    }

    public void setProgressCallback(OSSProgressCallback progressCallback) {
        progressCallbackWeakRef = new WeakReference<OSSProgressCallback>(progressCallback);
//        this.progressCallback = progressCallback;
    }

    public OSSRetryCallback getRetryCallback() {
        if (retryCallbackWeakRef == null){
            return null;
        }else{
            return retryCallbackWeakRef.get();
        }
    }

    public void setRetryCallback(OSSRetryCallback retryCallback) {
        retryCallbackWeakRef = new WeakReference<OSSRetryCallback>(retryCallback);
//        this.retryCallback = retryCallback;
    }
}
