package com.alibaba.sdk.android.mns.internal;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by pan.zengp on 2016/7/27.
 */
public interface ResponseParser<T> {
    public T parse(Response response) throws IOException;
}
