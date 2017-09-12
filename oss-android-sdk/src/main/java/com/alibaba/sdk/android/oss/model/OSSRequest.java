package com.alibaba.sdk.android.oss.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class OSSRequest {

    // Flag of explicitly requiring authorization.
    private boolean isAuthorizationRequired = true;

    public boolean isAuthorizationRequired() {
        return isAuthorizationRequired;
    }

    /**
     * Sets the flag of explicitly requiring authorization.
     * For example if the bucket's permission setting is public-ready, then call this method with parameter
     * isAuthorizationRequired:false will skip the authorization.
     * @param isAuthorizationRequired flag of requiring authorization.
     */
    public void setIsAuthorizationRequired(boolean isAuthorizationRequired) {
        this.isAuthorizationRequired = isAuthorizationRequired;
    }
}
