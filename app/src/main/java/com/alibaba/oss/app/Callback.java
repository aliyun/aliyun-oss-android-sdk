package com.alibaba.oss.app;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;

/**
 * Created by jingdan on 2017/8/31.
 */

public interface Callback<Request, Result> {

    void onSuccess(Request request, Result result);

    void onFailure(Request request, ClientException clientException, ServiceException serviceException);
}
