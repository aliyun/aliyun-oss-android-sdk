package com.alibaba.sdk.android.oss.common.utils;

import android.util.Base64;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import com.alibaba.sdk.android.oss.ClientConfiguration;
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
import com.alibaba.sdk.android.oss.internal.RequestMessage;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PartETag;
import okhttp3.Request;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.alibaba.sdk.android.oss.common.RequestParameters.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSUtils {

    private static final List<String> SIGNED_PARAMTERS = Arrays.asList(new String[]{
            SUBRESOURCE_ACL, SUBRESOURCE_UPLOADS, SUBRESOURCE_LOCATION,
            SUBRESOURCE_CORS, SUBRESOURCE_LOGGING, SUBRESOURCE_WEBSITE,
            SUBRESOURCE_REFERER, SUBRESOURCE_LIFECYCLE, SUBRESOURCE_DELETE,
            SUBRESOURCE_APPEND, UPLOAD_ID, PART_NUMBER, SECURITY_TOKEN, POSITION,
            RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_HEADER_CONTENT_DISPOSITION,
            RESPONSE_HEADER_CONTENT_ENCODING, RESPONSE_HEADER_CONTENT_LANGUAGE,
            RESPONSE_HEADER_CONTENT_TYPE, RESPONSE_HEADER_EXPIRES, X_OSS_PROCESS
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

    private static enum MetadataDirective {

        /* Copy metadata from source object */
        COPY("COPY"),

        /* Replace metadata with newly metadata */
        REPLACE("REPLACE");

        private final String directiveAsString;

        private MetadataDirective(String directiveAsString) {
            this.directiveAsString = directiveAsString;
        }

        @Override
        public String toString() {
            return this.directiveAsString;
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
        return str == null || str.length() == 0;
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

        StringBuilder builder = new StringBuilder();
        builder.append(resourcePath);

        if (parameters != null) {
            String[] parameterNames = parameters.keySet().toArray(
                    new String[parameters.size()]);
            Arrays.sort(parameterNames);

            char separater = '?';
            for(String paramName : parameterNames) {
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

        String signature = null;

        try {
            signature = new HmacSHA1Signature().computeSignature(screctKey, content);
            signature = signature.trim();
        } catch (Exception e) {
            throw new IllegalStateException("Compute signature failed!", e);
        }

        return "OSS " + accessKey + ":" + signature;
    }

    /**
     * 判断一个域名是否是cname
     *
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


    public static boolean isNullOrEmpty(String value) {
        return value == null || value.length() == 0;
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
        if (objectKey.length() <=0 || objectKey.length() > 1023) {
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
                || request instanceof CreateBucketRequest
                || request instanceof DeleteBucketRequest
                || request instanceof GetBucketACLRequest) {
            return false;
        } else {
            return true;
        }
    }

    public static void ensureRequestValid(OSSRequest request, RequestMessage message) {
        ensureBucketNameValid(message.getBucketName());
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

    public static void signRequest(RequestMessage message) throws IOException {
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
                OSSLog.logE("Can't get a federation token");
                throw new IOException("Can't get a federation token");
            }
            message.getHeaders().put(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());
        } else if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            federationToken = ((OSSStsTokenCredentialProvider) credentialProvider).getFederationToken();
            message.getHeaders().put(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());
        }

        String method = message.getMethod().toString();
        String contentMD5 = message.getHeaders().get(OSSHeaders.CONTENT_MD5);
        if (contentMD5 == null) {
            contentMD5 = "";
        }
        String contentType = message.getHeaders().get(OSSHeaders.CONTENT_TYPE);
        if (contentType == null) {
            contentType = "";
        }
        String dateString = message.getHeaders().get(OSSHeaders.DATE);

        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        for (String key: message.getHeaders().keySet()) {
            if (key.toLowerCase().startsWith("x-oss-")) {
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
        if (!OSSUtils.isEmptyString(canonicalizedHeader)) {
            canonicalizedHeader = canonicalizedHeader.trim();
            canonicalizedHeader += "\n";
        }

        String canonicalizedResource = OSSUtils.buildCanonicalizedResource(
                message.getBucketName(),
                message.getObjectKey(),
                message.getParameters());

        String contentToSign = String.format("%s\n%s\n%s\n%s\n%s%s",
                method, contentMD5, contentType, dateString, canonicalizedHeader, canonicalizedResource);

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

        OSSLog.logD("signed content: " + contentToSign.replaceAll("\n", "@") + "   ---------   signature: " + signature);

        message.getHeaders().put(OSSHeaders.AUTHORIZATION, signature);
    }
}
