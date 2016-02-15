package com.alibaba.sdk.android.oss.internal;

import okhttp3.Response;

import java.io.IOException;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public interface ResponseParser<T> {

    public T parse(Response response) throws IOException;
}
