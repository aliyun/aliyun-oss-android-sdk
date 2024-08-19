package com.alibaba.sdk.android.oss.common.utils;

import static com.alibaba.sdk.android.oss.common.utils.OSSUtils.assertTrue;
import static com.alibaba.sdk.android.oss.signer.SignParameters.AUTHORIZATION_PREFIX;

import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.internal.RequestMessage;
import com.alibaba.sdk.android.oss.signer.ServiceSignature;
import com.alibaba.sdk.android.oss.signer.SignParameters;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class SignUtils {

    public static String composeRequestAuthorization(String accessKeyId, String signature) {
        return AUTHORIZATION_PREFIX + accessKeyId + ":" + signature;
    }

    public static String buildCanonicalString(String method, String resourcePath, RequestMessage request,
                                              String expires) {

        StringBuilder canonicalString = new StringBuilder();
        canonicalString.append(method).append(SignParameters.NEW_LINE);

        Map<String, String> headers = request.getHeaders();
        TreeMap<String, String> headersToSign = new TreeMap<String, String>();

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (header.getKey() == null) {
                    continue;
                }

                String lowerKey = header.getKey().toLowerCase();
                if (lowerKey.equals(HttpHeaders.CONTENT_TYPE.toLowerCase())
                        || lowerKey.equals(HttpHeaders.CONTENT_MD5.toLowerCase())
                        || lowerKey.equals(HttpHeaders.DATE.toLowerCase())
                        || lowerKey.startsWith(OSSHeaders.OSS_PREFIX)) {
                    headersToSign.put(lowerKey, header.getValue().trim());
                }
            }
        }

        if (!headersToSign.containsKey(HttpHeaders.CONTENT_TYPE.toLowerCase())) {
            headersToSign.put(HttpHeaders.CONTENT_TYPE.toLowerCase(), "");
        }
        if (!headersToSign.containsKey(HttpHeaders.CONTENT_MD5.toLowerCase())) {
            headersToSign.put(HttpHeaders.CONTENT_MD5.toLowerCase(), "");
        }

        // Append all headers to sign to canonical string
        for (Map.Entry<String, String> entry : headersToSign.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.startsWith(OSSHeaders.OSS_PREFIX)) {
                canonicalString.append(key).append(':').append(value);
            } else {
                canonicalString.append(value);
            }

            canonicalString.append(SignParameters.NEW_LINE);
        }

        // Append canonical resource to canonical string
        canonicalString.append(buildCanonicalizedResource(resourcePath, request.getParameters()));

        return canonicalString.toString();
    }

    public static String buildCanonicalizedResource(String resourcePath, Map<String, String> parameters) {
        assertTrue(resourcePath.startsWith("/"), "Resource path should start with slash character");

        StringBuilder builder = new StringBuilder();
        builder.append(resourcePath);

        if (parameters != null) {
            String[] parameterNames = parameters.keySet().toArray(new String[parameters.size()]);
            Arrays.sort(parameterNames);

            char separator = '?';
            for (String paramName : parameterNames) {
                if (!SignParameters.SIGNED_PARAMTERS.contains(paramName)) {
                    continue;
                }

                builder.append(separator);
                builder.append(paramName);
                String paramValue = parameters.get(paramName);
                if (paramValue != null && !paramValue.trim().isEmpty()) {
                    builder.append("=").append(paramValue);
                }

                separator = '&';
            }
        }

        return builder.toString();
    }

    public static String buildSignature(String secretAccessKey, String httpMethod, String resourcePath, RequestMessage request) {
        String canonicalString = buildCanonicalString(httpMethod, resourcePath, request, null);
        return ServiceSignature.create().computeSignature(secretAccessKey, canonicalString);
    }
}
