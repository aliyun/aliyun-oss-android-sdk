package com.alibaba.sdk.android.oss.common.auth;

import com.alibaba.sdk.android.oss.ClientException;

/**
 * Created by zhouzhuo on 11/4/15.
 */
public interface OSSCredentialProvider {

    /**
     * get OSSFederationToken instance
     *
     * @return
     */
    OSSFederationToken getFederationToken() throws ClientException;
}
