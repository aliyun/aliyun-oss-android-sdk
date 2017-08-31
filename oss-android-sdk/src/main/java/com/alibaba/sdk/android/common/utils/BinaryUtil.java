/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.common.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BinaryUtil {
    public static String toBase64String(byte[] binaryData){
        return new String(Base64.encodeBase64(binaryData));
    }

    /**
     * Decodes base64 string
     */
    public static byte[] fromBase64String(String base64String){
        return Base64.decodeBase64(base64String.getBytes());
    }

    /**
     * Calculates MD5 digest of the byte array
     */
    public static byte[] calculateMd5(byte[] binaryData) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found.");
        }
        messageDigest.update(binaryData);
        return messageDigest.digest();
        
    }

    /**
     * Calculates the MD5 digest of the local file's content.
     */
    public static byte[] calculateMd5(String filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[4 * 1024];
            InputStream is = new FileInputStream(new File(filePath));
            int lent;
            while ((lent = is.read(buffer)) != -1) {
                digest.update(buffer, 0, lent);
            }
            is.close();
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found.");
        }
    }

    /**
     * Calculates the MD5 digest of byte array and returns the MD5 string.
     */
    public static String calculateMd5Str(byte[] binaryData) {
        return getMd5StrFromBytes(calculateMd5(binaryData));
    }

    /**
     * Calculates the MD5 digest of the local file's content and returns the MD5 string.
     */
    public static String calculateMd5Str(String filePath) throws IOException {
        return getMd5StrFromBytes(calculateMd5(filePath));
    }

    /**
     * Calculates the MD5 of the byte array and returns the base64 encoded string.
     */
    public static String calculateBase64Md5(byte[] binaryData) {
        return toBase64String(calculateMd5(binaryData));
    }

    /**
     * Calculates the MD5 of the local file's content and returns the base64 encoded string.
     */
    public static String calculateBase64Md5(String filePath) throws IOException {
        return toBase64String(calculateMd5(filePath));
    }

    /**
     * Convert the MD5 digest to string
     */
    public static String getMd5StrFromBytes(byte[] md5bytes) {
        if (md5bytes == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < md5bytes.length; i++) {
            sb.append(String.format("%02x", md5bytes[i]));
        }
        return sb.toString();
    }
}
