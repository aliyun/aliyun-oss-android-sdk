package com.alibaba.sdk.android.oss.internal;

import static com.alibaba.sdk.android.oss.common.OSSConstants.PRODUCT_DEFAULT;

import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.HttpMethod;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSLog;
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
import com.alibaba.sdk.android.oss.signer.OSSSignerBase;
import com.alibaba.sdk.android.oss.signer.OSSSignerParams;
import com.alibaba.sdk.android.oss.signer.RequestPresigner;
import com.alibaba.sdk.android.oss.signer.RequestSigner;

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

    private String product;
    private String region;
    private String cloudBoxId;

    public ObjectURLPresigner(URI endpoint, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
        this.endpoint = endpoint;
        this.credentialProvider = credentialProvider;
        this.conf = conf;
        this.product = PRODUCT_DEFAULT;
    }

    public String presignConstrainedURL(GeneratePresignedUrlRequest request) throws ClientException {

        String bucketName = request.getBucketName();
        String objectKey = request.getKey();
        HttpMethod method = request.getMethod() != null ? request.getMethod() : HttpMethod.GET;

        OSSUtils.ensureBucketNameValid(request.getBucketName());
        OSSUtils.ensureObjectKeyValid(request.getKey(), conf.isVerifyObjectStrict());

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(method);
        requestMessage.setBucketName(bucketName);
        requestMessage.setObjectKey(objectKey);
        requestMessage.setHeaders(request.getHeaders());

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

        requestMessage.setUseUrlSignature(true);
        RequestPresigner signer = createSigner(bucketName, objectKey, credentialProvider, conf, request);
        signer.presign(requestMessage);

        String host = buildCanonicalHost(endpoint, bucketName, conf);

        Map<String, String> params = new LinkedHashMap<String, String>();
        params.putAll(requestMessage.getParameters());
        String queryString = HttpUtil.paramToQueryString(params, "utf-8");

        String url = endpoint.getScheme() + "://" + host + "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME)
                + "?" + queryString;

        return url;
    }

    private RequestPresigner createSigner(String bucketName, String key, OSSCredentialProvider credentialProvider, ClientConfiguration config, GeneratePresignedUrlRequest request) {
        String resourcePath = "/" + ((bucketName != null) ? bucketName + "/" : "") + ((key != null ? key : ""));

        OSSSignerParams params = new OSSSignerParams(resourcePath, credentialProvider);
        params.setProduct(product);
        params.setRegion(region);
        params.setCloudBoxId(cloudBoxId);
        params.setExpiration(request.getExpiration());
        params.setAdditionalHeaderNames(request.getAdditionalHeaderNames());

        return OSSSignerBase.createRequestPresigner(config.getSignVersion(), params);
    }

    public String presignConstrainedURL(String bucketName, String objectKey, long expiredTimeInSeconds)
            throws ClientException {
        GeneratePresignedUrlRequest presignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey);
        presignedUrlRequest.setExpiration(expiredTimeInSeconds);
        return presignConstrainedURL(presignedUrlRequest);
    }

    public String presignPublicURL(String bucketName, String objectKey) {
        String host = buildCanonicalHost(endpoint, bucketName, conf);
        return endpoint.getScheme() + "://" + host + "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME);
    }

    private String buildCanonicalHost(URI endpoint, String bucketName, ClientConfiguration config) {
        String originHost = endpoint.getHost();
        String portString = null;
        String path = endpoint.getPath();

        int port = endpoint.getPort();
        if (port != -1) {
            portString = String.valueOf(port);
        }

        boolean isPathStyle = false;

        String host = originHost;
        if(!TextUtils.isEmpty(portString)){
            host += (":" + portString);
        }

        if (!TextUtils.isEmpty(bucketName)) {
            if (OSSUtils.isOssOriginHost(originHost)) {
                // official endpoint
                host = bucketName + "." + originHost;
            } else if (OSSUtils.isInCustomCnameExcludeList(originHost, config.getCustomCnameExcludeList())) {
                if (config.isPathStyleAccessEnable()) {
                    isPathStyle = true;
                } else {
                    host = bucketName + "." + originHost;
                }
            } else {
                try {
                    if (OSSUtils.isValidateIP(originHost)) {
                        // ip address
                        isPathStyle = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (config.isCustomPathPrefixEnable() && path != null) {
            host += path;
        }

        if (isPathStyle) {
            host += ("/" + bucketName);
        }

        return host;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCloudBoxId() {
        return cloudBoxId;
    }

    public void setCloudBoxId(String cloudBoxId) {
        this.cloudBoxId = cloudBoxId;
    }
}
