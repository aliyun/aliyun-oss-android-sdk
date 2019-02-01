package com.alibaba.sdk.android.oss.internal;

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

    private boolean isInCustomCnameExcludeList = false;

    private String uploadFilePath;
    private byte[] uploadData;

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

        int port = endpoint.getPort();
        if (port != -1) {
            portString = String.valueOf(port);
        }

        if (TextUtils.isEmpty(originHost)){
            String url = endpoint.toString();
            OSSLog.logDebug("endpoint url : " + url);
            originHost = url.substring((scheme + "://").length(),url.length());
        }

        OSSLog.logDebug(" scheme : " + scheme);
        OSSLog.logDebug(" originHost : " + originHost);
        OSSLog.logDebug(" port : " + portString);

        String baseURL = endpoint.toString();

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
            }else if (OSSUtils.isValidateIP(originHost)) {
                // ip address
                baseURL = endpoint.toString() + "/" + bucketName;
            } else {
                // cname时不做任何处理
                baseURL = endpoint.toString();
            }
        } else {
            baseURL = endpoint.toString();
        }

        /*
         * edited by wangzheng.
         * 重新整理url build 逻辑。如果是标准阿里云的域名。通过bucket拼装获取实际访问url。
         * 否则，直接用用户传入的自定义域名或者ip链接object 进行访问。
         */
//        if (OSSUtils.isOssOriginHost(originHost) && !TextUtils.isEmpty(bucketName)){
//            originHost = bucketName + "." + originHost;
//            String urlHost = null;
//            if (isHttpDnsEnable()) {
//                urlHost = HttpdnsMini.getInstance().getIpByHostAsync(originHost);
//            } else {
//                OSSLog.logDebug("[buildCannonicalURL], disable httpdns");
//            }
//            // The urlHost is null when the asynchronous DNS resolution API never returns IP.
//            if (urlHost == null) {
//                urlHost = originHost;
//            }
//            String headerHost = originHost;

            // isCname and isOssOriginHost are mutually exclusive, so code below will never be executed!
//            if (OSSUtils.isCname(originHost) && this.isInCustomCnameExcludeList() && !TextUtils.isEmpty(bucketName)) {
//                headerHost = bucketName + "." + originHost;
//            }
//            addHeader(OSSHeaders.HOST, originHost);
//            baseURL = scheme + "://" + urlHost;
//
//        } else {
//            baseURL = scheme + "://" + originHost;
//        }

//        if (OSSUtils.isValidateIP(originHost)) {
//            baseURL = scheme + "://" + originHost + "/" + bucketName;
//        } else {
//            // If it'd not a CName or it's in the CName exclude list, the host should be prefixed with the bucket name.
//            if (!OSSUtils.isCname(originHost) && bucketName != null) {
//                originHost = bucketName + "." + originHost;
//            }
//
//            String urlHost = null;
//            if (isHttpDnsEnable()) {
//                urlHost = HttpdnsMini.getInstance().getIpByHostAsync(originHost);
//            } else {
//                OSSLog.logDebug("[buildCannonicalURL], disable httpdns");
//            }
//
//            // The urlHost is null when the asynchronous DNS resolution API never returns IP.
//            if (urlHost == null) {
//                urlHost = originHost;
//            }
//
//            String headerHost = originHost;
//            if (OSSUtils.isCname(originHost) && this.isInCustomCnameExcludeList() && bucketName != null) {
//                headerHost = bucketName + "." + originHost;
//            }
//            addHeader(OSSHeaders.HOST, headerHost);
//            baseURL = scheme + "://" + urlHost;
//        }

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
