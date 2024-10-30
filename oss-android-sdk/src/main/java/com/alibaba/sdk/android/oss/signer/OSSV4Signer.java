package com.alibaba.sdk.android.oss.signer;

import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.RequestParameters;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpHeaders;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.SignUtils;
import com.alibaba.sdk.android.oss.common.utils.StringUtils;
import com.alibaba.sdk.android.oss.internal.RequestMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

public class OSSV4Signer extends OSSSignerBase {

    private static final List<String> DEFAULT_SIGNED_HEADERS = Arrays.asList(HttpHeaders.CONTENT_TYPE.toLowerCase(), HttpHeaders.CONTENT_MD5.toLowerCase());
    // ISO 8601 format
    private static final String ISO8601_DATETIME_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    private static final String ISO8601_DATE_FORMAT = "yyyyMMdd";
    private static final String SEPARATOR_BACKSLASH = "/";

    private static final String OSS4_HMAC_SHA256 = "OSS4-HMAC-SHA256";
    private static final String TERMINATOR = "aliyun_v4_request";
    private static final String SECRET_KEY_PREFIX = "aliyun_v4";
    private static final String CONTENT_STRING_TO_SIGN = "stringToSign";
    private static final String CONTENT_DATE = "date";
    private static final String CONTENT_ALGORITHM = "algorithm";
    private static final String CONTENT_REGION = "region";
    private static final String CONTENT_PRODUCT = "product";

    private static final String SECURITY_TOKEN = "x-oss-security-token";

    Set<String> additionalSignedHeaders;
    private Date requestDateTime;

    protected OSSV4Signer(OSSSignerParams signerParams) {
        super(signerParams);
    }

    private static DateFormat getIso8601DateTimeFormat() {
        SimpleDateFormat df = new SimpleDateFormat(ISO8601_DATETIME_FORMAT, Locale.US);
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df;
    }

    private static DateFormat getIso8601DateFormat() {
        SimpleDateFormat df = new SimpleDateFormat(ISO8601_DATE_FORMAT, Locale.US);
        df.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return df;
    }

    private String getDateTime() {
        return getIso8601DateTimeFormat().format(requestDateTime);
    }

    private String getDate() {
        return getIso8601DateFormat().format(requestDateTime);
    }

    private boolean hasDefaultSignedHeaders(String header) {
        if (DEFAULT_SIGNED_HEADERS.contains(header)) {
            return true;
        }
        return header.startsWith(OSSHeaders.OSS_PREFIX);
    }

    private boolean hasSignedHeaders(String header) {
        if (hasDefaultSignedHeaders(header)) {
            return true;
        }
        return additionalSignedHeaders.contains(header);
    }

    private boolean hasAdditionalSignedHeaders() {
        return (additionalSignedHeaders != null) && !additionalSignedHeaders.isEmpty();
    }

    private TreeMap<String, String> buildSortedHeadersMap(Map<String, String> headers) {
        TreeMap<String, String> orderMap = new TreeMap<String, String>();
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                String key = header.getKey().toLowerCase();
                if (hasSignedHeaders(key)) {
                    orderMap.put(key, header.getValue());
                }
            }
        }
        return orderMap;
    }

    private void resolveAdditionalSignedHeaders(RequestMessage request, Set<String> headerNames) {
        Set<String> signedHeaders = new TreeSet<String>();
        if (headerNames != null) {
            for (String additionalHeader : headerNames) {
                String additionalHeaderKey = additionalHeader.toLowerCase();
                for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                    String headerKey = header.getKey().toLowerCase();
                    if (headerKey.equals(additionalHeaderKey) && !hasDefaultSignedHeaders(additionalHeaderKey)) {
                        signedHeaders.add(additionalHeaderKey);
                    }
                }
            }
        }
        additionalSignedHeaders = signedHeaders;
    }

    private void addSignedHeaderIfNeeded(RequestMessage request) {
        Set<String> signedHeaders = additionalSignedHeaders;
        if (signedHeaders.contains(OSSHeaders.HOST.toLowerCase()) &&
                !request.getHeaders().containsKey(OSSHeaders.HOST)) {
            request.addHeader(OSSHeaders.HOST, request.getEndpoint().getHost());
        }
    }

    private void addOSSContentSha256Header(RequestMessage request) {
        request.addHeader(OSSHeaders.OSS_CONTENT_SHA256, "UNSIGNED-PAYLOAD");
    }

    @Override
    protected void addDateHeaderIfNeeded(RequestMessage request) {
        initRequestDateTime();
        request.getHeaders().put(OSSHeaders.DATE, getDateTime());
    }

    private void initRequestDateTime() {
        Date now = new Date();
        long fixedSkewedTimeMillis = DateUtil.getFixedSkewedTimeMillis();
        if (fixedSkewedTimeMillis != 0) {
            now.setTime(fixedSkewedTimeMillis);
        }
        requestDateTime = now;
    }

    private String buildCanonicalRequest(RequestMessage request) {
        String method = request.getMethod().toString();
        String resourcePath = signerParams.getResourcePath();

        StringBuilder canonicalString = new StringBuilder();

        //http method + "\n"
        canonicalString.append(method).append(SignParameters.NEW_LINE);

        //Canonical URI + "\n"
        canonicalString.append(HttpUtil.urlEncode(resourcePath, true)).append(SignParameters.NEW_LINE);

        //Canonical Query String + "\n" +
        Map<String, String> parameters = request.getParameters();
        TreeMap<String, String> orderMap = new TreeMap<String, String>();
        if (parameters != null) {
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                orderMap.put(HttpUtil.urlEncode(StringUtils.trim(param.getKey()), false), HttpUtil.urlEncode(StringUtils.trim(param.getValue()), false));
            }
        }
        String separator = "";
        StringBuilder canonicalPart = new StringBuilder();
        for (Map.Entry<String, String> param : orderMap.entrySet()) {
            canonicalPart.append(separator).append(param.getKey());
            if (param.getValue() != null && !param.getValue().isEmpty()) {
                canonicalPart.append("=").append(param.getValue());
            }
            separator = "&";
        }
        canonicalString.append(canonicalPart).append(SignParameters.NEW_LINE);

        //Canonical Headers + "\n" +
        orderMap = buildSortedHeadersMap(request.getHeaders());
        canonicalPart = new StringBuilder();
        for (Map.Entry<String, String> param : orderMap.entrySet()) {
            canonicalPart.append(param.getKey()).append(":").append(param.getValue().trim()).append(SignParameters.NEW_LINE);
        }
        canonicalString.append(canonicalPart).append(SignParameters.NEW_LINE);

        //Additional Headers + "\n" +
        String canonicalPartStr = StringUtils.join(";", additionalSignedHeaders);
        canonicalString.append(canonicalPartStr).append(SignParameters.NEW_LINE);

        //Hashed PayLoad
        String hashedPayLoad = request.getHeaders().get(OSSHeaders.OSS_CONTENT_SHA256);
        if (StringUtils.isNullOrEmpty(hashedPayLoad)) {
            hashedPayLoad = "UNSIGNED-PAYLOAD";
        }
        canonicalString.append(hashedPayLoad);

        return canonicalString.toString();
    }

    private String getRegion() {
        if (signerParams.getCloudBoxId() != null) {
            return signerParams.getCloudBoxId();
        }
        return signerParams.getRegion();
    }

    private String getProduct() {
        return signerParams.getProduct();
    }

    private String buildScope() {
        return getDate() + SEPARATOR_BACKSLASH +
                getRegion() + SEPARATOR_BACKSLASH +
                getProduct() + SEPARATOR_BACKSLASH +
                TERMINATOR;
    }

    private String buildStringToSign(String canonicalString) {
        return OSS4_HMAC_SHA256 + SignParameters.NEW_LINE +
                getDateTime() + SignParameters.NEW_LINE +
                buildScope() + SignParameters.NEW_LINE +
                BinaryUtil.toHex(BinaryUtil.calculateSha256(canonicalString.getBytes(StringUtils.UTF8)));
    }

    private byte[] buildSigningKey(OSSFederationToken federationToken) {
        ServiceSignature signature = ServiceSignature.create("HmacSHA256");
        byte[] signingSecret = (SECRET_KEY_PREFIX + federationToken.getTempSK()).getBytes(StringUtils.UTF8);
        byte[] signingDate = signature.computeHash(signingSecret, getDate().getBytes(StringUtils.UTF8));
        byte[] signingRegion = signature.computeHash(signingDate, getRegion().getBytes(StringUtils.UTF8));
        byte[] signingService = signature.computeHash(signingRegion, getProduct().getBytes(StringUtils.UTF8));

        return signature.computeHash(signingService, TERMINATOR.getBytes(StringUtils.UTF8));
    }

    private String buildSignature(byte[] signingKey, String stringToSign) {
        byte[] result = ServiceSignature.create("HmacSHA256").computeHash(signingKey, stringToSign.getBytes(StringUtils.UTF8));
        return BinaryUtil.toHex(result);
    }

    private String buildAuthorization(String signature, OSSFederationToken federationToken) {
        String credential = "Credential=" + federationToken.getTempAK() + SEPARATOR_BACKSLASH + buildScope();
        String signedHeaders = !hasAdditionalSignedHeaders() ? "" : ",AdditionalHeaders=" + StringUtils.join(";", additionalSignedHeaders);
        String sign = ",Signature=" + signature;
        return "OSS4-HMAC-SHA256 " + credential + signedHeaders + sign;
    }

    @Override
    protected void addAuthorizationHeader(RequestMessage request, OSSFederationToken federationToken) {
        String stringToSign = buildStringToSign(request);
        byte[] signingKey = buildSigningKey(federationToken);
        String signature = buildSignature(signingKey, stringToSign);
        String authorization = buildAuthorization(signature, federationToken);

        request.addHeader(OSSHeaders.AUTHORIZATION, authorization);
    }

    @Override
    protected String buildStringToSign(RequestMessage request) {
        String canonicalRequest = buildCanonicalRequest(request);
        OSSLog.logInfo(canonicalRequest);
        String stringToSign = buildStringToSign(canonicalRequest);
        return stringToSign;
    }

    @Override
    public void sign(RequestMessage request) throws Exception {
        if (!request.isAuthorizationRequired()) {
            return;
        }
        if (getRegion() == null) {
            throw new ClientException("Region or cloudBoxId haven't been set!");
        }
        OSSCredentialProvider credentialProvider = signerParams.getCredentialProvider();
        if (credentialProvider instanceof OSSCustomSignerCredentialProvider) {
            throw new IOException("V4 signature does not support OSSCustomSignerCredentialProvider");
        }
        OSSFederationToken federationToken = null;
        if (credentialProvider instanceof OSSFederationCredentialProvider) {
            federationToken = ((OSSFederationCredentialProvider) credentialProvider).getValidFederationToken();
        } else if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            federationToken = credentialProvider.getFederationToken();
        } else if (credentialProvider instanceof OSSPlainTextAKSKCredentialProvider) {
            OSSPlainTextAKSKCredentialProvider plainTextAKSKCredentialProvider = (OSSPlainTextAKSKCredentialProvider)credentialProvider;
            federationToken = new OSSFederationToken(plainTextAKSKCredentialProvider.getAccessKeyId(), plainTextAKSKCredentialProvider.getAccessKeySecret(), null, 0);
        }

        addDateHeaderIfNeeded(request);
        if (federationToken == null) {
            OSSLog.logError("Can't get a federation token");
            throw new ClientException("Can't get a federation token");
        }
        resolveAdditionalSignedHeaders(request, request.getAdditionalHeaderNames());
        addSignedHeaderIfNeeded(request);
        addSecurityTokenHeaderIfNeeded(request, federationToken);
        addOSSContentSha256Header(request);
        addAuthorizationHeader(request, federationToken);
    }

    @Override
    public void presign(RequestMessage request) throws Exception {
        if (getRegion() == null) {
            throw new ClientException("Region or cloudBoxId haven't been set!");
        }
        OSSCredentialProvider credentialProvider = signerParams.getCredentialProvider();
        if (credentialProvider instanceof OSSCustomSignerCredentialProvider) {
            throw new IOException("V4 signature does not support OSSCustomSignerCredentialProvider");
        }
        OSSFederationToken federationToken = null;
        if (credentialProvider instanceof OSSFederationCredentialProvider) {
            federationToken = ((OSSFederationCredentialProvider) credentialProvider).getValidFederationToken();
        } else if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            federationToken = credentialProvider.getFederationToken();
        } else if (credentialProvider instanceof OSSPlainTextAKSKCredentialProvider) {
            OSSPlainTextAKSKCredentialProvider plainTextAKSKCredentialProvider = (OSSPlainTextAKSKCredentialProvider)credentialProvider;
            federationToken = new OSSFederationToken(plainTextAKSKCredentialProvider.getAccessKeyId(), plainTextAKSKCredentialProvider.getAccessKeySecret(), null, 0);
        }

        // date
        initRequestDateTime();
        String expires = String.valueOf(signerParams.getExpiration());
        request.addParameter("x-oss-date", getIso8601DateTimeFormat().format(requestDateTime));
        request.addParameter("x-oss-expires", expires);

        //signed header
        resolveAdditionalSignedHeaders(request, signerParams.getAdditionalHeaderNames());
        addSignedHeaderIfNeeded(request);
        if (hasAdditionalSignedHeaders()) {
            request.addParameter("x-oss-additional-headers", StringUtils.join(";", additionalSignedHeaders));
        }

        request.addParameter("x-oss-signature-version", "OSS4-HMAC-SHA256");

        String signature = null;
        if (federationToken.useSecurityToken()) {
            request.addParameter(SECURITY_TOKEN, federationToken.getSecurityToken());
        }
        String credential = federationToken.getTempAK() + SEPARATOR_BACKSLASH + buildScope();
        request.addParameter("x-oss-credential", credential);
        String stringToSign = buildStringToSign(request);
        // sign
        byte[] signingKey = buildSigningKey(federationToken);
        signature = buildSignature(signingKey, stringToSign);

        request.addParameter("x-oss-signature", signature);
    }
}