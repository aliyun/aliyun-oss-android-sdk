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
     * Creates an instance of StsTokenCredentialProvider with the STS token got from RAM.
     * STS token has four entities: AccessKey, SecretKeyId, SecurityToken, Expiration.
     * If the authentication is in this way, SDK will not refresh the token once it's expired.
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
