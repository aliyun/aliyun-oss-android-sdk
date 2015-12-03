package com.alibaba.sdk.android.oss.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class OSSRequest {

    // 可以显式强制该次请求是否需要鉴权
    private boolean isAuthorizationRequired = true;

    public boolean isAuthorizationRequired() {
        return isAuthorizationRequired;
    }

    /**
     * 可以通过这个函数指定本次请求是否鉴权
     * 如，如果Bucket的权限是公共读，那么GetObjectRequest设置false后，请求不走鉴权逻辑
     * @param isAuthorizationRequired 是否鉴权
     */
    public void setIsAuthorizationRequired(boolean isAuthorizationRequired) {
        this.isAuthorizationRequired = isAuthorizationRequired;
    }
}
