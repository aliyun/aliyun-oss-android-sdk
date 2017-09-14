package com.alibaba.sdk.android.oss.model;

/**
 * Created by jingdan on 2017/9/6.
 */

public class StsModel {
    public String RequestId;
    public AssumedRoleUser AssumedRoleUser;
    public Credentials Credentials;

    public static class AssumedRoleUser{
        public String AssumedRoleId;
        public String Arn;
    }

    public static class Credentials{
        public String AccessKeySecret;
        public String AccessKeyId;
        public String Expiration;
        public String SecurityToken;
    }
}



