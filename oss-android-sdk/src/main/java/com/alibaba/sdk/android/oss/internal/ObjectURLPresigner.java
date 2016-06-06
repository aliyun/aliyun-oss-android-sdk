package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;

import java.net.URI;

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

    public String presignConstrainedURL(String bucketName, String objectKey, long expiredTimeInSeconds)
            throws ClientException {

        String resource = "/" + bucketName + "/" + objectKey;
        String expires = String.valueOf(DateUtil.getFixedSkewedTimeMillis() / 1000 + expiredTimeInSeconds);
        OSSFederationToken token = null;

        if (credentialProvider instanceof OSSFederationCredentialProvider) {
            token = ((OSSFederationCredentialProvider) credentialProvider).getValidFederationToken();
            if (token == null) {
                throw new ClientException("Can not get a federation token!");
            }
            resource += "?security-token=" + token.getSecurityToken();
        } else if (credentialProvider instanceof OSSStsTokenCredentialProvider) {
            token = ((OSSStsTokenCredentialProvider) credentialProvider).getFederationToken();
            resource += "?security-token=" + token.getSecurityToken();
        }

        String contentToSign = "GET\n\n\n" + expires + "\n" + resource;
        String signature = "";

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

        String url = endpoint.getScheme() + "://" + host + "/" + HttpUtil.urlEncode(objectKey, OSSConstants.DEFAULT_CHARSET_NAME)
                + "?OSSAccessKeyId=" + HttpUtil.urlEncode(accessKey, OSSConstants.DEFAULT_CHARSET_NAME)
                + "&Expires=" + expires
                + "&Signature=" + HttpUtil.urlEncode(signature, OSSConstants.DEFAULT_CHARSET_NAME);

        if (credentialProvider instanceof OSSFederationCredentialProvider
                || credentialProvider instanceof  OSSStsTokenCredentialProvider) {
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
