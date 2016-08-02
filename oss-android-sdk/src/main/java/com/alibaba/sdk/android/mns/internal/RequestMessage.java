package com.alibaba.sdk.android.mns.internal;

import com.alibaba.sdk.android.common.HttpMethod;
import com.alibaba.sdk.android.common.auth.CredentialProvider;
import com.alibaba.sdk.android.common.utils.HttpUtil;
import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.common.MNSHeaders;
import com.alibaba.sdk.android.mns.common.MNSLog;
import com.alibaba.sdk.android.mns.common.MNSUtils;
import com.alibaba.sdk.android.common.utils.HttpdnsMini;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by pan.zengp on 2016/7/4.
 */
public class RequestMessage {
    private URI endpoint;
    private String queueName;
    private HttpMethod method;
    private boolean isAuthorizationRequired = true;
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> parameters = new LinkedHashMap<String, String>();
    private String content;
    private long contentLength;
    private String resourcePath;
    private MNSConstants.MNSType type;

    private CredentialProvider credentialProvider;
    private boolean isHttpdnsEnable = true;

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public CredentialProvider getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    public boolean isHttpdnsEnable() {
        return isHttpdnsEnable;
    }

    public void setIsHttpdnsEnable(boolean isHttpdnsEnable) {
        this.isHttpdnsEnable = isHttpdnsEnable;
    }

    public String getQueueName(){
        return queueName;
    }

    public void setQueueName(String queueName){
        this.queueName = queueName;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers = headers;
        }
    }

    public void addHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers.putAll(headers);
        }
    }

    public boolean isAuthorizationRequired() {
        return isAuthorizationRequired;
    }

    public void setIsAuthorizationRequired(boolean isAuthorizationRequired) {
        this.isAuthorizationRequired = isAuthorizationRequired;
    }

    public String buildCanonicalURL() {
        MNSUtils.assertTrue(endpoint != null, "Endpoint haven't been set!");

        String baseURL;

        String scheme = endpoint.getScheme();
        String originHost = endpoint.getHost();

        String urlHost = null;
        if (isHttpdnsEnable) {
            urlHost = HttpdnsMini.getInstance().getIpByHostAsync(originHost);
        } else {
            MNSLog.logD("[buildCannonicalURL] - proxy exist, disable httpdns");
        }

        // 异步调用HTTPDNS解析IP，如果还没解析到结果，也是返回null
        if (urlHost == null) {
            urlHost = originHost;
        }

        String headerHost = originHost;

        headers.put(MNSHeaders.HOST, headerHost);

        baseURL = scheme + "://" + urlHost;

        switch (type)
        {
            case QUEUE:
                if (queueName != null) {
                    baseURL = baseURL + "/queues/" + queueName;
                    resourcePath = "/queues/" + queueName;
                }
                else {
                    baseURL = baseURL + "/queues";
                    resourcePath = "/queues";
                }
                break;
            case MESSAGE:
                baseURL = baseURL + "/queues/" + queueName + "/messages";
                resourcePath = "/queues/" + queueName + "/messages";
                break;
            default:
                break;
        }

        String queryString = MNSUtils.paramToQueryString(this.parameters, MNSConstants.DEFAULT_CHARSET_NAME);
        if (MNSUtils.isEmptyString(queryString)) {
            return baseURL;
        } else {
            resourcePath = resourcePath + "?" + queryString;
            return baseURL + "?" + queryString;
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) throws IOException {
        this.content = content;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setResourcePath(String resourcePath) { this.resourcePath = resourcePath; }

    public String getResourcePath() { return resourcePath; }

    public void setType(MNSConstants.MNSType type) { this.type = type; }

    public MNSConstants.MNSType getType() { return type; }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
