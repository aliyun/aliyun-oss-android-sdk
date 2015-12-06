package com.alibaba.sdk.android.oss.common.auth;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;

/**
 * Created by zhouzhuo on 11/4/15.
 */
public abstract class OSSFederationCredentialProvider extends OSSCredentialProvider {

    private volatile OSSFederationToken cachedToken;

    /**
     * 需要实现这个回调函数，返回一个可用的STS Token
     * @return 有效的STS Token
     */
    public abstract OSSFederationToken getFederationToken();

    public synchronized OSSFederationToken getValidFederationToken() {
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
