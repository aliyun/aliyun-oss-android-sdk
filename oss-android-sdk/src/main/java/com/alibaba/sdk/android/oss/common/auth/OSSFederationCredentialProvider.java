package com.alibaba.sdk.android.oss.common.auth;

/**
 * Created by zhouzhuo on 11/4/15.
 */
public abstract class OSSFederationCredentialProvider extends OSSCredentialProvider {

    /**
     * 需要实现这个回调函数，返回一个可用的STS Token
     * @return 有效的STS Token
     */
    public abstract OSSFederationToken getFederationToken();
}
