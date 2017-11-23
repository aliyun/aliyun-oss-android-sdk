package com.alibaba.sdk.android.oss.common.auth;

/**
 * Created by zhouzhuo on 11/4/15.
 * Edited by zhuoqin on 7/12/17.
 * Mobile devices are not the trusted environment. It's very risky to save the AccessKeyId and AccessKeySecret in mobile devices for accessing OSS.
 * We recommend to use STS authentication or custom authentication.
 */
@Deprecated
public class OSSPlainTextAKSKCredentialProvider implements OSSCredentialProvider {
    private String accessKeyId;
    private String accessKeySecret;

    /**
     * 用阿里云提供的AccessKeyId， AccessKeySecret构造一个凭证提供器
     *
     * @param accessKeyId
     * @param accessKeySecret
     */
    public OSSPlainTextAKSKCredentialProvider(String accessKeyId, String accessKeySecret) {
        setAccessKeyId(accessKeyId.trim());
        setAccessKeySecret(accessKeySecret.trim());
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    @Override
    public OSSFederationToken getFederationToken() {
        return null;
    }
}
