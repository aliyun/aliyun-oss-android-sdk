package com.alibaba.sdk.android.oss.common.auth;

/**
 * Created by zhouzhuo on 11/4/15.
 */
public class OSSPlainTextAKSKCredentialProvider extends OSSCredentialProvider {
    private String accessKeyId;
    private String accessKeySecret;

    /**
     * 用阿里云提供的AccessKeyId， AccessKeySecret构造一个凭证提供器
     *
     * @param accessKeyId
     * @param accessKeySecret
     */
    public OSSPlainTextAKSKCredentialProvider(String accessKeyId, String accessKeySecret) {
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
