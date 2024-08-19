package com.alibaba.sdk.android.oss.signer;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.internal.RequestMessage;

public interface RequestPresigner {
    public void presign(RequestMessage request) throws Exception;
}
