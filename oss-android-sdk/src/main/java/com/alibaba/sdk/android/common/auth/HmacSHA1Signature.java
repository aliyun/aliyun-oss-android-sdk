/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.common.auth;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.sdk.android.common.utils.BinaryUtil;

/**
 * Hmac-SHA1签名。
 *
 */
public class HmacSHA1Signature {
    private static final String DEFAULT_ENCODING = "UTF-8"; // Default encoding
    private static final String ALGORITHM = "HmacSHA1"; // Signature method.
    private static final String VERSION = "1"; // Signature version.
    private static final Object LOCK = new Object();
    private static Mac macInstance; // Prototype of the Mac instance.

    public String getAlgorithm() {
        return ALGORITHM;
    }

    public String getVersion() {
        return VERSION;
    }

    public HmacSHA1Signature(){
    }

    public String computeSignature(String key, String data){
        try{
            byte[] signData = sign(
                    key.getBytes(DEFAULT_ENCODING),
                    data.getBytes(DEFAULT_ENCODING));

            return BinaryUtil.toBase64String(signData);
        }
        catch(UnsupportedEncodingException ex){
            throw new RuntimeException("Unsupported algorithm: " + DEFAULT_ENCODING);
        }
    }


    private byte[] sign(byte[] key, byte[] data){
        try{
            // Because Mac.getInstance(String) calls a synchronized method,
            // it could block on invoked concurrently.
            // SO use prototype pattern to improve perf.
            if (macInstance == null) {
                synchronized (LOCK) {
                    if (macInstance == null) {
                        macInstance = Mac.getInstance(ALGORITHM);
                    }
                }
            }

            Mac mac = null;
            try {
                mac = (Mac)macInstance.clone();
            } catch (CloneNotSupportedException e) {
                // If it is not clonable, create a new one.
                mac = Mac.getInstance(ALGORITHM);
            }
            mac.init(new SecretKeySpec(key, ALGORITHM));
            return mac.doFinal(data);
        }
        catch(NoSuchAlgorithmException ex){
            throw new RuntimeException("Unsupported algorithm: " + ALGORITHM);
        }
        catch(InvalidKeyException ex){
            throw new RuntimeException();
        }
    }
}