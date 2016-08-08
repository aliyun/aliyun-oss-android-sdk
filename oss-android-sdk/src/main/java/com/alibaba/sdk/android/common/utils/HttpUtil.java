/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUtil {

    /**
     * Encode a URL segment with special chars replaced.
     * @param value
     * @return
     * @throws UnsupportedEncodingException
     */
    // TODO change the method name to percentageEncode
    public static String urlEncode(String value, String encoding) {
        if (value == null) {
            return "";
        }

        try {
            String encoded = URLEncoder.encode(value, encoding);
            return encoded.replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~").replace("%2F", "/");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("failed to encode url!", e);
        }
    }

    /**
     * Encodes request parameters to a URL query.
     * @param params
     * @param charset
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String paramToQueryString(Map<String, String> params, String charset)
            throws UnsupportedEncodingException{
        if (params == null || params.size() == 0){
            return null;
        }

        StringBuilder paramString = new StringBuilder();
        boolean first = true;
        for(Entry<String, String> p : params.entrySet()){
            String key = p.getKey();
            String val = p.getValue();

            if (!first){
                paramString.append("&");
            }

            paramString.append(key);
            if (val != null){
                // The query string in URL should be encoded with URLEncoder standard.
                paramString.append("=").append(HttpUtil.urlEncode(val, charset));
                // TODO: Should use URLEncoder.encode(val, charset)) instead of HttpUril#urlEncode;
            }

            first = false;
        }

        return paramString.toString();
    }

    private static final String ISO_8859_1_CHARSET = "iso-8859-1";
    private static final String JAVA_CHARSET = "utf-8";

    // To fix the bug that the header value could not be unicode chars.
    // Because HTTP headers are encoded in iso-8859-1,
    // we need to convert the utf-8(java encoding) strings to iso-8859-1 ones.
    public static void convertHeaderCharsetFromIso88591(Map<String, String> headers){
        convertHeaderCharset(headers, ISO_8859_1_CHARSET, JAVA_CHARSET);
    }

    // For response, convert from iso-8859-1 to utf-8.
    public static void convertHeaderCharsetToIso88591(Map<String, String> headers) {
        convertHeaderCharset(headers, JAVA_CHARSET, ISO_8859_1_CHARSET);
    }

    private static void convertHeaderCharset(
            Map<String, String> headers, String fromCharset, String toCharset){
        assert (headers != null);

        for(Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getValue() == null) {
                continue;
            }

            try {
                header.setValue(new String(header.getValue().getBytes(fromCharset) , toCharset));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("Invalid charset name.");
            }
        }
    }
}
