package com.alibaba.sdk.android.oss.network;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.internal.OSSRetryHandler;
import com.alibaba.sdk.android.oss.internal.OSSRetryType;
import com.alibaba.sdk.android.oss.internal.RequestMessage;
import com.alibaba.sdk.android.oss.internal.ResponseParser;
import com.alibaba.sdk.android.oss.internal.ResponseParsers;
import com.alibaba.sdk.android.oss.model.OSSResult;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSRequestTask<T extends OSSResult> implements Callable<T> {

    private ResponseParser<T> responseParser;

    private RequestMessage message;

    private ExecutionContext context;

    private OkHttpClient client;

    private OSSRetryHandler retryHandler;

    private int currentRetryCount = 0;

    class ProgressTouchableRequestBody extends RequestBody {

        private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

        private byte[] data;
        private File file;
        private InputStream inputStream;
        private String contentType;
        private long contentLength;
        private OSSProgressCallback callback;

        public ProgressTouchableRequestBody(File file, String contentType, OSSProgressCallback callback) {
            this.file = file;
            this.contentType = contentType;
            this.contentLength = file.length();
            this.callback = callback;
        }

        public ProgressTouchableRequestBody(byte[] data, String contentType, OSSProgressCallback callback) {
            this.data = data;
            this.contentType = contentType;
            this.contentLength = data.length;
            this.callback = callback;
        }

        public ProgressTouchableRequestBody(InputStream input, long contentLength, String contentType, OSSProgressCallback callback) {
            this.inputStream = input;
            this.contentType = contentType;
            this.contentLength = contentLength;
            this.callback = callback;
        }

        @Override
        public MediaType contentType() {
            return MediaType.parse(this.contentType);
        }

        @Override
        public long contentLength() throws IOException {
            return this.contentLength;
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;
            if (this.file != null) {
                source = Okio.source(this.file);
            } else if (this.data != null) {
                source = Okio.source(new ByteArrayInputStream(this.data));
            } else if (this.inputStream != null) {
                source = Okio.source(this.inputStream);
            }
            long total = 0;
            long read, toRead, remain;

            while (total < contentLength) {
                remain = contentLength - total;
                toRead = Math.min(remain, SEGMENT_SIZE);

                read = source.read(sink.buffer(), toRead);
                if (read == -1) {
                    break;
                }

                total += read;
                sink.flush();

                if (callback != null) {
                    callback.onProgress(OSSRequestTask.this.context.getRequest(), total, contentLength);
                }
            }
            if(source != null){
                source.close();
            }
        }
    }

    public OSSRequestTask(RequestMessage message, ResponseParser parser, ExecutionContext context, int maxRetry) {
        this.responseParser = parser;
        this.message = message;
        this.context = context;
        this.client = context.getClient();
        this.retryHandler = new OSSRetryHandler(maxRetry);
    }

    @Override
    public T call() throws Exception {

        Request request = null;
        Response response = null;
        Exception exception = null;
        Call call = null;

        try {
            OSSLog.logD("[call] - ");

            // validate request
            OSSUtils.ensureRequestValid(context.getRequest(), message);
            // signing
            OSSUtils.signRequest(message);

            if (context.getCancellationHandler().isCancelled()) {
                throw new InterruptedIOException("This task is cancelled!");
            }

            Request.Builder requestBuilder = new Request.Builder();

            // build request url
            String url = message.buildCanonicalURL();
            requestBuilder = requestBuilder.url(url);

            // set request headers
            for (String key : message.getHeaders().keySet()) {
                requestBuilder = requestBuilder.addHeader(key, message.getHeaders().get(key));
            }

            String contentType = message.getHeaders().get(OSSHeaders.CONTENT_TYPE);

            // set request body
            switch (message.getMethod()) {
                case POST:
                case PUT:
                    OSSUtils.assertTrue(contentType != null, "Content type can't be null when upload!");

                    if (message.getUploadData() != null) {
                        requestBuilder = requestBuilder.method(message.getMethod().toString(),
                                new ProgressTouchableRequestBody(message.getUploadData(), contentType,
                                        context.getProgressCallback()));
                    } else if (message.getUploadFilePath() != null) {
                        requestBuilder = requestBuilder.method(message.getMethod().toString(),
                                new ProgressTouchableRequestBody(new File(message.getUploadFilePath()), contentType,
                                        context.getProgressCallback()));
                    } else if (message.getUploadInputStream() != null) {
                        requestBuilder = requestBuilder.method(message.getMethod().toString(),
                                new ProgressTouchableRequestBody(message.getUploadInputStream(),
                                        message.getReadStreamLength(), contentType,
                                        context.getProgressCallback()));
                    } else {
                        requestBuilder = requestBuilder.method(message.getMethod().toString(), RequestBody.create(null, new byte[0]));
                    }
                    break;
                case GET:
                    requestBuilder = requestBuilder.get();
                    break;
                case HEAD:
                    requestBuilder = requestBuilder.head();
                    break;
                case DELETE:
                    requestBuilder = requestBuilder.delete();
                    break;
                default:
                    break;
            }

            request = requestBuilder.build();

            call = client.newCall(request);
            context.getCancellationHandler().setCall(call);

            // send request
            response = call.execute();

            // 输出响应信息日志
            Map<String, List<String>> headerMap = response.headers().toMultimap();
            StringBuilder printRsp = new StringBuilder();
            printRsp.append("请求响应数据:---------------------\n");
            printRsp.append("response code: " + response.code() + " for url: " + request.url()+"\n");
            printRsp.append("response msg: "+ response.message()+"\n");
            for(String key : headerMap.keySet()){
                printRsp.append("responseHeader ["+key+"]: ").append(headerMap.get(key).get(0)+"\n");
            }
            OSSLog.logD(printRsp.toString());

        } catch (Exception e) {
            OSSLog.logE("Encounter local execpiton: " + e.toString());
            if (OSSLog.isEnableLog()) {
                e.printStackTrace();
            }
            exception = new ClientException(e.getMessage(), e);
        }

        if (response != null) {
            String responseDateString = response.header(OSSHeaders.DATE);
            try {
                // 每次请求回来都更新一下标准时间值
                long serverTime = DateUtil.parseRfc822Date(responseDateString).getTime();
                DateUtil.setCurrentServerTime(serverTime);
            } catch (Exception ignore) {
                // 解析时间失败，不做任何事情
            }
        }

        if (exception == null && (response.code() == 203 || response.code() >= 300)) {
            try {
                exception = ResponseParsers.parseResponseErrorXML(response, request.method().equals("HEAD"));
            } catch (IOException e) {
                exception = new ClientException(e.getMessage(), e);
            }
        } else if (exception == null) {
            try {
                T result = responseParser.parse(response);
                if (context.getCompletedCallback() != null) {
                    try {
                        context.getCompletedCallback().onSuccess(context.getRequest(), result);
                    } catch (Exception ignore) {
                        // 用户在成功回调里抛出的异常，不予理会
                    }
                }
                return result;
            } catch (IOException e) {
                exception = new ClientException(e.getMessage(), e);
            }
        }

        // reconstruct exception caused by manually cancelling
        if ((call != null && call.isCanceled())
                || context.getCancellationHandler().isCancelled()) {
            exception = new ClientException("Task is cancelled!", exception.getCause(), true);
        }

        OSSRetryType retryType = retryHandler.shouldRetry(exception, currentRetryCount);
        OSSLog.logE("[run] - retry, retry type: " + retryType);
        if (retryType == OSSRetryType.OSSRetryTypeShouldRetry) {
            this.currentRetryCount++;
            return call();
        } else if (retryType == OSSRetryType.OSSRetryTypeShouldFixedTimeSkewedAndRetry) {
            // 更新一下请求头的时间，发起重试
            if (response != null) {
                message.getHeaders().put(OSSHeaders.DATE, response.header(OSSHeaders.DATE));
            }
            this.currentRetryCount++;
            return call();
        } else {
            if (exception instanceof ClientException) {
                if (context.getCompletedCallback() != null) {
                    context.getCompletedCallback().onFailure(context.getRequest(), (ClientException) exception, null);
                }
            } else {
                if (context.getCompletedCallback() != null) {
                    context.getCompletedCallback().onFailure(context.getRequest(), null, (ServiceException) exception);
                }
            }
            throw exception;
        }
    }
}
