package com.alibaba.sdk.android.common.auth;

/**
 * Created by zhouzhuo on 11/4/15.
 * Edited by zhuoqin on 7/12/17.
 * Mobile devices are not the trusted environment. It's very risky to save the AccessKeyId and AccessKeySecret in mobile devices for accessing OSS.
 * We recommend to use STS authentication or custom authentication.
 */
@Deprecated
public class PlainTextAKSKCredentialProvider extends CredentialProvider {
    private String accessKeyId;
    private String accessKeySecret;

    /**
     * Use AccessKeyId， AccessKeySecret provided by AliCloud to create the CredentialProvider instance
     *
     * @param accessKeyId
     * @param accessKeySecret
     */
    public PlainTextAKSKCredentialProvider(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId.trim();
        this.accessKeySecret = accessKeySecret.trim();
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
}
