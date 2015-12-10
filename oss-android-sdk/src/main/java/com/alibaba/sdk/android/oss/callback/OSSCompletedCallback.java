package com.alibaba.sdk.android.oss.callback;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;

/**
 * Created by zhouzhuo on 11/19/15.
 */
public interface OSSCompletedCallback<T1 extends OSSRequest, T2 extends OSSResult> {

    public void onSuccess(T1 request, T2 result);

    public void onFailure(T1 request, ClientException clientException, ServiceException serviceException);
}
