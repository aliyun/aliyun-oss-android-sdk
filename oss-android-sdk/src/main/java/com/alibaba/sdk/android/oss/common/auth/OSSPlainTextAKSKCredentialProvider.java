package com.alibaba.sdk.android.oss.common.auth;

/**
 * Created by zhouzhuo on 11/4/15.
 * Edited by zhuoqin on 7/12/17.
 * 移动终端是一个不受信任的环境，把AccessKeyId和AccessKeySecret直接保存在终端用来加签请求，存在极高的风险。推荐使用STS鉴权模式或自签名模式。
 */
@Deprecated
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
}
