package com.alibaba.sdk.android.common.auth;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.common.utils.DateUtil;

/**
 * Created by zhouzhuo on 11/4/15.
 */
public abstract class FederationCredentialProvider extends CredentialProvider {

    private volatile FederationToken cachedToken;

    /**
     * Gets the valid STS token. The subclass needs to implement this function.
     * @return The valid STS Token
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
