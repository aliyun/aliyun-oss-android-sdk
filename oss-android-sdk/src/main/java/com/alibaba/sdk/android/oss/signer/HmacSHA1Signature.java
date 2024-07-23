package com.alibaba.sdk.android.oss.signer;

import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;

import java.io.UnsupportedEncodingException;

import javax.crypto.Mac;

public class HmacSHA1Signature extends ServiceSignature {

    /* The default encoding. */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /* Signature method. */
    private static final String ALGORITHM = "HmacSHA1";

    /* Signature version. */
    private static final String VERSION = "1";

    private static final Object LOCK = new Object();

    /* Prototype of the Mac instance. */
    private static Mac macInstance;

    public String getAlgorithm() {
        return ALGORITHM;
    }

    public String getVersion() {
        return VERSION;
    }

    public String computeSignature(String key, String data) {
        try {
            byte[] signData = sign(key.getBytes(DEFAULT_ENCODING), data.getBytes(DEFAULT_ENCODING), macInstance,
                    LOCK, ALGORITHM);
            return BinaryUtil.toBase64String(signData);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported algorithm: " + DEFAULT_ENCODING, ex);
        }
    }

    public byte[] computeHash(byte[] key, byte[] data) {
        return sign(key, data, macInstance, LOCK, ALGORITHM);
    }
}
