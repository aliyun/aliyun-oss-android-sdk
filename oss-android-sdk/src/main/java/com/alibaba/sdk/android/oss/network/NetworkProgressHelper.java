package com.alibaba.sdk.android.oss.network;


import java.io.IOException;
import java.io.InputStream;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by jingdan on 2017/9/12.
 */

public class NetworkProgressHelper {

    /**
     * process response progress
     */
    public static OkHttpClient addProgressResponseListener(OkHttpClient client,
                                                           final ExecutionContext context) {
        OkHttpClient newClient = client.newBuilder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ProgressTouchableResponseBody(originalResponse.body(),
                                        context))
                                .build();
                    }
                })
                .build();
        return newClient;
    }

    /**
     * process request progress
     */
    public static ProgressTouchableRequestBody addProgressRequestBody(InputStream input,
                                                                      long contentLength,
                                                                      String contentType,
                                                                      ExecutionContext context) {
        return new ProgressTouchableRequestBody(input, contentLength, contentType, context);
    }
}
