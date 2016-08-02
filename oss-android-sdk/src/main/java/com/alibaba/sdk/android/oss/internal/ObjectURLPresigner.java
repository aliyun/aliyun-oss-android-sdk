package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.common.ClientConfiguration;
import com.alibaba.sdk.android.common.ClientException;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.common.auth.CredentialProvider;
import com.alibaba.sdk.android.common.auth.CustomSignerCredentialProvider;
import com.alibaba.sdk.android.common.auth.FederationCredentialProvider;
import com.alibaba.sdk.android.common.auth.FederationToken;
import com.alibaba.sdk.android.common.auth.PlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.common.auth.StsTokenCredentialProvider;
import com.alibaba.sdk.android.common.utils.DateUtil;
import com.alibaba.sdk.android.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;

import java.net.URI;

/**
 * Created by zhouzhuo on 11/29/15.
 */
public class ObjectURLPresigner {

    private URI endpoint;
    private CredentialProvider credentialProvider;
    private ClientConfiguration conf;

    public ObjectURLPresigner(URI endpoint, CredentialProvider credentialProvider, ClientConfiguration conf) {
        this.endpoint = endpoint;
        this.credentialProvider = credentialProvider;
        this.conf = conf;
    }

    public String presignConstrainedURL(String bucketName, String objectKey, long expiredTimeInSeconds)
            throws ClientException {

        String resource = "/" + bucketName + "/" + objectKey;
        String expires = String.valueOf(DateUtil.getFixedSkewedTimeMillis() / 1000 + expiredTimeInSeconds);
        FederationToken token = null;

        if (credentialProvider instanceof FederationCredentialProvider) {
            token = ((FederationCredentialProvider) credentialProvider).getValidFederationToken();
            if (token == null) {
                throw new ClientException("Can not get a federation token!");
            }
            resource += "?security-token=" + token.getSecurityToken();
        } else if (credentialProvider instanceof StsTokenCredentialProvider) {
            token = ((StsTokenCredentialProvider) credentialProvider).getFederationToken();
            resource += "?security-token=" + token.getSecurityToken();
        }

        String contentToSign = "GET\n\n\n" + expires + "\n" + resource;
        String signature = "";

        if (credentialProvider instanceof FederationCredentialProvider
                || credentialProvider instanceof StsTokenCredentialProvider) {
            signature = OSSUtils.sign(token.getTempAK(), token.getTempSK(), contentToSign);
        } else if (credentialProvider instanceof PlainTextAKSKCredentialProvider) {
            signature = OSSUtils.sign(((PlainTextAKSKCredentialProvider) credentialProvider).getAccessKeyId(),
                    ((PlainTextAKSKCredentialProvider) credentialProvider).getAccessKeySecret(), contentToSign);
        } else if (credentialProvider instanceof CustomSignerCredentialProvider) {
            signature = ((CustomSignerCredentialProvider) credentialProvider).signContent(contentToSign);
        } else {
            throw new ClientException("Unknown credentialProvider!");
        }

        String accessKey = signature.split(":")[0].substring(4);
        signature = signature.split(":")[1];

        String host = endpoint.getHost();
        if (!OSSUtils.isCname(host) || OSSUtils.isInCustomCnameExcludeList(host, conf.getCustomCnameExcludeList())) {
            host = bucketName + "." + host;
        }

        String url = endpoint.getScheme() + "://" + host + "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME)
                + "?OSSAccessKeyId=" + HttpUtil.urlEncode(accessKey, OSSConstants.DEFAULT_CHARSET_NAME)
                + "&Expires=" + expires
                + "&Signature=" + HttpUtil.urlEncode(signature, OSSConstants.DEFAULT_CHARSET_NAME);

        if (credentialProvider instanceof FederationCredentialProvider
                || credentialProvider instanceof StsTokenCredentialProvider) {
            url = url + "&security-token=" + HttpUtil.urlEncode(token.getSecurityToken(), OSSConstants.DEFAULT_CHARSET_NAME);
        }

        return url;
    }

    public String presignPublicURL(String bucketName, String objectKey) {
        String host = endpoint.getHost();
        if (!OSSUtils.isCname(host) || OSSUtils.isInCustomCnameExcludeList(host, conf.getCustomCnameExcludeList())) {
            host = bucketName + "." + host;
        }
        return endpoint.getScheme() + "://" + host + "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME);
    }
}
