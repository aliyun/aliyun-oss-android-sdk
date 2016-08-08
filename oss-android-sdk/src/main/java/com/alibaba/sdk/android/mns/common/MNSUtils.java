package com.alibaba.sdk.android.mns.common;

/**
 * Created by pan.zengp on 2016/7/27.
 */

import android.util.Base64;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import com.alibaba.sdk.android.common.auth.CredentialProvider;
import com.alibaba.sdk.android.common.auth.CustomSignerCredentialProvider;
import com.alibaba.sdk.android.common.auth.FederationCredentialProvider;
import com.alibaba.sdk.android.common.auth.FederationToken;
import com.alibaba.sdk.android.common.auth.HmacSHA1Signature;
import com.alibaba.sdk.android.common.auth.PlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.common.auth.StsTokenCredentialProvider;
import com.alibaba.sdk.android.common.utils.HttpUtil;
import com.alibaba.sdk.android.mns.internal.RequestMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MNSUtils{
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.length() == 0;
    }

    public static String paramToQueryString(Map<String, String> params, String charset) {

        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder paramString = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> p : params.entrySet()) {
            String key = p.getKey();
            String value = p.getValue();

            if (!first) {
                paramString.append("&");
            }

            // Urlencode each request parameter
            paramString.append(HttpUtil.urlEncode(key, charset));
            if (!isEmptyString(value)) {
                paramString.append("=").append(HttpUtil.urlEncode(value, charset));
            }

            first = false;
        }

        return paramString.toString();
    }

    public static void signRequest(RequestMessage message) throws IOException{
        if (message.getCredentialProvider() == null) {
            throw new IllegalStateException("当前CredentialProvider为空！！！");
        }

        CredentialProvider credentialProvider = message.getCredentialProvider();
        FederationToken federationToken = null;

        if (credentialProvider instanceof FederationCredentialProvider) {
            federationToken = ((FederationCredentialProvider) credentialProvider).getValidFederationToken();
            if (federationToken == null) {
                MNSLog.logE("Can't get a federation token");
                throw new IOException("Can't get a federation token");
            }
            message.getHeaders().put(MNSHeaders.MNS_SECURITY_TOKEN, federationToken.getSecurityToken());
        } else if (credentialProvider instanceof StsTokenCredentialProvider) {
            federationToken = ((StsTokenCredentialProvider) credentialProvider).getFederationToken();
            message.getHeaders().put(MNSHeaders.MNS_SECURITY_TOKEN, federationToken.getSecurityToken());
        }

        String method = message.getMethod().toString();
        String contentMD5 = message.getHeaders().get(MNSHeaders.CONTENT_MD5);
        if (contentMD5 == null) {
            contentMD5 = "";
        }
        String contentType = message.getHeaders().get(MNSHeaders.CONTENT_TYPE);
        if (contentType == null) {
            contentType = "";
        }
        String dateString = message.getHeaders().get(MNSHeaders.DATE);

        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        for (String key: message.getHeaders().keySet()) {
            if (key.toLowerCase().startsWith("x-mns-")) {
                list.add(new Pair<String, String>(key.toLowerCase(), message.getHeaders().get(key)));
            }
        }
        Collections.sort(list, new Comparator<Pair<String, String>>() {

            @Override
            public int compare(Pair<String, String> lhs, Pair<String, String> rhs) {
                String k1 = lhs.first;
                String k2 = rhs.first;
                return k1.compareTo(k2);
            }
        });

        StringBuilder sb = new StringBuilder();
        Pair<String, String> previous = null;
        for (Pair<String, String> curr : list) {
            if (previous == null) {
                sb.append(curr.first + ":" + curr.second);
            } else {
                if (previous.first.equals(curr.first)) {
                    sb.append("," + curr.second);
                } else {
                    sb.append("\n" + curr.first + ":" + curr.second);
                }
            }
            previous = curr;
        }
        String canonicalizedHeader = sb.toString();
        if (!MNSUtils.isEmptyString(canonicalizedHeader)) {
            canonicalizedHeader = canonicalizedHeader.trim();
            canonicalizedHeader += "\n";
        }

        String canonicalizedResource = message.getResourcePath();

        String contentToSign = String.format("%s\n%s\n%s\n%s\n%s%s",
                method, contentMD5, contentType, dateString, canonicalizedHeader, canonicalizedResource);
        MNSLog.logI(contentToSign);

        String signature = "---initValue---";

        if (credentialProvider instanceof FederationCredentialProvider ||
                credentialProvider instanceof StsTokenCredentialProvider) {
            signature = MNSUtils.sign(federationToken.getTempAK(), federationToken.getTempSK(), contentToSign);
        } else if (credentialProvider instanceof PlainTextAKSKCredentialProvider) {
            signature = MNSUtils.sign(((PlainTextAKSKCredentialProvider) credentialProvider).getAccessKeyId(),
                    ((PlainTextAKSKCredentialProvider) credentialProvider).getAccessKeySecret(), contentToSign);
        } else if (credentialProvider instanceof CustomSignerCredentialProvider) {
            signature = ((CustomSignerCredentialProvider) credentialProvider).signContent(contentToSign);
        }

        MNSLog.logD("signed content: " + contentToSign.replaceAll("\n", "@") + "   ---------   signature: " + signature);

        message.getHeaders().put(MNSHeaders.AUTHORIZATION, signature);
    }

    public static String sign(String accessKey, String screctKey, String content) {

        String signature = null;

        try {
            signature = new HmacSHA1Signature().computeSignature(screctKey, content);
            signature = signature.trim();
        } catch (Exception e) {
            throw new IllegalStateException("Compute signature failed!", e);
        }

        return "MNS " + accessKey + ":" + signature;
    }
}