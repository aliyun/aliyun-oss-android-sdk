/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * <p>
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss.common.auth;

import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Hmac-SHA1 signature
 */
public class HmacSHA1Signature {
    private static final String DEFAULT_ENCODING = "UTF-8"; // Default encoding
    private static final String ALGORITHM = "HmacSHA1"; // Signature method.
    private static final String VERSION = "1"; // Signature version.
    private static final Object LOCK = new Object();
    private static Mac macInstance; // Prototype of the Mac instance.

    public HmacSHA1Signature() {
    }

    public String getAlgorithm() {
        return ALGORITHM;
    }

    public String getVersion() {
        return VERSION;
    }

    public String computeSignature(String key, String data) {
        OSSLog.logDebug(getAlgorithm(), false);
        OSSLog.logDebug(getVersion(), false);
        String sign = null;
        try {
            byte[] signData = sign(
                    key.getBytes(DEFAULT_ENCODING),
                    data.getBytes(DEFAULT_ENCODING));

            sign = BinaryUtil.toBase64String(signData);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported algorithm: " + DEFAULT_ENCODING);
        }
        return sign;
    }


    private byte[] sign(byte[] key, byte[] data) {
        byte[] sign = null;
        try {
            // Because Mac.getInstance(String) calls a synchronized method,
            // it could block on invoked concurrently.
            // SO use prototype pattern to improve perf.
            if (macInstance == null) {
                synchronized (LOCK) {
                    if (macInstance == null) {
                        macInstance = Mac.getInstance(getAlgorithm());
                    }
                }
            }

            Mac mac;
            try {
                mac = (Mac) macInstance.clone();
            } catch (CloneNotSupportedException e) {
                // If it is not clonable, create a new one.
                mac = Mac.getInstance(getAlgorithm());
            }
            mac.init(new SecretKeySpec(key, getAlgorithm()));
            sign = mac.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unsupported algorithm: " + ALGORITHM);
        } catch (InvalidKeyException ex) {
            throw new RuntimeException("key must not be null");
        }
        return sign;
    }
}