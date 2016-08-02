package com.alibaba.sdk.android.mns.model.serialize;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import java.io.InputStream;

public interface Serializer<T> {
    String serialize(T obj, String encoding) throws Exception;
}