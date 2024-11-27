package com.alibaba.sdk.android.oss.signer;

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
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.SignUtils;
import com.alibaba.sdk.android.oss.internal.RequestMessage;

public class OSSV1Signer extends OSSSignerBase {

    public OSSV1Signer(OSSSignerParams signerParams) {
        super(signerParams);
    }

    @Override
    protected void addAuthorizationHeader(RequestMessage request, OSSFederationToken federationToken) {
        String accessKeyId = federationToken.getTempAK();
        String secretAccessKey = federationToken.getTempSK();
        String canonicalString = buildStringToSign(request);
        String signature = ServiceSignature.create().computeSignature(secretAccessKey, canonicalString);
        request.addHeader(OSSHeaders.AUTHORIZATION, SignUtils.composeRequestAuthorization(accessKeyId, signature));
    }

    @Override
    public void presign(RequestMessage request) throws Exception {
        OSSCredentialProvider credentialProvider = signerParams.getCredentialProvider();
        OSSFederationToken federationToken = null;
        if (credentialProvider instanceof OSSFederationCredentialProvider) {
            federationToken = ((OSSFederationCredentialProvider) credentialProvider).getValidFederationToken();
        } else if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            federationToken = credentialProvider.getFederationToken();
        } else if (credentialProvider instanceof OSSPlainTextAKSKCredentialProvider) {
            OSSPlainTextAKSKCredentialProvider plainTextAKSKCredentialProvider = (OSSPlainTextAKSKCredentialProvider)credentialProvider;
            federationToken = new OSSFederationToken(plainTextAKSKCredentialProvider.getAccessKeyId(), plainTextAKSKCredentialProvider.getAccessKeySecret(), null, 0);
        }

        String canonicalResource = signerParams.getResourcePath();
        String expires = String.valueOf(DateUtil.getFixedSkewedTimeMillis() / 1000 + signerParams.getExpiration());

        request.addHeader(OSSHeaders.DATE, expires);

        if (federationToken != null && federationToken.useSecurityToken()) {
            request.addParameter(RequestParameters.SECURITY_TOKEN, federationToken.getSecurityToken());
        }
        String canonicalString = SignUtils.buildCanonicalString(request.getMethod().toString(), canonicalResource, request, expires);
        String signature = null;
        String accessKeyId = null;
        if (credentialProvider instanceof OSSCustomSignerCredentialProvider) {
            try {
                OSSCustomSignerCredentialProvider customSignerCredentialProvider = (OSSCustomSignerCredentialProvider)credentialProvider;
                signature = customSignerCredentialProvider.signContent(canonicalString);
                accessKeyId = signature.split(":")[0].substring(4);
                signature = signature.split(":")[1];
            } catch (Exception e) {
                throw new ClientException(e.getMessage(), e);
            }
        } else {
            accessKeyId = federationToken.getTempAK();
            String secretAccessKey = federationToken.getTempSK();
            signature = ServiceSignature.create().computeSignature(secretAccessKey, canonicalString);
        }
        request.addParameter(OSSHeaders.EXPIRES, expires);
        request.addParameter(RequestParameters.SIGNATURE, signature);
        request.addParameter(RequestParameters.OSS_ACCESS_KEY_ID, accessKeyId);
    }

    @Override
    protected String buildStringToSign(RequestMessage requestMessage) {
        String canonicalString = SignUtils.buildCanonicalString(requestMessage.getMethod().toString(), signerParams.getResourcePath(), requestMessage, null);
        return canonicalString;
    }
}