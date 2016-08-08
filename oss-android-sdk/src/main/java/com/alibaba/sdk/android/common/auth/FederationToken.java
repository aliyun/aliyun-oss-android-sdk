package com.alibaba.sdk.android.common.auth;


import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.common.utils.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author zhouzhuo
 * Mar 26, 2015
 *
 */
public class FederationToken {
    private String tempAk;
    private String tempSk;
    private String securityToken;
    private long expiration;

    public FederationToken() {}

    /**
     * 构造一个新的OSSFederationToken
     * @param tempAK STS返回Token中的AccessKeyId
     * @param tempSK STS返回Token中的AccessKeySecret
     * @param securityToken STS返回Token中的SecurityToken
     * @param expiration STS返回的Token的过期时间点，单位秒，为linux Epoch时间
     */
    public FederationToken(String tempAK, String tempSK, String securityToken, long expiration) {
        this.tempAk = tempAK;
        this.tempSk = tempSK;
        this.securityToken = securityToken;
        this.setExpiration(expiration);
    }

    /**
     * 构造一个新的OSSFederationToken
     * @param tempAK STS返回Token中的AccessKeyId
     * @param tempSK STS返回Token中的AccessKeySecret
     * @param securityToken STS返回Token中的SecurityToken
     * @param expirationInGMTFormat STS返回的Token的过期时间点，是一个GMT格式字符串，即STS返回时的原时间字符串
     */
    public FederationToken(String tempAK, String tempSK, String securityToken, String expirationInGMTFormat) {
        this.tempAk = tempAK;
        this.tempSk = tempSK;
        this.securityToken = securityToken;
        setExpirationInGMTFormat(expirationInGMTFormat);
    }

    @Override
    public String toString() {
        return "FederationToken [tempAk=" + tempAk + ", tempSk=" + tempSk + ", securityToken="
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

    public void setTempAk(String tempAk) {
        this.tempAk = tempAk;
    }

    public void setTempSk(String tempSk) {
        this.tempSk = tempSk;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    // 获取失效时间，为UNIX Epoch时间，一个long值，单位秒
    public long getExpiration() {
        return expiration;
    }

    // 设置失效时间，为UNIX Epoch时间，一个long值，单位秒
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    // 设置STS返回的Token的过期时间点，是一个GMT格式字符串，即STS返回时的原时间字符串
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
