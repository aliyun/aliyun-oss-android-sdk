package com.alibaba.sdk.android.oss.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.HmacSHA1Signature;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.exception.InconsistentException;
import com.alibaba.sdk.android.oss.internal.RequestMessage;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.GetBucketInfoRequest;
import com.alibaba.sdk.android.oss.model.DeleteMultipleObjectRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.ListBucketsRequest;
import com.alibaba.sdk.android.oss.model.ListMultipartUploadsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PartETag;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.sdk.android.oss.common.RequestParameters.DELIMITER;
import static com.alibaba.sdk.android.oss.common.RequestParameters.ENCODING_TYPE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.KEY_MARKER;
import static com.alibaba.sdk.android.oss.common.RequestParameters.MARKER;
import static com.alibaba.sdk.android.oss.common.RequestParameters.MAX_KEYS;
import static com.alibaba.sdk.android.oss.common.RequestParameters.MAX_UPLOADS;
import static com.alibaba.sdk.android.oss.common.RequestParameters.PART_NUMBER;
import static com.alibaba.sdk.android.oss.common.RequestParameters.POSITION;
import static com.alibaba.sdk.android.oss.common.RequestParameters.PREFIX;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CACHE_CONTROL;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CONTENT_DISPOSITION;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CONTENT_ENCODING;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CONTENT_LANGUAGE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CONTENT_TYPE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_EXPIRES;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SECURITY_TOKEN;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_BUCKETINFO;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_ACL;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_APPEND;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_CORS;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_DELETE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_LIFECYCLE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_LOCATION;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_LOGGING;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_REFERER;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_SEQUENTIAL;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_UPLOADS;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_WEBSITE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.UPLOAD_ID;
import static com.alibaba.sdk.android.oss.common.RequestParameters.UPLOAD_ID_MARKER;
import static com.alibaba.sdk.android.oss.common.RequestParameters.X_OSS_PROCESS;
import static com.alibaba.sdk.android.oss.common.RequestParameters.X_OSS_RESTORE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.X_OSS_SYMLINK;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSUtils {

    private static final String NEW_LINE = "\n";

    private static final List<String> SIGNED_PARAMTERS = Arrays.asList(new String[]{
            SUBRESOURCE_BUCKETINFO, SUBRESOURCE_ACL, SUBRESOURCE_UPLOADS, SUBRESOURCE_LOCATION,
            SUBRESOURCE_CORS, SUBRESOURCE_LOGGING, SUBRESOURCE_WEBSITE,
            SUBRESOURCE_REFERER, SUBRESOURCE_LIFECYCLE, SUBRESOURCE_DELETE,
            SUBRESOURCE_APPEND, UPLOAD_ID, PART_NUMBER, SECURITY_TOKEN, POSITION,
            RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_HEADER_CONTENT_DISPOSITION,
            RESPONSE_HEADER_CONTENT_ENCODING, RESPONSE_HEADER_CONTENT_LANGUAGE,
            RESPONSE_HEADER_CONTENT_TYPE, RESPONSE_HEADER_EXPIRES, X_OSS_PROCESS,
            SUBRESOURCE_SEQUENTIAL, X_OSS_SYMLINK, X_OSS_RESTORE
    });

    /**
     * Populate metadata to headers.
     */
    public static void populateRequestMetadata(Map<String, String> headers, ObjectMetadata metadata) {
        if (metadata == null) {
            return;
        }

        Map<String, Object> rawMetadata = metadata.getRawMetadata();
        if (rawMetadata != null) {
            for (Map.Entry<String, Object> entry : rawMetadata.entrySet()) {
                headers.put(entry.getKey(), entry.getValue().toString());
            }
        }

        Map<String, String> userMetadata = metadata.getUserMetadata();
        if (userMetadata != null) {
            for (Map.Entry<String, String> entry : userMetadata.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null) key = key.trim();
                if (value != null) value = value.trim();
                headers.put(key, value);
            }
        }
    }

    public static void populateListBucketRequestParameters(ListBucketsRequest listBucketsRequest,
                                                           Map<String, String> params) {
        if (listBucketsRequest.getPrefix() != null) {
            params.put(PREFIX, listBucketsRequest.getPrefix());
        }

        if (listBucketsRequest.getMarker() != null) {
            params.put(MARKER, listBucketsRequest.getMarker());
        }

        if (listBucketsRequest.getMaxKeys() != null) {
            params.put(MAX_KEYS, Integer.toString(listBucketsRequest.getMaxKeys()));
        }
    }

    public static void populateListObjectsRequestParameters(ListObjectsRequest listObjectsRequest,
                                                            Map<String, String> params) {

        if (listObjectsRequest.getPrefix() != null) {
            params.put(PREFIX, listObjectsRequest.getPrefix());
        }

        if (listObjectsRequest.getMarker() != null) {
            params.put(MARKER, listObjectsRequest.getMarker());
        }

        if (listObjectsRequest.getDelimiter() != null) {
            params.put(DELIMITER, listObjectsRequest.getDelimiter());
        }

        if (listObjectsRequest.getMaxKeys() != null) {
            params.put(MAX_KEYS, Integer.toString(listObjectsRequest.getMaxKeys()));
        }

        if (listObjectsRequest.getEncodingType() != null) {
            params.put(ENCODING_TYPE, listObjectsRequest.getEncodingType());
        }
    }

    public static void populateListMultipartUploadsRequestParameters(ListMultipartUploadsRequest request,
                                                                     Map<String, String> params) {

        if (request.getDelimiter() != null) {
            params.put(DELIMITER, request.getDelimiter());
        }

        if (request.getMaxUploads() != null) {
            params.put(MAX_UPLOADS, Integer.toString(request.getMaxUploads()));
        }

        if (request.getKeyMarker() != null) {
            params.put(KEY_MARKER, request.getKeyMarker());
        }

        if (request.getPrefix() != null) {
            params.put(PREFIX, request.getPrefix());
        }

        if (request.getUploadIdMarker() != null) {
            params.put(UPLOAD_ID_MARKER, request.getUploadIdMarker());
        }

        if (request.getEncodingType() != null) {
            params.put(ENCODING_TYPE, request.getEncodingType());
        }
    }

    public static boolean checkParamRange(long param, long from, boolean leftInclusive,
                                          long to, boolean rightInclusive) {
        if (leftInclusive && rightInclusive) {    // [from, to]
            if (from <= param && param <= to) {
                return true;
            } else {
                return false;
            }
        } else if (leftInclusive && !rightInclusive) {  // [from, to)
            if (from <= param && param < to) {
                return true;
            } else {
                return false;
            }
        } else if (!leftInclusive && !rightInclusive) {    // (from, to)
            if (from < param && param < to) {
                return true;
            } else {
                return false;
            }
        } else {     // (from, to]
            if (from < param && param <= to) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static void populateCopyObjectHeaders(CopyObjectRequest copyObjectRequest,
                                                 Map<String, String> headers) {
        String copySourceHeader = "/" + copyObjectRequest.getSourceBucketName() + "/"
                + HttpUtil.urlEncode(copyObjectRequest.getSourceKey(), OSSConstants.DEFAULT_CHARSET_NAME);
        headers.put(OSSHeaders.COPY_OBJECT_SOURCE, copySourceHeader);

        addDateHeader(headers,
                OSSHeaders.COPY_OBJECT_SOURCE_IF_MODIFIED_SINCE,
                copyObjectRequest.getModifiedSinceConstraint());
        addDateHeader(headers,
                OSSHeaders.COPY_OBJECT_SOURCE_IF_UNMODIFIED_SINCE,
                copyObjectRequest.getUnmodifiedSinceConstraint());

        addStringListHeader(headers,
                OSSHeaders.COPY_OBJECT_SOURCE_IF_MATCH,
                copyObjectRequest.getMatchingETagConstraints());
        addStringListHeader(headers,
                OSSHeaders.COPY_OBJECT_SOURCE_IF_NONE_MATCH,
                copyObjectRequest.getNonmatchingEtagConstraints());

        addHeader(headers,
                OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION,
                copyObjectRequest.getServerSideEncryption());

        ObjectMetadata newObjectMetadata = copyObjectRequest.getNewObjectMetadata();
        if (newObjectMetadata != null) {
            headers.put(OSSHeaders.COPY_OBJECT_METADATA_DIRECTIVE, MetadataDirective.REPLACE.toString());
            populateRequestMetadata(headers, newObjectMetadata);
        }

        // The header of Content-Length should not be specified on copying an object.
        removeHeader(headers, HttpHeaders.CONTENT_LENGTH);
    }

    public static String buildXMLFromPartEtagList(List<PartETag> partETagList) {
        StringBuilder builder = new StringBuilder();
        builder.append("<CompleteMultipartUpload>\n");
        for (PartETag partETag : partETagList) {
            builder.append("<Part>\n");
            builder.append("<PartNumber>" + partETag.getPartNumber() + "</PartNumber>\n");
            builder.append("<ETag>" + partETag.getETag() + "</ETag>\n");
            builder.append("</Part>\n");
        }
        builder.append("</CompleteMultipartUpload>\n");
        return builder.toString();
    }

    public static void addHeader(Map<String, String> headers, String header, String value) {
        if (value != null) {
            headers.put(header, value);
        }
    }

    public static void addDateHeader(Map<String, String> headers, String header, Date value) {
        if (value != null) {
            headers.put(header, DateUtil.formatRfc822Date(value));
        }
    }

    public static void addStringListHeader(Map<String, String> headers, String header,
                                           List<String> values) {
        if (values != null && !values.isEmpty()) {
            headers.put(header, join(values));
        }
    }

    public static void removeHeader(Map<String, String> headers, String header) {
        if (header != null && headers.containsKey(header)) {
            headers.remove(header);
        }
    }

    public static String join(List<String> strings) {
        StringBuilder result = new StringBuilder();

        boolean first = true;
        for (String s : strings) {
            if (!first) result.append(", ");

            result.append(s);
            first = false;
        }

        return result.toString();
    }

    /**
     * 判断一个字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmptyString(String str) {
        return TextUtils.isEmpty(str);

    }

    public static String buildCanonicalString(RequestMessage request) {

        StringBuilder canonicalString = new StringBuilder();
        canonicalString.append(request.getMethod().toString() + NEW_LINE);

        Map<String, String> headers = request.getHeaders();
        TreeMap<String, String> headersToSign = new TreeMap<String, String>();

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                if (header.getKey() == null) {
                    continue;
                }

                String lowerKey = header.getKey().toLowerCase();
                if (lowerKey.equals(HttpHeaders.CONTENT_TYPE.toLowerCase()) ||
                        lowerKey.equals(HttpHeaders.CONTENT_MD5.toLowerCase()) ||
                        lowerKey.equals(HttpHeaders.DATE.toLowerCase()) ||
                        lowerKey.startsWith(OSSHeaders.OSS_PREFIX)) {
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

            canonicalString.append(NEW_LINE);
        }

        // Append canonical resource to canonical string
        canonicalString.append(buildCanonicalizedResource(request.getBucketName(), request.getObjectKey(), request.getParameters()));

        return canonicalString.toString();
    }

    public static String buildCanonicalizedResource(String bucketName, String objectKey, Map<String, String> parameters) {
        String resourcePath;
        if (bucketName == null && objectKey == null) {
            resourcePath = "/";
        } else if (objectKey == null) {
            resourcePath = "/" + bucketName + "/";
        } else {
            resourcePath = "/" + bucketName + "/" + objectKey;
        }

        return buildCanonicalizedResource(resourcePath, parameters);
    }

    public static String buildCanonicalizedResource(String resourcePath, Map<String, String> parameters) {

        StringBuilder builder = new StringBuilder();
        builder.append(resourcePath);

        if (parameters != null) {
            String[] parameterNames = parameters.keySet().toArray(
                    new String[parameters.size()]);
            Arrays.sort(parameterNames);

            char separater = '?';
            for (String paramName : parameterNames) {
                if (!SIGNED_PARAMTERS.contains(paramName)) {
                    continue;
                }

                builder.append(separater);
                builder.append(paramName);
                String paramValue = parameters.get(paramName);
                if (!isEmptyString(paramValue)) {
                    builder.append("=").append(paramValue);
                }

                separater = '&';
            }
        }

        return builder.toString();
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
        for (Map.Entry<String, String> p : params.entrySet()) {
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

    public static String populateMapToBase64JsonString(Map<String, String> map) {
        JSONObject jsonObj = new JSONObject(map);
        return Base64.encodeToString(jsonObj.toString().getBytes(), Base64.NO_WRAP);
    }

    /**
     * 根据ak/sk、content生成token
     *
     * @param accessKey
     * @param screctKey
     * @param content
     * @return
     */
    public static String sign(String accessKey, String screctKey, String content) {

        String signature;

        try {
            signature = new HmacSHA1Signature().computeSignature(screctKey, content);
            signature = signature.trim();
        } catch (Exception e) {
            throw new IllegalStateException("Compute signature failed!", e);
        }

        return "OSS " + accessKey + ":" + signature;
    }

    /**
     *
     */
    public static boolean isOssOriginHost(String host){
        if (TextUtils.isEmpty(host)){
            return false;
        }
        for (String suffix : OSSConstants.OSS_ORIGN_HOST) {
            if (host.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一个域名是否是cname
     */
    public static boolean isCname(String host) {
        for (String suffix : OSSConstants.DEFAULT_CNAME_EXCLUDE_LIST) {
            if (host.toLowerCase().endsWith(suffix)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断一个域名是否在自定义Cname排除列表之中
     */
    public static boolean isInCustomCnameExcludeList(String endpoint, List<String> customCnameExludeList) {
        for (String host : customCnameExludeList) {
            if (endpoint.endsWith(host.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 校验bucketName的合法性
     *
     * @param bucketName
     * @return
     */
    public static boolean validateBucketName(String bucketName) {
        if (bucketName == null) {
            return false;
        }
        final String BUCKETNAME_REGX = "^[a-z0-9][a-z0-9_\\-]{2,62}$";
        return bucketName.matches(BUCKETNAME_REGX);
    }

    public static void ensureBucketNameValid(String bucketName) {
        if (!validateBucketName(bucketName)) {
            throw new IllegalArgumentException("The bucket name is invalid. \n" +
                    "A bucket name must: \n" +
                    "1) be comprised of lower-case characters, numbers or dash(-); \n" +
                    "2) start with lower case or numbers; \n" +
                    "3) be between 3-63 characters long. ");
        }
    }

    /**
     * 校验objectKey的合法性
     *
     * @param objectKey
     * @return
     */
    public static boolean validateObjectKey(String objectKey) {
        if (objectKey == null) {
            return false;
        }
        if (objectKey.length() <= 0 || objectKey.length() > 1023) {
            return false;
        }
        byte[] keyBytes;
        try {
            keyBytes = objectKey.getBytes(OSSConstants.DEFAULT_CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        char[] keyChars = objectKey.toCharArray();
        char beginKeyChar = keyChars[0];
        if (beginKeyChar == '/' || beginKeyChar == '\\') {
            return false;
        }
        for (char keyChar : keyChars) {
            if (keyChar != 0x09 && keyChar < 0x20) {
                return false;
            }
        }
        return true;
    }

    public static void ensureObjectKeyValid(String objectKey) {
        if (!validateObjectKey(objectKey)) {
            throw new IllegalArgumentException("The object key is invalid. \n" +
                    "An object name should be: \n" +
                    "1) between 1 - 1023 bytes long when encoded as UTF-8 \n" +
                    "2) cannot contain LF or CR or unsupported chars in XML1.0, \n" +
                    "3) cannot begin with \"/\" or \"\\\".");
        }
    }

    public static boolean doesRequestNeedObjectKey(OSSRequest request) {
        if (request instanceof ListObjectsRequest
                || request instanceof ListBucketsRequest
                || request instanceof CreateBucketRequest
                || request instanceof DeleteBucketRequest
                || request instanceof GetBucketInfoRequest
                || request instanceof GetBucketACLRequest
                || request instanceof DeleteMultipleObjectRequest
                || request instanceof ListMultipartUploadsRequest) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean doesBucketNameValid(OSSRequest request) {
        if (request instanceof ListBucketsRequest) {
            return false;
        } else {
            return true;
        }
    }

    public static void ensureRequestValid(OSSRequest request, RequestMessage message) {
        if (doesBucketNameValid(request)) {
            ensureBucketNameValid(message.getBucketName());
        }
        if (doesRequestNeedObjectKey(request)) {
            ensureObjectKeyValid(message.getObjectKey());
        }

        if (request instanceof CopyObjectRequest) {
            ensureObjectKeyValid(((CopyObjectRequest) request).getDestinationKey());
        }
    }

    public static String determineContentType(String initValue, String srcPath, String toObjectKey) {
        if (initValue != null) {
            return initValue;
        }

        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        if (srcPath != null) {
            String extension = srcPath.substring(srcPath.lastIndexOf('.') + 1);
            String contentType = typeMap.getMimeTypeFromExtension(extension);
            if (contentType != null) {
                return contentType;
            }
        }

        if (toObjectKey != null) {
            String extension = toObjectKey.substring(toObjectKey.lastIndexOf('.') + 1);
            String contentType = typeMap.getMimeTypeFromExtension(extension);
            if (contentType != null) {
                return contentType;
            }
        }

        return "application/octet-stream";
    }

    public static void signRequest(RequestMessage message) throws Exception {
        if (!message.isAuthorizationRequired()) {
            return;
        } else {
            if (message.getCredentialProvider() == null) {
                throw new IllegalStateException("当前CredentialProvider为空！！！"
                        + "\n1. 请检查您是否在初始化OSSService时设置CredentialProvider;"
                        + "\n2. 如果您bucket为公共权限，请确认获取到Bucket后已经调用Bucket中接口声明ACL;");
            }
        }

        OSSCredentialProvider credentialProvider = message.getCredentialProvider();
        OSSFederationToken federationToken = null;
        if (credentialProvider instanceof OSSFederationCredentialProvider) {
            federationToken = ((OSSFederationCredentialProvider) credentialProvider).getValidFederationToken();
            if (federationToken == null) {
                OSSLog.logError("Can't get a federation token");
                throw new IOException("Can't get a federation token");
            }
            message.getHeaders().put(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());
        } else if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            federationToken = credentialProvider.getFederationToken();
            message.getHeaders().put(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());
        }

        String contentToSign = OSSUtils.buildCanonicalString(message);
        String signature = "---initValue---";

        if (credentialProvider instanceof OSSFederationCredentialProvider ||
                credentialProvider instanceof OSSStsTokenCredentialProvider) {
            signature = OSSUtils.sign(federationToken.getTempAK(), federationToken.getTempSK(), contentToSign);
        } else if (credentialProvider instanceof OSSPlainTextAKSKCredentialProvider) {
            signature = OSSUtils.sign(((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeyId(),
                    ((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeySecret(), contentToSign);
        } else if (credentialProvider instanceof OSSCustomSignerCredentialProvider) {
            signature = ((OSSCustomSignerCredentialProvider) credentialProvider).signContent(contentToSign);
        }

//        OSSLog.logDebug("signed content: " + contentToSign.replaceAll("\n", "@") + "   ---------   signature: " + signature);
        OSSLog.logDebug("signed content: " + contentToSign + "   \n ---------   signature: " + signature, false);


        message.getHeaders().put(OSSHeaders.AUTHORIZATION, signature);
    }

    public static String buildBaseLogInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("=====[device info]=====\n");
        sb.append("[INFO]: android_version：" + Build.VERSION.RELEASE + "\n");
        sb.append("[INFO]: mobile_model：" + Build.MODEL + "\n");
        String operatorName = getOperatorName(context);
        if (!TextUtils.isEmpty(operatorName)) {
            sb.append("[INFO]: operator_name：" + operatorName + "\n");
        }
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        String networkState = "unconnected";
        String netType = "unknown";
        if (activeNetworkInfo != null && activeNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
            netType = activeNetworkInfo.getTypeName() + " ";
            networkState = "connected";
        }
        sb.append("[INFO]: network_state：" + networkState + "\n");//网络状况
        sb.append("[INFO]: network_type：" + netType);//当前网络类型 如 wifi 2g 3g 4g
        return sb.toString();
    }

    /**
     * 获取运营商名字,需要sim卡
     */
    private static String getOperatorName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telephonyManager.getSimOperator();
        String operatorName = "";
        if (operator != null) {
            if (operator.equals("46000") || operator.equals("46002")) {
                operatorName = "CMCC";
            } else if (operator.equals("46001")) {
                operatorName = "CUCC";
            } else if (operator.equals("46003")) {
                operatorName = "CTCC";
            } else {
                operatorName = operator;
            }
        }
        return operatorName;
    }

    /**
     * Checks if OSS and SDK's checksum is same. If not, throws InconsistentException.
     */
    public static void checkChecksum(Long clientChecksum, Long serverChecksum, String requestId) throws InconsistentException {
        if (clientChecksum != null && serverChecksum != null &&
                !clientChecksum.equals(serverChecksum)) {
            throw new InconsistentException(clientChecksum, serverChecksum, requestId);
        }
    }

    /*
     * check is standard ip

    public static boolean isValidateIP(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }

        //判断IP格式和范围
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";

        Pattern pat = Pattern.compile(rexp);

        Matcher mat = pat.matcher(addr);

        boolean ipAddress = mat.find();

        return ipAddress;
    }
    */

    /***
     * @param host
     * @return
     */
    public static boolean isValidateIP(String host) throws Exception {
        if (host == null) {
            throw new Exception("host is null");
        }

        InetAddress ia;
        try {
            ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return false;
        }

        if (host.equals(ia.getHostAddress())) {
            return true;
        }

        return false;
    }


    public static String buildTriggerCallbackBody(Map<String, String> callbackParams, Map<String, String> callbackVars) {
        StringBuilder builder = new StringBuilder();
        builder.append("x-oss-process=trigger/callback,callback_");

        if (callbackParams != null && callbackParams.size() > 0) {
            JSONObject jsonObj = new JSONObject(callbackParams);
            String paramsJsonString = Base64.encodeToString(jsonObj.toString().getBytes(), Base64.NO_WRAP);
            builder.append(paramsJsonString);
        }
        builder.append("," + "callback-var_");

        if (callbackVars != null && callbackVars.size() > 0) {
            JSONObject jsonObj = new JSONObject(callbackVars);
            String varsJsonString = Base64.encodeToString(jsonObj.toString().getBytes(), Base64.NO_WRAP);
            builder.append(varsJsonString);
        }

        return builder.toString();
    }

    public static String buildImagePersistentBody(String toBucketName, String toObjectKey, String action) {
        StringBuilder builder = new StringBuilder();
        builder.append("x-oss-process=");
        if (action.startsWith("image/")) {
            builder.append(action);
        } else {
            builder.append("image/");
            builder.append(action);
        }
        builder.append("|sys/");
        if (!TextUtils.isEmpty(toBucketName) && !TextUtils.isEmpty(toObjectKey)) {
            String bucketName_base64 = Base64.encodeToString(toBucketName.getBytes(), Base64.NO_WRAP);
            String objectkey_base64 = Base64.encodeToString(toObjectKey.getBytes(), Base64.NO_WRAP);
            builder.append("saveas,o_");
            builder.append(objectkey_base64);
            builder.append(",b_");
            builder.append(bucketName_base64);
        }
        String body = builder.toString();
        OSSLog.logDebug("ImagePersistent body : " + body);
        return body;
    }

    private enum MetadataDirective {

        /* Copy metadata from source object */
        COPY("COPY"),

        /* Replace metadata with newly metadata */
        REPLACE("REPLACE");

        private final String directiveAsString;

        MetadataDirective(String directiveAsString) {
            this.directiveAsString = directiveAsString;
        }

        @Override
        public String toString() {
            return this.directiveAsString;
        }
    }

}
