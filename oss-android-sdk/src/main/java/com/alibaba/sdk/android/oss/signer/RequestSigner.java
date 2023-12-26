package com.alibaba.sdk.android.oss.signer;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.internal.RequestMessage;

public interface RequestSigner {
    public void sign(RequestMessage request) throws ClientException;
}