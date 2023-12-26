/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * <p>
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss.common.utils;

import static com.alibaba.sdk.android.oss.common.OSSConstants.DEFAULT_CHARSET_NAME;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUtil {

    private static final String[] ENCODED_CHARACTERS_WITH_SLASHES = new String[]{"+", "*", "%7E", "%2F"};
    private static final String[] ENCODED_CHARACTERS_WITH_SLASHES_REPLACEMENTS = new String[]{"%20", "%2A", "~", "/"};

    private static final String[] ENCODED_CHARACTERS_WITHOUT_SLASHES = new String[]{"+", "*", "%7E"};
    private static final String[] ENCODED_CHARACTERS_WITHOUT_SLASHES_REPLACEMENTS = new String[]{"%20", "%2A", "~"};

    /**
     * Encode a URL segment with special chars replaced.
     *
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
            // a b*c~d/e+f  经过URLEncoder.encode 会 转变成 a+b*c%7Ed%2Fe%2Bf
            return encoded.replace("+", "%20").replace("*", "%2A")
                    .replace("%7E", "~").replace("%2F", "/");
        } catch (Exception e) {
            throw new IllegalArgumentException("failed to encode url!", e);
        }
    }

    public static String urlEncode(String value, boolean ignoreSlashes) {
        if (value == null) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(value, DEFAULT_CHARSET_NAME);
            if (!ignoreSlashes) {
                return StringUtils.replaceEach(encoded, ENCODED_CHARACTERS_WITHOUT_SLASHES, ENCODED_CHARACTERS_WITHOUT_SLASHES_REPLACEMENTS);
            }
            return StringUtils.replaceEach(encoded, ENCODED_CHARACTERS_WITH_SLASHES, ENCODED_CHARACTERS_WITH_SLASHES_REPLACEMENTS);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("FailedToEncodeUri", e);
        }
    }

    /**
     * Encode request parameters to URL segment.
     */
    public static String paramToQueryString(Map<String, String> params, String charset) {

        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder paramString = new StringBuilder();
        boolean first = true;
        for (Entry<String, String> p : params.entrySet()) {
            String key = p.getKey();
            String value = p.getValue();

            if (!first) {
                paramString.append("&");
            }

            // Urlencode each request parameter
            paramString.append(urlEncode(key, charset));
            if (value != null) {
                paramString.append("=").append(urlEncode(value, charset));
            }

            first = false;
        }

        return paramString.toString();
    }
}
