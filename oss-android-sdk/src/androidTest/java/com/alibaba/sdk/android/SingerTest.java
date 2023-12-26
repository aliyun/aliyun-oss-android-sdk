package com.alibaba.sdk.android;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.support.test.runner.AndroidJUnit4;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.HttpMethod;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.internal.RequestMessage;
import com.alibaba.sdk.android.oss.signer.OSSSignerBase;
import com.alibaba.sdk.android.oss.signer.OSSSignerParams;
import com.alibaba.sdk.android.oss.signer.RequestPresigner;
import com.alibaba.sdk.android.oss.signer.RequestSigner;
import com.alibaba.sdk.android.oss.signer.SignVersion;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.Signer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
public class SingerTest {

    @Test
    public void testSingerWithSignerV4() throws Exception {
        SignVersion signVersion = SignVersion.V4;

        OSSCredentialProvider credentialsProvider = new OSSPlainTextAKSKCredentialProvider("ak", "sk");

        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702743657018L;
        DateUtil.setCurrentServerTime(t);

        Map headers = new HashMap();
        Map parameters = new HashMap();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);

        RequestSigner signer = OSSSignerBase.createRequestSigner(signVersion, signerParam);
        signer.sign(requestMessage);

        String authPat = "OSS4-HMAC-SHA256 Credential=ak/20231216/cn-hangzhou/oss/aliyun_v4_request,Signature=9efc975fb94480f28a9ab2fcf5b23b4170d83a069838df4e77501d79868bd829";
        assertEquals(authPat, requestMessage.getHeaders().get(OSSHeaders.AUTHORIZATION));
    }


    @Test
    public void testSingerWithSignerV4Token() throws Exception {
        SignVersion signVersion = SignVersion.V4;

        OSSCredentialProvider credentialsProvider = new OSSStsTokenCredentialProvider("ak", "sk", "token");

        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702784856007L;
        DateUtil.setCurrentServerTime(t);

        Map headers = new HashMap();
        Map parameters = new HashMap();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);

        RequestSigner signer = OSSSignerBase.createRequestSigner(signVersion, signerParam);
        signer.sign(requestMessage);


        String authPat = "OSS4-HMAC-SHA256 Credential=ak/20231217/cn-hangzhou/oss/aliyun_v4_request,Signature=0436fec1623c737d5827c11d200afd3df51d067b80196080438f57c94d99b9b0";
        assertEquals(authPat, requestMessage.getHeaders().get(OSSHeaders.AUTHORIZATION));
        assertEquals("token", requestMessage.getHeaders().get(OSSHeaders.OSS_SECURITY_TOKEN));
    }

    @Test
    public void testV4SingerWithFederationCredentialProvider() throws Exception {
        SignVersion signVersion = SignVersion.V4;

        OSSCredentialProvider credentialsProvider = new OSSFederationCredentialProvider() {
            @Override
            public OSSFederationToken getFederationToken() throws ClientException {
                OSSFederationToken federationToken = new OSSFederationToken("ak", "sk", "token", 0);
                return federationToken;
            }
        };

        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702784856007L;
        DateUtil.setCurrentServerTime(t);

        Map headers = new HashMap();
        Map parameters = new HashMap();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);

        RequestSigner signer = OSSSignerBase.createRequestSigner(signVersion, signerParam);
        signer.sign(requestMessage);


        String authPat = "OSS4-HMAC-SHA256 Credential=ak/20231217/cn-hangzhou/oss/aliyun_v4_request,Signature=0436fec1623c737d5827c11d200afd3df51d067b80196080438f57c94d99b9b0";
        assertEquals(authPat, requestMessage.getHeaders().get(OSSHeaders.AUTHORIZATION));
        assertEquals("token", requestMessage.getHeaders().get(OSSHeaders.OSS_SECURITY_TOKEN));
    }

    @Test
    public void testV4SingerWithOSSCustomSignerCredentialProvider() throws Exception {
        SignVersion signVersion = SignVersion.V4;
        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702784856007L;
        DateUtil.setCurrentServerTime(t);

        OSSCredentialProvider credentialsProvider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                try {
                    JSONObject json = new JSONObject(content);
                    String date = json.getString("date");
                    String algorithm = json.getString("algorithm");
                    String region = json.getString("region");
                    String product = json.getString("product");
                    String stringToSign = json.getString("stringToSign");
                    assertEquals(product, product);
                    assertEquals(region, region);
                    assertEquals("20231217", date);
                    assertEquals("OSS4-HMAC-SHA256", algorithm);
                    assertEquals("OSS4-HMAC-SHA256\n" +
                            "20231217T034736Z\n" +
                            "20231217/cn-hangzhou/oss/aliyun_v4_request\n" +
                            "068f4aca83d49df1bdedaf73bb024dade6e8aebc7f880481f840553ccd752590", stringToSign);
                    return "OSS4-HMAC-SHA256 Credential=ak/20231217/cn-hangzhou/oss/aliyun_v4_request,Signature=0436fec1623c737d5827c11d200afd3df51d067b80196080438f57c94d99b9b0";
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Map headers = new HashMap();
        Map parameters = new HashMap();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);

        RequestSigner signer = OSSSignerBase.createRequestSigner(signVersion, signerParam);
        signer.sign(requestMessage);


        String authPat = "OSS4-HMAC-SHA256 Credential=ak/20231217/cn-hangzhou/oss/aliyun_v4_request,Signature=0436fec1623c737d5827c11d200afd3df51d067b80196080438f57c94d99b9b0";
        assertEquals(authPat, requestMessage.getHeaders().get(OSSHeaders.AUTHORIZATION));
    }

    @Test
    public void testSignerV4WithAdditionalHeaders() throws Exception {
        SignVersion signVersion = SignVersion.V4;

        OSSCredentialProvider credentialsProvider = new OSSStsTokenCredentialProvider("ak", "sk", "token");

        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702784856007L;
        DateUtil.setCurrentServerTime(t);

        Map headers = new HashMap();
        Map parameters = new HashMap();
        Set signHeaders = new HashSet();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        signHeaders.add("abc");
        signHeaders.add("ZAbc");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);
        requestMessage.setAdditionalHeaderNames(signHeaders);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);

        RequestSigner signer = OSSSignerBase.createRequestSigner(signVersion, signerParam);
        signer.sign(requestMessage);


        String authPat = "OSS4-HMAC-SHA256 Credential=ak/20231217/cn-hangzhou/oss/aliyun_v4_request,AdditionalHeaders=abc;zabc,Signature=53452bf8805332ba233a2b3e9e710b668fdccc02c6d012f9ceac05eae4f85462";
        assertEquals(authPat, requestMessage.getHeaders().get(OSSHeaders.AUTHORIZATION));

        //
        signHeaders = new HashSet();
        signHeaders.add("abc");
        signHeaders.add("ZAbc");
        signHeaders.add("x-oss-head1");
        signHeaders.add("x-oss-no-exist");

        requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);
        requestMessage.setAdditionalHeaderNames(signHeaders);

        resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);

        signer = OSSSignerBase.createRequestSigner(signVersion, signerParam);
        signer.sign(requestMessage);

        assertEquals(authPat, requestMessage.getHeaders().get(OSSHeaders.AUTHORIZATION));
    }

    @Test
    public void testSingerWithSignerV4Presign() throws Exception {
        SignVersion signVersion = SignVersion.V4;

        OSSCredentialProvider credentialsProvider = new OSSPlainTextAKSKCredentialProvider("ak", "sk");

        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702784856007L;
        DateUtil.setCurrentServerTime(t);

        Map headers = new HashMap();
        Map parameters = new HashMap();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);
        requestMessage.setUseUrlSignature(true);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);
        signerParam.setExpiration(599);

        RequestPresigner signer = OSSSignerBase.createRequestPresigner(signVersion, signerParam);
        signer.presign(requestMessage);

        assertEquals("OSS4-HMAC-SHA256", requestMessage.getParameters().get("x-oss-signature-version"));
        assertEquals("20231217T034736Z", requestMessage.getParameters().get("x-oss-date"));
        assertEquals("599", requestMessage.getParameters().get("x-oss-expires"));
        assertEquals("ak/20231217/cn-hangzhou/oss/aliyun_v4_request", requestMessage.getParameters().get("x-oss-credential"));
        assertEquals("c79e8654f07e7361c4c6eb02cdb500c1e65888d7dc38affe1f92dc388b2402c4", requestMessage.getParameters().get("x-oss-signature"));
        assertTrue(requestMessage.getParameters().get("x-oss-additional-headers") == null);
    }

    @Test
    public void testV4PresignWithOSSFederationCredentialProvider() throws Exception {
        SignVersion signVersion = SignVersion.V4;

        OSSCredentialProvider credentialsProvider = new OSSFederationCredentialProvider() {
            @Override
            public OSSFederationToken getFederationToken() throws ClientException {
                OSSFederationToken federationToken = new OSSFederationToken("ak", "sk", "token", 0);
                return federationToken;
            }
        };

        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702784856007L;
        DateUtil.setCurrentServerTime(t);

        Map headers = new HashMap();
        Map parameters = new HashMap();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);
        requestMessage.setUseUrlSignature(true);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);
        signerParam.setExpiration(599);

        RequestPresigner signer = OSSSignerBase.createRequestPresigner(signVersion, signerParam);
        signer.presign(requestMessage);

        assertEquals("OSS4-HMAC-SHA256", requestMessage.getParameters().get("x-oss-signature-version"));
        assertEquals("20231217T034736Z", requestMessage.getParameters().get("x-oss-date"));
        assertEquals("599", requestMessage.getParameters().get("x-oss-expires"));
        assertEquals("ak/20231217/cn-hangzhou/oss/aliyun_v4_request", requestMessage.getParameters().get("x-oss-credential"));
        assertEquals("66a169d090c6ecde7c0e151aa1b55424430dd9f5c24149fbda5503af8f0387e1", requestMessage.getParameters().get("x-oss-signature"));
        assertTrue(requestMessage.getParameters().get("x-oss-additional-headers") == null);
    }

    @Test
    public void testV4PresignWithOSSCustomSignerCredentialProvider() throws Exception {
        SignVersion signVersion = SignVersion.V4;

        OSSCredentialProvider credentialsProvider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                try {
                    JSONObject json = new JSONObject(content);
                    String date = json.getString("date");
                    String algorithm = json.getString("algorithm");
                    String region = json.getString("region");
                    String product = json.getString("product");
                    String stringToSign = json.getString("stringToSign");
                    assertEquals(product, product);
                    assertEquals(region, region);
                    assertEquals("20231217", date);
                    assertEquals("OSS4-HMAC-SHA256", algorithm);
                    assertEquals("OSS4-HMAC-SHA256\n" +
                            "20231217T034736Z\n" +
                            "20231217/cn-hangzhou/oss/aliyun_v4_request\n" +
                            "91cc404ffe6635b4400c92acb70b6f96618d5d79b92240635b4f0a3ef4f24a1b", stringToSign);
                    return "OSS4-HMAC-SHA256 Credential=ak/20231217/cn-hangzhou/oss/aliyun_v4_request,Signature=0436fec1623c737d5827c11d200afd3df51d067b80196080438f57c94d99b9b0";
                } catch (Exception e) {
                    fail();
                    return null;
                }
            }
        };

        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702784856007L;
        DateUtil.setCurrentServerTime(t);

        Map headers = new HashMap();
        Map parameters = new HashMap();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);
        requestMessage.setUseUrlSignature(true);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);
        signerParam.setExpiration(599);

        RequestPresigner signer = OSSSignerBase.createRequestPresigner(signVersion, signerParam);
        signer.presign(requestMessage);

        assertEquals("OSS4-HMAC-SHA256", requestMessage.getParameters().get("x-oss-signature-version"));
        assertEquals("20231217T034736Z", requestMessage.getParameters().get("x-oss-date"));
        assertEquals("599", requestMessage.getParameters().get("x-oss-expires"));
        assertEquals("ak/20231217/cn-hangzhou/oss/aliyun_v4_request", requestMessage.getParameters().get("x-oss-credential"));
        assertEquals("0436fec1623c737d5827c11d200afd3df51d067b80196080438f57c94d99b9b0", requestMessage.getParameters().get("x-oss-signature"));
        assertTrue(requestMessage.getParameters().get("x-oss-additional-headers") == null);
    }

    @Test
    public void testSingerWithSignerV4PresignToken() throws Exception {
        SignVersion signVersion = SignVersion.V4;

        OSSCredentialProvider credentialsProvider = new OSSStsTokenCredentialProvider("ak", "sk", "token");

        String bucket = "bucket";
        String key = "1234+-/123/1.txt";
        String region = "cn-hangzhou";
        String product = "oss";
        long t = 1702784856007L;
        DateUtil.setCurrentServerTime(t);

        Map headers = new HashMap();
        Map parameters = new HashMap();
        Set signHeaders = new HashSet();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        signHeaders.add("abc");
        signHeaders.add("ZAbc");

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);
        requestMessage.setUseUrlSignature(true);

        String resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        OSSSignerParams signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);
        signerParam.setExpiration(599);
        signerParam.setAdditionalHeaderNames(signHeaders);

        RequestPresigner signer = OSSSignerBase.createRequestPresigner(signVersion, signerParam);
        signer.presign(requestMessage);

        assertEquals("OSS4-HMAC-SHA256", requestMessage.getParameters().get("x-oss-signature-version"));
        assertEquals("20231217T034736Z", requestMessage.getParameters().get("x-oss-date"));
        assertEquals("599", requestMessage.getParameters().get("x-oss-expires"));
        assertEquals("ak/20231217/cn-hangzhou/oss/aliyun_v4_request", requestMessage.getParameters().get("x-oss-credential"));
        assertEquals("cfe3ece6600fd383c3bb4cde54089cd0954bd1453d1550c88cd8ddca379a10dc", requestMessage.getParameters().get("x-oss-signature"));
        assertEquals("abc;zabc", requestMessage.getParameters().get("x-oss-additional-headers"));

        headers = new HashMap();
        parameters = new HashMap();
        signHeaders = new HashSet();

        headers.put("x-oss-head1", "value");
        headers.put("abc", "value");
        headers.put("ZAbc", "value");
        headers.put("XYZ", "value");
        headers.put("XYZ", "value");
        headers.put("content-type", "text/plain");
        headers.put("x-oss-content-sha256", "UNSIGNED-PAYLOAD");

        parameters.put("param1", "value1");
        parameters.put("|param1", "value2");
        parameters.put("+param1", "value3");
        parameters.put("|param1", "value4");
        parameters.put("+param2", "");
        parameters.put("|param2", "");
        parameters.put("param2", "");

        signHeaders.add("abc");
        signHeaders.add("ZAbc");
        signHeaders.add("x-oss-head1");

        requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(true);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(bucket);
        requestMessage.setObjectKey(key);
        requestMessage.setHeaders(headers);
        requestMessage.setParameters(parameters);
        requestMessage.setUseUrlSignature(true);

        resourcePath = "/" + ((bucket != null) ? bucket + "/" : "") + ((key != null ? key : ""));
        signerParam = new OSSSignerParams(resourcePath, credentialsProvider);
        signerParam.setProduct(product);
        signerParam.setRegion(region);
        signerParam.setExpiration(599);
        signerParam.setAdditionalHeaderNames(signHeaders);

        signer = OSSSignerBase.createRequestPresigner(signVersion, signerParam);
        signer.presign(requestMessage);

        assertEquals("OSS4-HMAC-SHA256", requestMessage.getParameters().get("x-oss-signature-version"));
        assertEquals("20231217T034736Z", requestMessage.getParameters().get("x-oss-date"));
        assertEquals("599", requestMessage.getParameters().get("x-oss-expires"));
        assertEquals("ak/20231217/cn-hangzhou/oss/aliyun_v4_request", requestMessage.getParameters().get("x-oss-credential"));
        assertEquals("cfe3ece6600fd383c3bb4cde54089cd0954bd1453d1550c88cd8ddca379a10dc", requestMessage.getParameters().get("x-oss-signature"));
        assertEquals("abc;zabc", requestMessage.getParameters().get("x-oss-additional-headers"));
    }
}
