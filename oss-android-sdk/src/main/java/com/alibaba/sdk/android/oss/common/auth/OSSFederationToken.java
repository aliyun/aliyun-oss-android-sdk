package com.alibaba.sdk.android.oss.common.auth;


import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author zhouzhuo
 *         Mar 26, 2015
 */
public class OSSFederationToken {
    private String tempAk;
    private String tempSk;
    private String securityToken;
    private long expiration;

    /**
     * Creates a new instance of OSSFederationToken
     *
     * @param tempAK        AccessKeyId returned from STS
     * @param tempSK        AccessKeySecret returned from STS
     * @param securityToken SecurityToken returned from STS
     * @param expiration    The expiration time in seconds from STS, in the Unix Epoch format.
     */
    public OSSFederationToken(String tempAK, String tempSK, String securityToken, long expiration) {
        setTempAk(tempAK);
        setTempSk(tempSK);
        setSecurityToken(securityToken);
        this.setExpiration(expiration);
    }

    /**
     * Creates a new instance of OSSFederationToken
     *
     * @param tempAK                AccessKeyId returned from STS
     * @param tempSK                AccessKeySecret returned from STS
     * @param securityToken         SecurityToken returned from STS
     * @param expirationInGMTFormat The expiration time in seconds from STS, in the GMT format.
     */
    public OSSFederationToken(String tempAK, String tempSK, String securityToken, String expirationInGMTFormat) {
        setTempAk(tempAK);
        setTempSk(tempSK);
        setSecurityToken(securityToken);
        setExpirationInGMTFormat(expirationInGMTFormat);
    }

    @Override
    public String toString() {
        return "OSSFederationToken [tempAk=" + tempAk + ", tempSk=" + tempSk + ", securityToken="
                + securityToken + ", expiration=" + expiration + "]";
    }

    public String getTempAK() {
        return tempAk;
    }

    public String getTempSK() {
        return tempSk;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public void setTempAk(String tempAk) {
        this.tempAk = tempAk;
    }

    public void setTempSk(String tempSk) {
        this.tempSk = tempSk;
    }

    // Gets the expiration time in seconds in Unix Epoch format.
    public long getExpiration() {
        return expiration;
    }

    // Sets the expiration time in seconds in Unix Epoch format.
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    // Sets the expiration time according to the value from STS. The time is in GMT format which is the original format returned from STS.
    public void setExpirationInGMTFormat(String expirationInGMTFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(expirationInGMTFormat);
            this.expiration = date.getTime() / 1000;
        } catch (ParseException e) {
            if (OSSLog.isEnableLog()) {
                e.printStackTrace();
            }
            this.expiration = DateUtil.getFixedSkewedTimeMillis() / 1000 + 30;
        }
    }
}
