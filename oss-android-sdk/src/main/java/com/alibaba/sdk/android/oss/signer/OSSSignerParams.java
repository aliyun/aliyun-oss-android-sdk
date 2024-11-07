package com.alibaba.sdk.android.oss.signer;


import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class OSSSignerParams {
    /* Note that resource path should not have been url-encoded. */
    private String resourcePath;

    private OSSCredentialProvider credentialProvider;

    private String product;

    private String region;

    private String cloudBoxId;

    private long expiration;

    private Set<String> additionalHeaderNames = new HashSet<String>();

    public OSSSignerParams(String resourcePath, OSSCredentialProvider credentialProvider) {
        this.resourcePath = resourcePath;
        this.credentialProvider = credentialProvider;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public OSSCredentialProvider getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(OSSCredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCloudBoxId() {
        return cloudBoxId;
    }

    public void setCloudBoxId(String cloudBoxId) {
        this.cloudBoxId = cloudBoxId;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public Set<String> getAdditionalHeaderNames() {
        return additionalHeaderNames;
    }

    public void setAdditionalHeaderNames(Set<String> additionalHeaderNames) {
        this.additionalHeaderNames = additionalHeaderNames;
    }
}