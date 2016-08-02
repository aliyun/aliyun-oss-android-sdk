package com.alibaba.sdk.android.mns.callback;

import com.alibaba.sdk.android.common.ClientException;
import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.mns.model.MNSRequest;
import com.alibaba.sdk.android.mns.model.MNSResult;

/**
 * Created by pan.zengp on 2016/7/4.
 */
public interface MNSCompletedCallback<T1 extends MNSRequest, T2 extends MNSResult> {

    public void onSuccess(T1 request, T2 result);

    public void onFailure(T1 request, ClientException clientException, ServiceException serviceException);
}
