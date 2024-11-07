package com.alibaba.sdk.android.oss.signer;


import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public abstract class ServiceSignature {

    /**
     * Gets the algorithm of signature.
     *
     * @return The algorithm of the signature.
     */
    public abstract String getAlgorithm();

    /**
     * Gets the algorithm version.
     *
     * @return The algorithm version.
     */
    public abstract String getVersion();

    /**
     * Computes the signature of the data by the given key.
     *
     * @param key
     *            The key for the signature.
     * @param data
     *            The data to compute the signature on.
     * @return The signature in string.
     */
    public abstract String computeSignature(String key, String data);

    /**
     * Computes the hash of the data by the given key.
     *
     * @param key
     *            The key for the signature.
     * @param data
     *            The data to compute the hash on.
     * @return The hash in byte array.
     */
    public abstract byte[] computeHash(byte[] key, byte[] data);

    /**
     *
     * Creates the default <code>ServiceSignature</code> instance which is
     * {@link HmacSHA1Signature}.
     *
     * @return The default <code>ServiceSignature</code> instance
     */
    public static ServiceSignature create() {
        return new HmacSHA1Signature();
    }

    /**
     *
     * Creates the <code>ServiceSignature</code> instance by the algorithm
     *
     * @return The <code>ServiceSignature</code> instance
     */
    public static ServiceSignature create(String algorithm) {
        if ("HmacSHA256".equals(algorithm)) {
            return new HmacSHA256Signature();
        } else if ("HmacSHA1".equals(algorithm)) {
            return new HmacSHA1Signature();
        }
        throw new RuntimeException("Unsupported algorithm: " + algorithm);
    }

    protected byte[] sign(byte[] key, byte[] data, Mac macInstance, Object lock, String algorithm) {
        try {
            // Because Mac.getInstance(String) calls a synchronized method, it
            // could block on
            // invoked concurrently, so use prototype pattern to improve perf.
            if (macInstance == null) {
                synchronized (lock) {
                    if (macInstance == null) {
                        macInstance = Mac.getInstance(algorithm);
                    }
                }
            }

            Mac mac;
            try {
                mac = (Mac) macInstance.clone();
            } catch (CloneNotSupportedException e) {
                // If it is not clonable, create a new one.
                mac = Mac.getInstance(algorithm);
            }
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unsupported algorithm: " + algorithm, ex);
        } catch (InvalidKeyException ex) {
            throw new RuntimeException("Invalid key: " + key, ex);
        }
    }

}