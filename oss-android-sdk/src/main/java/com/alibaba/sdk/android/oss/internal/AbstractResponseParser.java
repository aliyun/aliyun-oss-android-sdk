package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.CaseInsensitiveHashMap;
import com.alibaba.sdk.android.oss.model.OSSResult;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CheckedInputStream;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * create by jingdan 15/08/17
 */
public abstract class AbstractResponseParser<T extends OSSResult> implements ResponseParser {

    //关闭okhttp响应链接
    public static void safeCloseResponse(ResponseMessage response) {
        try {
            response.close();
        } catch (Exception e) {
        }
    }

    /**
     * 数据解析，子类需要复写自己的具体实现
     *
     * @param response 服务器返回数据
     * @param result   根据范型生成的业务对象
     * @return 解析后的业务对象
     * @throws Exception
     */
    abstract T parseData(ResponseMessage response, T result) throws Exception;

    public boolean needCloseResponse() {
        return true;
    }

    @Override
    public T parse(ResponseMessage response) throws IOException {
        try {
            Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            Class<?> classType = (Class<?>) type;
            T result = (T) classType.newInstance();
            if (result != null) {
                result.setRequestId(response.getHeaders().get(OSSHeaders.OSS_HEADER_REQUEST_ID));
                result.setStatusCode(response.getStatusCode());
                result.setResponseHeader(parseResponseHeader(response.getResponse()));
                setCRC(result, response);
                result = parseData(response, result);
            }
            return result;
        } catch (Exception e) {
            IOException ioException = new IOException(e.getMessage(), e);
            e.printStackTrace();
            OSSLog.logThrowable2Local(e);
            throw ioException;
        } finally {
            if (needCloseResponse()) {
                safeCloseResponse(response);
            }
        }
    }

    //处理返回信息的信息头
    private CaseInsensitiveHashMap<String, String> parseResponseHeader(Response response) {
        CaseInsensitiveHashMap<String, String> result = new CaseInsensitiveHashMap<String, String>();
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            result.put(headers.name(i), headers.value(i));
        }
        return result;
    }

    public <Result extends OSSResult> void setCRC(Result result,
                                                  ResponseMessage response) {
        InputStream inputStream = response.getRequest().getContent();
        if (inputStream != null && inputStream instanceof CheckedInputStream) {
            CheckedInputStream checkedInputStream = (CheckedInputStream) inputStream;
            result.setClientCRC(checkedInputStream.getChecksum().getValue());
        }

        String strSrvCrc = response.getHeaders().get(OSSHeaders.OSS_HASH_CRC64_ECMA);
        if (strSrvCrc != null) {
            BigInteger bi = new BigInteger(strSrvCrc);
            result.setServerCRC(bi.longValue());
        }
    }
}
