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
public class RequestMessage extends HttpMessage {

    private URI endpoint;
    private String bucketName;
    private String objectKey;
    private HttpMethod method;
    private boolean isAuthorizationRequired = true;
    private Map<String, String> parameters = new LinkedHashMap<String, String>();
    private boolean checkCRC64;
    private OSSCredentialProvider credentialProvider;
    private boolean httpDnsEnable = false;

    private boolean isInCustomCnameExcludeList = false;

    private String uploadFilePath;
    private byte[] uploadData;

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

    public boolean isHttpDnsEnable() {
        return httpDnsEnable;
    }

    public void setHttpDnsEnable(boolean httpDnsEnable) {
        this.httpDnsEnable = httpDnsEnable;
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

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public byte[] getUploadData() {
        return uploadData;
    }

    public void setUploadData(byte[] uploadData) {
        this.uploadData = uploadData;
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

    public boolean isCheckCRC64() {
        return checkCRC64;
    }

    public void setCheckCRC64(boolean checkCRC64) {
        this.checkCRC64 = checkCRC64;
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
            setContent(inStream);
            setContentLength(length);
        }
    }

    public String buildCanonicalURL() {
        OSSUtils.assertTrue(endpoint != null, "Endpoint haven't been set!");

        String baseURL;

        String scheme = endpoint.getScheme();
        String originHost = endpoint.getHost();

        if (OSSUtils.isIP(originHost)){
            baseURL = scheme + "://" + originHost + "/" + bucketName;
        }else{
            // If it'd not a CName or it's in the CName exclude list, the host should be prefixed with the bucket name.
            if (!OSSUtils.isCname(originHost) && bucketName != null) {
                originHost = bucketName + "." + originHost;
            }

            String urlHost = null;
            if (isHttpDnsEnable()) {
                urlHost = HttpdnsMini.getInstance().getIpByHostAsync(originHost);
            } else {
                OSSLog.logDebug("[buildCannonicalURL], disable httpdns");
            }

            // The urlHost is null when the asynchronous DNS resolution API never returns IP.
            if (urlHost == null) {
                urlHost = originHost;
            }

            String headerHost = originHost;
            if (OSSUtils.isCname(originHost) && this.isInCustomCnameExcludeList() && bucketName != null) {
                headerHost = bucketName + "." + originHost;
            }
            addHeader(OSSHeaders.HOST, headerHost);
            baseURL = scheme + "://" + urlHost;
        }

        if (objectKey != null) {
            baseURL += "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME);
        }

        String queryString = OSSUtils.paramToQueryString(this.parameters, OSSConstants.DEFAULT_CHARSET_NAME);

        //输入请求信息日志
        StringBuilder printReq = new StringBuilder();
        printReq.append("request---------------------\n");
        printReq.append("request url=" + baseURL + "\n");
        printReq.append("request params=" + queryString + "\n");
        for (String key : getHeaders().keySet()) {
            printReq.append("requestHeader [" + key + "]: ").append(getHeaders().get(key) + "\n");
        }
        OSSLog.logDebug(printReq.toString());

        if (OSSUtils.isEmptyString(queryString)) {
            return baseURL;
        } else {
            return baseURL + "?" + queryString;
        }
    }
}
