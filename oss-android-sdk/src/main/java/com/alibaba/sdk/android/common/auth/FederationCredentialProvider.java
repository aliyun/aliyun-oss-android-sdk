package com.alibaba.sdk.android.common.auth;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.common.utils.DateUtil;

/**
 * Created by zhouzhuo on 11/4/15.
 */
public abstract class FederationCredentialProvider extends CredentialProvider {

    private volatile FederationToken cachedToken;

    /**
     * 需要实现这个回调函数，返回一个可用的STS Token
     * @return 有效的STS Token
     */
    public abstract FederationToken getFederationToken();

    public synchronized FederationToken getValidFederationToken() {
        if (cachedToken == null
                || DateUtil.getFixedSkewedTimeMillis() / 1000 > cachedToken.getExpiration() - 15) {

            if (cachedToken != null) {
                OSSLog.logD("token expired! current time: " + DateUtil.getFixedSkewedTimeMillis() / 1000 + " token expired: " + cachedToken.getExpiration());
            }
            cachedToken = getFederationToken();
        }

        return cachedToken;
    }
}
