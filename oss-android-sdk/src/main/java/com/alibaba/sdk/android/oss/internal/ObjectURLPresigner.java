package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.HttpMethod;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.RequestParameters;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpHeaders;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.GeneratePresignedUrlRequest;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by zhouzhuo on 11/29/15.
 */
public class ObjectURLPresigner {

    private URI endpoint;
    private OSSCredentialProvider credentialProvider;
    private ClientConfiguration conf;

    public ObjectURLPresigner(URI endpoint, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
        this.endpoint = endpoint;
        this.credentialProvider = credentialProvider;
        this.conf = conf;
    }

    public String presignConstrainedURL(GeneratePresignedUrlRequest request) throws ClientException {

        String bucketName = request.getBucketName();
        String objectKey = request.getKey();
        String expires = String.valueOf(DateUtil.getFixedSkewedTimeMillis() / 1000 + request.getExpiration());
        HttpMethod method = request.getMethod() != null ? request.getMethod() : HttpMethod.GET;

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(method);
        requestMessage.setBucketName(bucketName);
        requestMessage.setObjectKey(objectKey);

        requestMessage.getHeaders().put(HttpHeaders.DATE, expires);

        if (request.getContentType() != null && !request.getContentType().trim().equals("")) {
            requestMessage.getHeaders().put(HttpHeaders.CONTENT_TYPE, request.getContentType());
        }
        if (request.getContentMD5() != null && !request.getContentMD5().trim().equals("")) {
            requestMessage.getHeaders().put(HttpHeaders.CONTENT_MD5, request.getContentMD5());
        }

        if (request.getQueryParameter() != null && request.getQueryParameter().size() > 0) {
            for (Map.Entry<String, String> entry : request.getQueryParameter().entrySet()) {
                requestMessage.getParameters().put(entry.getKey(), entry.getValue());
            }
        }
        //process img
        if (request.getProcess() != null && !request.getProcess().trim().equals("")) {
            requestMessage.getParameters().put(RequestParameters.X_OSS_PROCESS, request.getProcess());
        }

        OSSFederationToken token = null;

        if (credentialProvider instanceof OSSFederationCredentialProvider) {
            token = ((OSSFederationCredentialProvider) credentialProvider).getValidFederationToken();
            requestMessage.getParameters().put(RequestParameters.SECURITY_TOKEN, token.getSecurityToken());
            if (token == null) {
                throw new ClientException("Can not get a federation token!");
            }
        } else if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            token = ((OSSStsTokenCredentialProvider) credentialProvider).getFederationToken();
            requestMessage.getParameters().put(RequestParameters.SECURITY_TOKEN, token.getSecurityToken());
        }

        String contentToSign = OSSUtils.buildCanonicalString(requestMessage);

        String signature;

        if (credentialProvider instanceof OSSFederationCredentialProvider
                || credentialProvider instanceof OSSStsTokenCredentialProvider) {
            signature = OSSUtils.sign(token.getTempAK(), token.getTempSK(), contentToSign);
        } else if (credentialProvider instanceof OSSPlainTextAKSKCredentialProvider) {
            signature = OSSUtils.sign(((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeyId(),
                    ((OSSPlainTextAKSKCredentialProvider) credentialProvider).getAccessKeySecret(), contentToSign);
        } else if (credentialProvider instanceof OSSCustomSignerCredentialProvider) {
            signature = ((OSSCustomSignerCredentialProvider) credentialProvider).signContent(contentToSign);
        } else {
            throw new ClientException("Unknown credentialProvider!");
        }

        String accessKey = signature.split(":")[0].substring(4);
        signature = signature.split(":")[1];

        String host = endpoint.getHost();
        if (!OSSUtils.isCname(host) || OSSUtils.isInCustomCnameExcludeList(host, conf.getCustomCnameExcludeList())) {
            host = bucketName + "." + host;
        }

        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put(HttpHeaders.EXPIRES, expires);
        params.put(RequestParameters.OSS_ACCESS_KEY_ID, accessKey);
        params.put(RequestParameters.SIGNATURE, signature);
        params.putAll(requestMessage.getParameters());

        String queryString = HttpUtil.paramToQueryString(params, "utf-8");

        String url = endpoint.getScheme() + "://" + host + "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME)
                + "?" + queryString;

        return url;
    }

    public String presignConstrainedURL(String bucketName, String objectKey, long expiredTimeInSeconds)
            throws ClientException {
        GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey);
        presignedUrlRequest.setExpiration(expiredTimeInSeconds);
        return presignConstrainedURL(presignedUrlRequest);
    }

    public String presignPublicURL(String bucketName, String objectKey) {
        String host = endpoint.getHost();
        if (!OSSUtils.isCname(host) || OSSUtils.isInCustomCnameExcludeList(host, conf.getCustomCnameExcludeList())) {
            host = bucketName + "." + host;
        }
        return endpoint.getScheme() + "://" + host + "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME);
    }
}
