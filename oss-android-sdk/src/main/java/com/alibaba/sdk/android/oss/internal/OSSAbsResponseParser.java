package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.model.OSSResult;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * create by jingdan 15/08/17
 */
public abstract class OSSAbsResponseParser<T extends OSSResult>  implements ResponseParser{

    /**
     * 数据解析，子类需要复写自己的具体实现
     * @param response 服务器返回数据
     * @param result 根据范型生成的业务对象
     * @return 解析后的业务对象
     * @throws Exception
     */
    abstract T parseData(Response response,T result) throws Exception;

    public boolean needCloseResponse(){
        return true;
    }

    @Override
    public T parse(Response response) throws IOException {
        try{
            Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            Class<?> classType = (Class<?>) type;
            T result = (T) classType.newInstance();
            result.setRequestId(response.header(OSSHeaders.OSS_HEADER_REQUEST_ID));
            result.setStatusCode(response.code());
            result.setResponseHeader(parseResponseHeader(response));
            return parseData(response,result);
        }catch (Exception e){
            IOException ioException = new IOException(e.getMessage(), e);
            e.printStackTrace();
            OSSLog.logThrowable2Local(e);
            throw ioException;
        }finally {
            if(needCloseResponse()) {
                safeCloseResponse(response);
            }
        }
    }

    //关闭okhttp响应链接
    public static void safeCloseResponse(Response response) {
        try {
            response.body().close();
        } catch(Exception e) {
        }
    }

    //处理返回信息的信息头
    private Map<String, String> parseResponseHeader(Response response) {
        Map<String, String> result = new HashMap<String, String>();
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }
}
