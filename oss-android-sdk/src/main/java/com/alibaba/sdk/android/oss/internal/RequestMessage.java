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
import com.alibaba.sdk.android.oss.signer.RequestSigner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private RequestSigner signer;
    private boolean httpDnsEnable = false;
    private boolean pathStyleAccessEnable = false;
    private boolean customPathPrefixEnable = false;
    private String ipWithHeader;
    private boolean isInCustomCnameExcludeList = false;
    /* Indicate whether using url signature */
    private boolean useUrlSignature = false;

    private String uploadFilePath;
    private byte[] uploadData;
    private Uri uploadUri;

    private Set<String> additionalHeaderNames = new HashSet<String>();

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

    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
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

    public boolean isPathStyleAccessEnable() {
        return pathStyleAccessEnable;
    }

    public void setPathStyleAccessEnable(boolean pathStyleAccessEnable) {
        this.pathStyleAccessEnable = pathStyleAccessEnable;
    }

    public boolean isCustomPathPrefixEnable() {
        return customPathPrefixEnable;
    }

    public void setCustomPathPrefixEnable(boolean customPathPrefixEnable) {
        this.customPathPrefixEnable = customPathPrefixEnable;
    }

    public boolean isUseUrlSignature() {
        return useUrlSignature;
    }

    public void setUseUrlSignature(boolean useUrlSignature) {
        this.useUrlSignature = useUrlSignature;
    }

    public Set<String> getAdditionalHeaderNames() {
        return additionalHeaderNames;
    }

    public void setAdditionalHeaderNames(Set<String> additionalHeaderNames) {
        this.additionalHeaderNames = additionalHeaderNames;
    }

    public RequestSigner getSigner() {
        return signer;
    }

    public void setSigner(RequestSigner signer) {
        this.signer = signer;
    }

    public void createBucketRequestBodyMarshall(Map<String, String> configures) throws UnsupportedEncodingException {
        StringBuffer xmlBody = new StringBuffer();
        if (configures != null) {
            xmlBody.append("<CreateBucketConfiguration>");
            for (Map.Entry<String, String> entry : configures.entrySet()) {
                xmlBody.append("<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">");
            }
            xmlBody.append("</CreateBucketConfiguration>");
            String StringBody = xmlBody.toString();
            setStringBody(StringBody);
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

        String StringBody = xmlBody.toString();
        setStringBody(StringBody);
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

        String StringBody = xmlBody.toString();
        setStringBody(StringBody);
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

        String StringBody = xmlBody.toString();
        setStringBody(StringBody);
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
            xmlBody.append("<Key>").append(OSSUtils.escapeKey(key)).append("</Key>");
            xmlBody.append("</Object>");
        }
        xmlBody.append("</Delete>");
        String StringBody = xmlBody.toString();
        byte[] binaryData = StringBody.getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
        setStringBody(StringBody);
        return binaryData;
    }

    public byte[] putObjectTaggingRequestBodyMarshall(Map<String, String> tags) throws UnsupportedEncodingException {
        StringBuffer xmlBody = new StringBuffer();
        xmlBody.append("<Tagging>");
        xmlBody.append("<TagSet>");
        if (tags != null) {
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                xmlBody.append("<Tag>");
                xmlBody.append("<Key>").append(tag.getKey()).append("</Key>");
                xmlBody.append("<Value>").append(tag.getValue()).append("</Value>");
                xmlBody.append("</Tag>");
            }
        }
        xmlBody.append("</TagSet>");
        xmlBody.append("</Tagging>");
        String StringBody = xmlBody.toString();
        byte[] binaryData = StringBody.getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
        setStringBody(StringBody);
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

    public String buildCanonicalURL() throws Exception {
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

        boolean isPathStyle = false;

        String baseURL = scheme + "://" + originHost;
        if(!TextUtils.isEmpty(portString)){
            baseURL += (":" + portString);
        }

        if (!TextUtils.isEmpty(bucketName)) {
            if (OSSUtils.isOssOriginHost(originHost)) {
                // official endpoint
                originHost = bucketName + "." + originHost;
                String urlHost = null;
                if (isHttpDnsEnable()) {
                    urlHost = HttpdnsMini.getInstance().getIpByHostAsync(originHost);
                } else {
                    OSSLog.logDebug("[buildCannonicalURL], disable httpdns");
                }
                addHeader(OSSHeaders.HOST, originHost);

                if (!TextUtils.isEmpty(urlHost)) {
                    baseURL = scheme + "://" + urlHost;
                } else {
                    baseURL = scheme + "://" + originHost;
                }
            } else if (isInCustomCnameExcludeList) {
                if (pathStyleAccessEnable) {
                    isPathStyle = true;
                } else {
                    baseURL = scheme + "://" + bucketName + "." + originHost;
                }
            } else if (OSSUtils.isValidateIP(originHost)) {
                // ip address
                if (OSSUtils.isEmptyString(ipWithHeader)) {
                    isPathStyle = true;
                } else {
                    addHeader(OSSHeaders.HOST, getIpWithHeader());
                }
            }
        }

        if (customPathPrefixEnable && path != null) {
            baseURL += path;
        }

        if (isPathStyle) {
            baseURL += ("/" + bucketName);
        }

        if (!TextUtils.isEmpty(objectKey)) {
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
