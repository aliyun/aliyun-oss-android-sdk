package com.alibaba.sdk.android.oss.signer;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.SignUtils;
import com.alibaba.sdk.android.oss.internal.RequestMessage;

import java.io.IOException;
import java.util.Date;

public abstract class OSSSignerBase implements RequestSigner, RequestPresigner {
    protected final OSSSignerParams signerParams;

    protected OSSSignerBase(OSSSignerParams signerParams) {
        this.signerParams = signerParams;
    }

    protected void addDateHeaderIfNeeded(RequestMessage request) {
        Date now = new Date();
        long fixedSkewedTimeMillis = DateUtil.getFixedSkewedTimeMillis();
        if (fixedSkewedTimeMillis != 0) {
            now.setTime(fixedSkewedTimeMillis);
        }
        request.getHeaders().put(OSSHeaders.DATE, DateUtil.formatRfc822Date(now));
    }

    protected void addSecurityTokenHeaderIfNeeded(RequestMessage request, OSSFederationToken federationToken) {
        if (federationToken.useSecurityToken() && !request.isUseUrlSignature()) {
            request.addHeader(OSSHeaders.OSS_SECURITY_TOKEN, federationToken.getSecurityToken());
        }
    }

    protected abstract void addAuthorizationHeader(RequestMessage request, OSSFederationToken federationToken);

    protected abstract String buildStringToSign(RequestMessage requestMessage);

    @Override
    public void sign(RequestMessage request) throws Exception {
        if (!request.isAuthorizationRequired()) {
            return;
        }

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

        addDateHeaderIfNeeded(request);
        if (credentialProvider instanceof OSSCustomSignerCredentialProvider) {
            try {
                OSSCustomSignerCredentialProvider customSignerCredentialProvider = (OSSCustomSignerCredentialProvider)credentialProvider;
                String stringToSign = buildStringToSign(request);
                String authorization = customSignerCredentialProvider.signContent(stringToSign);
                request.addHeader(OSSHeaders.AUTHORIZATION, authorization);
            } catch (Exception e) {
                throw new ClientException(e.getMessage(), e);
            }
        } else {
            if (federationToken == null) {
                OSSLog.logError("Can't get a federation token");
                throw new ClientException("Can't get a federation token");
            }
            addSecurityTokenHeaderIfNeeded(request, federationToken);
            addAuthorizationHeader(request, federationToken);
        }
    }

    public static RequestSigner createRequestSigner(SignVersion version, OSSSignerParams signerParams) {
        if (SignVersion.V4.equals(version)) {
            return new OSSV4Signer(signerParams);
        } else {
            return new OSSV1Signer(signerParams);
        }
    }

    public static RequestPresigner createRequestPresigner(SignVersion version, OSSSignerParams signerParams) {
        if (SignVersion.V4.equals(version)) {
            return new OSSV4Signer(signerParams);
        } else {
            return new OSSV1Signer(signerParams);
        }
    }
}
