/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.common.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class IOUtils {

    private final static int BUFFER_SIZE = 4096;

    public static String readStreamAsString(InputStream in, String charset)
            throws IOException {
        if (in == null)
            return "";

        Reader reader = null;
        Writer writer = new StringWriter();
        String result;

        char[] buffer = new char[1024];
        try{
            reader = new BufferedReader(
                    new InputStreamReader(in, charset));

            int n;
            while((n = reader.read(buffer)) > 0){
                writer.write(buffer, 0, n);
            }

            result = writer.toString();
        } finally {
            in.close();
            if (reader != null){
                reader.close();
            }
            if (writer != null){
                writer.close();
            }
        }

        return result;
    }
    
    public static byte[] readStreamAsBytesArray(InputStream in)
        throws IOException {
        if (in == null) {
            return new byte[0];
        }
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        while ((len = in.read(buffer)) > -1) {
            output.write(buffer, 0, len);
        }
        output.flush();
        return output.toByteArray();
    }

    public static byte[] readStreamAsBytesArray(InputStream in, int readLength)
            throws IOException {
        if (in == null) {
            return new byte[0];
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        long readed = 0;
        while (readed < readLength && (len = in.read(buffer, 0, Math.min(2048, (int)(readLength - readed)))) > -1) {
            output.write(buffer, 0, len);
            readed += len;
        }
        output.flush();
        return output.toByteArray();
    }

    public static void safeClose(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) { }
        }
    }

    public static void readStreamToFile(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        int len;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
        out.close();
    }

    public static void safeClose(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {}
        }
    }
}
