package com.alibaba.sdk.android.common.auth;

import com.alibaba.sdk.android.common.auth.FederationToken;
import com.alibaba.sdk.android.common.auth.CredentialProvider;

/**
 * Created by zhouzhuo on 1/22/16.
 */
public class StsTokenCredentialProvider extends CredentialProvider {

    private String accessKeyId;
    private String secretKeyId;
    private String securityToken;

    /**
     * 用预先向RAM获取到的STS Token构造一个凭证提供器，STS Token通常包含4个字段：AccessKey、SecretKeyId、SecurityToken、Expiration
     * 使用OSSStsTokenCredeProvider鉴权方式，SDK将不会管理Token的过期时间
     * @param accessKeyId
     * @param secretKeyId
     * @param securityToken
     */
    public StsTokenCredentialProvider(String accessKeyId, String secretKeyId, String securityToken) {
        this.accessKeyId = accessKeyId.trim();
        this.secretKeyId = secretKeyId.trim();
        this.securityToken = securityToken.trim();
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretKeyId() {
        return secretKeyId;
    }

    public void setSecretKeyId(String secretKeyId) {
        this.secretKeyId = secretKeyId;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public FederationToken getFederationToken() {
        return new FederationToken(accessKeyId, secretKeyId, securityToken, Long.MAX_VALUE);
    }
}
