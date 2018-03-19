package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.model.OSSResult;

import java.io.IOException;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public interface ResponseParser<T extends OSSResult> {

    public T parse(ResponseMessage response) throws IOException;
}
