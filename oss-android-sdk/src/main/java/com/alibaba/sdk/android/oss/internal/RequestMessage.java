package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.common.HttpMethod;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpdnsMini;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class RequestMessage {

    private URI endpoint;
    private String bucketName;
    private String objectKey;
    private HttpMethod method;
    private boolean isAuthorizationRequired = true;
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, String> parameters = new LinkedHashMap<String, String>();

    private OSSCredentialProvider credentialProvider;
    private boolean isHttpdnsEnable = true;

    private boolean isInCustomCnameExcludeList = false;

    private byte[] uploadData;
    private String uploadFilePath;
    private InputStream uploadInputStream;
    private long readStreamLength;

    private String downloadFilePath;

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public OSSCredentialProvider getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(OSSCredentialProvider credentialProvider) {
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

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
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

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public byte[] getUploadData() {
        return uploadData;
    }

    public void setUploadData(byte[] uploadData) {
        this.uploadData = uploadData;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public String getDownloadFilePath() {
        return downloadFilePath;
    }

    public void setDownloadFilePath(String downloadFilePath) {
        this.downloadFilePath = downloadFilePath;
    }

    public boolean isAuthorizationRequired() {
        return isAuthorizationRequired;
    }

    public void setIsAuthorizationRequired(boolean isAuthorizationRequired) {
        this.isAuthorizationRequired = isAuthorizationRequired;
    }

    public boolean isInCustomCnameExcludeList() {
        return isInCustomCnameExcludeList;
    }

    public void setIsInCustomCnameExcludeList(boolean isInExcludeCnameList) {
        this.isInCustomCnameExcludeList = isInExcludeCnameList;
    }

    public void setUploadInputStream(InputStream uploadInputStream, long inputLength) {
        if (uploadInputStream != null) {
            this.uploadInputStream = uploadInputStream;
            this.readStreamLength = inputLength;
        }
    }

    public InputStream getUploadInputStream() {
        return uploadInputStream;
    }

    public void createBucketRequestBodyMarshall(String locationConstraint) throws UnsupportedEncodingException {
        StringBuffer xmlBody = new StringBuffer();
        if (locationConstraint != null) {
            xmlBody.append("<CreateBucketConfiguration>");
            xmlBody.append("<LocationConstraint>" + locationConstraint + "</LocationConstraint>");
            xmlBody.append("</CreateBucketConfiguration>");
            byte[] binaryData = xmlBody.toString().getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
            long length = binaryData.length;
            InputStream inStream = new ByteArrayInputStream(binaryData);
            setUploadInputStream(inStream, length);
        }
    }

    public long getReadStreamLength() {
        return readStreamLength;
    }

    public String buildCanonicalURL() {
        OSSUtils.assertTrue(endpoint != null, "Endpoint haven't been set!");

        String baseURL;

        String scheme = endpoint.getScheme();
        String originHost = endpoint.getHost();

        // 如果不是Cname，或者用户在clientconfiguration中规定它不是cname，就要加上bucketName作为host
        if (!OSSUtils.isCname(originHost) && bucketName != null) {
            originHost = bucketName + "." + originHost;
        }

        String urlHost = null;
        if (isHttpdnsEnable) {
            urlHost = HttpdnsMini.getInstance().getIpByHostAsync(originHost);
        } else {
            OSSLog.logD("[buildCannonicalURL] - proxy exist, disable httpdns");
        }

        // 异步调用HTTPDNS解析IP，如果还没解析到结果，也是返回null
        if (urlHost == null) {
            urlHost = originHost;
        }

        String headerHost = originHost;
        if (OSSUtils.isCname(originHost) && this.isInCustomCnameExcludeList() && bucketName != null) {
            headerHost = bucketName + "." + originHost;
        }

        headers.put(OSSHeaders.HOST, headerHost);

        baseURL = scheme + "://" + urlHost;
        if (objectKey != null) {
            baseURL += "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME);
        }

        String queryString = OSSUtils.paramToQueryString(this.parameters, OSSConstants.DEFAULT_CHARSET_NAME);

        //输入请求信息日志
        StringBuilder printReq = new StringBuilder();
        printReq.append("请求参数---------------------\n");
        printReq.append("request url="+baseURL+"\n");
        printReq.append("request params="+queryString+"\n");
        for(String key : headers.keySet()){
            printReq.append("requestHeader ["+key+"]: ").append(headers.get(key)+"\n");
        }
        OSSLog.logD(printReq.toString());

        if (OSSUtils.isEmptyString(queryString)) {
            return baseURL;
        } else {
            return baseURL + "?" + queryString;
        }
    }
}
