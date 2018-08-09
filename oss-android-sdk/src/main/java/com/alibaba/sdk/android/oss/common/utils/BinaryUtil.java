/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * <p>
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss.common.utils;

import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BinaryUtil {
    public static String toBase64String(byte[] binaryData) {
        return new String(Base64.encode(binaryData,Base64.DEFAULT)).trim();
    }

    /**
     * decode base64 string
     */
    public static byte[] fromBase64String(String base64String) {
        return Base64.decode(base64String,Base64.DEFAULT);
    }

    /**
     * calculate md5 for bytes
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
     * calculate md5 for local file
     */
    public static byte[] calculateMd5(String filePath) throws IOException {
        byte[] md5;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[10 * 1024];
            FileInputStream is = new FileInputStream(new File(filePath));
            int len;
            while ((len = is.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            is.close();
            md5 = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found.");
        }
        return md5;
    }

    /**
     * calculate md5 for bytes and string back
     */
    public static String calculateMd5Str(byte[] binaryData) {
        return getMd5StrFromBytes(calculateMd5(binaryData));
    }

    /**
     * calculate md5 for file and string back
     */
    public static String calculateMd5Str(String filePath) throws IOException {
        return getMd5StrFromBytes(calculateMd5(filePath));
    }

    /**
     * calculate md5 for bytes and base64 string back
     */
    public static String calculateBase64Md5(byte[] binaryData) {
        return toBase64String(calculateMd5(binaryData));
    }

    /**
     * calculate md5 for local file and base64 string back
     */
    public static String calculateBase64Md5(String filePath) throws IOException {
        return toBase64String(calculateMd5(filePath));
    }

    /**
     * MD5sum for string
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

    /**
     * Get the sha1 value of the filepath specified file
     *
     * @param filePath The filepath of the file
     * @return The sha1 value
     */
    public static String fileToSHA1(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath); // Create an FileInputStream instance according to the filepath
            byte[] buffer = new byte[1024]; // The buffer to read the file
            MessageDigest digest = MessageDigest.getInstance("SHA-1"); // Get a SHA-1 instance
            int numRead = 0; // Record how many bytes have been read
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead); // Update the digest
                }
            }
            byte[] sha1Bytes = digest.digest(); // Complete the hash computing
            return convertHashToString(sha1Bytes); // Call the function to convert to hex digits
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close(); // Close the InputStream
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Convert the hash bytes to hex digits string
     *
     * @param hashBytes
     * @return The converted hex digits string
     */
    private static String convertHashToString(byte[] hashBytes) {
        String returnVal = "";
        for (int i = 0; i < hashBytes.length; i++) {
            returnVal += Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return returnVal.toLowerCase();
    }
}
