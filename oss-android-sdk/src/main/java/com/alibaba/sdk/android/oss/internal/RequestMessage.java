package com.alibaba.sdk.android.oss.internal;

import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.common.HttpMethod;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpdnsMini;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.BucketLifecycleRule;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class RequestMessage extends HttpMessage {

    private URI service;
    private URI endpoint;
    private String bucketName;
    private String objectKey;
    private HttpMethod method;
    private boolean isAuthorizationRequired = true;
    private Map<String, String> parameters = new LinkedHashMap<String, String>();
    private boolean checkCRC64;
    private OSSCredentialProvider credentialProvider;
    private boolean httpDnsEnable = false;
    private boolean supportCnameEnable = false;
    private List<String> customCnameExcludeList = new ArrayList<String>();
    private Boolean pathStyleAccessEnable = false;
    private Boolean customPathPrefixEnable = false;
    private String ipWithHeader;
    private boolean isInCustomCnameExcludeList = false;

    private String uploadFilePath;
    private byte[] uploadData;
    private Uri uploadUri;

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public OSSCredentialProvider getCredentialProvider() {
        return credentialProvider;
    }

    public void setCredentialProvider(OSSCredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public URI getService() {
        return service;
    }

    public void setService(URI service) {
        this.service = service;
    }

    public URI getEndpoint() {
        return endpoint;
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

    public Uri getUploadUri() {
        return uploadUri;
    }

    public void setUploadUri(Uri uploadUri) {
        this.uploadUri = uploadUri;
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

    public String getIpWithHeader() {
        return ipWithHeader;
    }

    public void setIpWithHeader(String ipWithHeader) {
        this.ipWithHeader = ipWithHeader;
    }

    public Boolean getPathStyleAccessEnable() {
        return pathStyleAccessEnable;
    }

    public void setPathStyleAccessEnable(Boolean pathStyleAccessEnable) {
        this.pathStyleAccessEnable = pathStyleAccessEnable;
    }

    public Boolean getCustomPathPrefixEnable() {
        return customPathPrefixEnable;
    }

    public void setCustomPathPrefixEnable(Boolean customPathPrefixEnable) {
        this.customPathPrefixEnable = customPathPrefixEnable;
    }

    public boolean isSupportCnameEnable() {
        return supportCnameEnable;
    }

    public void setSupportCnameEnable(boolean supportCnameEnable) {
        this.supportCnameEnable = supportCnameEnable;
    }

    public List<String> getCustomCnameExcludeList() {
        return customCnameExcludeList;
    }

    public void setCustomCnameExcludeList(List<String> customCnameExcludeList) {
        this.customCnameExcludeList = customCnameExcludeList;
    }

    public void createBucketRequestBodyMarshall(Map<String, String> configures) throws UnsupportedEncodingException {
        StringBuffer xmlBody = new StringBuffer();
        if (configures != null) {
            xmlBody.append("<CreateBucketConfiguration>");
            for (Map.Entry<String, String> entry : configures.entrySet()) {
                xmlBody.append("<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">");
            }
            xmlBody.append("</CreateBucketConfiguration>");
            byte[] binaryData = xmlBody.toString().getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
            long length = binaryData.length;
            InputStream inStream = new ByteArrayInputStream(binaryData);
            setContent(inStream);
            setContentLength(length);
        }
    }

    public void putBucketRefererRequestBodyMarshall(ArrayList<String> referers, boolean allowEmpty) throws UnsupportedEncodingException {
        StringBuffer xmlBody = new StringBuffer();
        xmlBody.append("<RefererConfiguration>");
        xmlBody.append("<AllowEmptyReferer>" + (allowEmpty ? "true" : "false") + "</AllowEmptyReferer>");
        if (referers != null && referers.size() > 0) {
            xmlBody.append("<RefererList>");
            for (String referer : referers) {
                xmlBody.append("<Referer>" + referer + "</Referer>");
            }
            xmlBody.append("</RefererList>");
        }
        xmlBody.append("</RefererConfiguration>");

        byte[] binaryData = xmlBody.toString().getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
        long length = binaryData.length;
        InputStream inStream = new ByteArrayInputStream(binaryData);
        setContent(inStream);
        setContentLength(length);
    }

    public void putBucketLoggingRequestBodyMarshall(String targetBucketName, String targetPrefix) throws UnsupportedEncodingException {
        StringBuffer xmlBody = new StringBuffer();
        xmlBody.append("<BucketLoggingStatus>");
        if (targetBucketName != null) {
            xmlBody.append("<LoggingEnabled><TargetBucket>" + targetBucketName + "</TargetBucket>");
            if (targetPrefix != null) {
                xmlBody.append("<TargetPrefix>" + targetPrefix + "</TargetPrefix>");
            }
            xmlBody.append("</LoggingEnabled>");
        }

        xmlBody.append("</BucketLoggingStatus>");

        byte[] binaryData = xmlBody.toString().getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
        long length = binaryData.length;
        InputStream inStream = new ByteArrayInputStream(binaryData);
        setContent(inStream);
        setContentLength(length);
    }

    public void putBucketLifecycleRequestBodyMarshall(ArrayList<BucketLifecycleRule> lifecycleRules) throws UnsupportedEncodingException {
        StringBuffer xmlBody = new StringBuffer();
        xmlBody.append("<LifecycleConfiguration>");
        for (BucketLifecycleRule rule : lifecycleRules) {
            xmlBody.append("<Rule>");
            if (rule.getIdentifier() != null) {
                xmlBody.append("<ID>" + rule.getIdentifier() + "</ID>");
            }
            if (rule.getPrefix() != null) {
                xmlBody.append("<Prefix>" + rule.getPrefix() + "</Prefix>");
            }
            xmlBody.append("<Status>" + (rule.getStatus() ? "Enabled": "Disabled") + "</Status>");
            if (rule.getDays() != null) {
                xmlBody.append("<Days>" + rule.getDays() + "</Days>");
            } else if (rule.getExpireDate() != null) {
                xmlBody.append("<Date>" + rule.getExpireDate() + "</Date>");
            }

            if (rule.getMultipartDays() != null) {
                xmlBody.append("<AbortMultipartUpload><Days>" + rule.getMultipartDays() + "</Days></AbortMultipartUpload>");
            } else if (rule.getMultipartExpireDate() != null) {
                xmlBody.append("<AbortMultipartUpload><Date>" + rule.getMultipartDays() + "</Date></AbortMultipartUpload>");
            }

            if (rule.getIADays() != null) {
                xmlBody.append("<Transition><Days>" + rule.getIADays() + "</Days><StorageClass>IA</StorageClass></Transition>");
            } else if (rule.getIAExpireDate() != null) {
                xmlBody.append("<Transition><Date>" + rule.getIAExpireDate() + "</Date><StorageClass>IA</StorageClass></Transition>");
            } else if (rule.getArchiveDays() != null) {
                xmlBody.append("<Transition><Days>" + rule.getArchiveDays() + "</Days><StorageClass>Archive</StorageClass></Transition>");
            } else if (rule.getArchiveExpireDate() != null) {
                xmlBody.append("<Transition><Date>" + rule.getArchiveExpireDate() + "</Date><StorageClass>Archive</StorageClass></Transition>");
            }

            xmlBody.append("</Rule>");
        }

        xmlBody.append("</LifecycleConfiguration>");

        byte[] binaryData = xmlBody.toString().getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
        long length = binaryData.length;
        InputStream inStream = new ByteArrayInputStream(binaryData);
        setContent(inStream);
        setContentLength(length);
    }

    public byte[] deleteMultipleObjectRequestBodyMarshall(List<String> objectKeys, boolean isQuiet) throws UnsupportedEncodingException {
        StringBuffer xmlBody = new StringBuffer();
        xmlBody.append("<Delete>");
        if (isQuiet) {
            xmlBody.append("<Quiet>true</Quiet>");
        } else {
            xmlBody.append("<Quiet>false</Quiet>");
        }
        for (String key : objectKeys) {
            xmlBody.append("<Object>");
            xmlBody.append("<Key>").append(key).append("</Key>");
            xmlBody.append("</Object>");
        }
        xmlBody.append("</Delete>");
        byte[] binaryData = xmlBody.toString().getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
        long length = binaryData.length;
        InputStream inStream = new ByteArrayInputStream(binaryData);
        setContent(inStream);
        setContentLength(length);
        return binaryData;
    }

    public String buildOSSServiceURL() {
        OSSUtils.assertTrue(service != null, "Service haven't been set!");
        String originHost = service.getHost();
        String scheme = service.getScheme();

        String urlHost = null;
        if (isHttpDnsEnable() && scheme.equalsIgnoreCase("http")) {
            urlHost = HttpdnsMini.getInstance().getIpByHostAsync(originHost);
        } else {
            OSSLog.logDebug("[buildOSSServiceURL], disable httpdns or http is not need httpdns");
        }
        if (urlHost == null) {
            urlHost = originHost;
        }

        getHeaders().put(OSSHeaders.HOST, originHost);

        String baseURL = scheme + "://" + urlHost;
        String queryString = OSSUtils.paramToQueryString(this.parameters, OSSConstants.DEFAULT_CHARSET_NAME);

        if (OSSUtils.isEmptyString(queryString)) {
            return baseURL;
        } else {
            return baseURL + "?" + queryString;
        }
    }

    public String buildCanonicalURL() throws Exception{
        OSSUtils.assertTrue(endpoint != null, "Endpoint haven't been set!");

        String scheme = endpoint.getScheme();
        String originHost = endpoint.getHost();
        String portString = null;
        String path = endpoint.getPath();

        int port = endpoint.getPort();
        if (port != -1) {
            portString = String.valueOf(port);
        }

        if (TextUtils.isEmpty(originHost)){
            String url = endpoint.toString();
            OSSLog.logDebug("endpoint url : " + url);
//            originHost = url.substring((scheme + "://").length(),url.length());
        }

        OSSLog.logDebug(" scheme : " + scheme);
        OSSLog.logDebug(" originHost : " + originHost);
        OSSLog.logDebug(" port : " + portString);

        String canonicalHost = buildCanonicalHost(endpoint, bucketName, pathStyleAccessEnable, supportCnameEnable, customCnameExcludeList);
        String canonicalPath = pathStyleAccessEnable ? buildCanonicalPath(bucketName, objectKey) : buildCanonicalPath(objectKey);
        String queryString = OSSUtils.paramToQueryString(this.parameters, OSSConstants.DEFAULT_CHARSET_NAME);

        StringBuffer baseURL = new StringBuffer();
        if (!OSSUtils.isEmptyString(scheme)) {
            baseURL.append(scheme).append("://");
        }
        if (!OSSUtils.isEmptyString(canonicalHost)) {
            baseURL.append(canonicalHost);
        }
        if (!OSSUtils.isEmptyString(portString)) {
            baseURL.append(":").append(portString);
        }
        if (customPathPrefixEnable && !OSSUtils.isEmptyString(path)) {
            baseURL.append(path);
        }
        if (!OSSUtils.isEmptyString(canonicalPath)) {
            baseURL.append("/").append(canonicalPath);
        }
        if (!OSSUtils.isEmptyString(queryString)) {
            baseURL.append("?").append(queryString);
        }

        //输入请求信息日志
        StringBuilder printReq = new StringBuilder();
        printReq.append("request---------------------\n");
        printReq.append("request url=" + baseURL.toString() + "\n");
        printReq.append("request params=" + queryString + "\n");
        for (String key : getHeaders().keySet()) {
            printReq.append("requestHeader [" + key + "]: ").append(getHeaders().get(key) + "\n");
        }
        OSSLog.logDebug(printReq.toString());

        return baseURL.toString();
    }

    private String buildCanonicalHost(URI endpoint, String bucket, Boolean pathStyleAccessEnable, boolean supportCnameEnable, List<String> cnameExcludeList) {
        StringBuffer host = new StringBuffer();
        String originHost = endpoint.getHost();

        if (originHost == null) {
            throw new IllegalArgumentException("Host name can not be null.");
        }
        Boolean isCname = (supportCnameEnable && cnameExcludeFilter(originHost, cnameExcludeList));
        if (bucket != null && !isCname && !pathStyleAccessEnable) {
            host.append(bucket).append(".").append(originHost);
        } else {
            host.append(originHost);
        }
        return host.toString();
    }

    private boolean cnameExcludeFilter(String host, List<String> cnameExcludeList) {
        if (host != null && !host.trim().isEmpty()) {
            String canonicalHost = host.toLowerCase();
            for (String cnameExclude : cnameExcludeList) {
                if (canonicalHost.endsWith(cnameExclude)) {
                    return false;
                }
            }
            return true;
        }
        throw new IllegalArgumentException("Host name can not be null.");
    }

    private String buildCanonicalPath(String key) {
        return key != null ? HttpUtil.urlEncode(key, OSSConstants.DEFAULT_CHARSET_NAME) : null;
    }

    private String buildCanonicalPath(String bucket, String key) {
        if (bucket != null) {
            return bucket + "/" + (key != null ? HttpUtil.urlEncode(key, OSSConstants.DEFAULT_CHARSET_NAME) : "");
        } else {
            return null;
        }
    }
}
