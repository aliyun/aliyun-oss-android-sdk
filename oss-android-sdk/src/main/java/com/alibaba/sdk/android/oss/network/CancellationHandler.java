package com.alibaba.sdk.android.oss.network;

import okhttp3.Call;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class CancellationHandler {

    private volatile boolean isCancelled;

    private volatile Call call;

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
        isCancelled = true;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCall(Call call) {
        this.call = call;
    }
}
