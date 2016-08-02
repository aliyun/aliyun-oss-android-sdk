package com.alibaba.sdk.android.mns.model.deserialize;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import java.io.InputStream;

import okhttp3.Response;

public interface Deserializer<T> {
    T deserialize(Response response) throws Exception;
}
