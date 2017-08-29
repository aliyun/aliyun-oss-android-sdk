/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss.common.utils;

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
        } catch (Exception e) {
            throw new IllegalArgumentException("failed to encode url!", e);
        }
    }
}
