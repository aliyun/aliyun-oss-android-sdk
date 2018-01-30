package com.alibaba.sdk.android.oss.model;

/**
 * Created by huaixu on 2018/1/29.
 */

public class TriggerCallbackResult extends OSSResult {

    private String mServerCallbackReturnBody;

    /**
     * Gets the callback response if the servercallback is specified
     *
     * @return The callback response in Json
     */
    public String getServerCallbackReturnBody() {
        return mServerCallbackReturnBody;
    }

    public void setServerCallbackReturnBody(String serverCallbackReturnBody) {
        this.mServerCallbackReturnBody = serverCallbackReturnBody;
    }
}
