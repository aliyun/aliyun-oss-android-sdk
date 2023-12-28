package com.alibaba.sdk.android.oss.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class OSSRequest {

    // Flag of explicitly requiring authorization.
    private boolean isAuthorizationRequired = true;
    // crc64
    private Enum CRC64 = CRC64Config.NULL;

    public boolean isAuthorizationRequired() {
        return isAuthorizationRequired;
    }

    private Set<String> additionalHeaderNames = new HashSet<String>();
    /**
     * Sets the flag of explicitly requiring authorization.
     * For example if the bucket's permission setting is public-ready, then call this method with parameter
     * isAuthorizationRequired:false will skip the authorization.
     *
     * @param isAuthorizationRequired flag of requiring authorization.
     */
    public void setIsAuthorizationRequired(boolean isAuthorizationRequired) {
        this.isAuthorizationRequired = isAuthorizationRequired;
    }

    public Enum getCRC64() {
        return CRC64;
    }

    public void setCRC64(Enum CRC64) {
        this.CRC64 = CRC64;
    }

    public Set<String> getAdditionalHeaderNames() {
        return additionalHeaderNames;
    }

    public void setAdditionalHeaderNames(Set<String> additionalHeaderNames) {
        this.additionalHeaderNames = additionalHeaderNames;
    }

    public enum CRC64Config {
        NULL,
        YES,
        NO
    }
}
