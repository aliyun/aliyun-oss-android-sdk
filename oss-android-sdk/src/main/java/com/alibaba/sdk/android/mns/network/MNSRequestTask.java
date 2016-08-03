package com.alibaba.sdk.android.mns.network;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import com.alibaba.sdk.android.common.ClientException;
import com.alibaba.sdk.android.common.HttpMethod;
import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.mns.callback.MNSProgressCallback;
import com.alibaba.sdk.android.mns.common.MNSHeaders;
import com.alibaba.sdk.android.mns.common.MNSLog;
import com.alibaba.sdk.android.mns.common.MNSUtils;
import com.alibaba.sdk.android.mns.internal.RequestMessage;
import com.alibaba.sdk.android.mns.internal.ResponseParser;
import com.alibaba.sdk.android.mns.internal.ResponseParsers;
import com.alibaba.sdk.android.mns.model.MNSResult;

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

public class MNSRequestTask<T extends MNSResult> implements Callable<T> {
    private ResponseParser<T> responseParser;

    private RequestMessage message;

    private ExecutionContext context;

    private OkHttpClient client;

    class ProgressTouchableRequestBody extends RequestBody {

        private static final int SEGMENT_SIZE = 2048; // okio.Segment.SIZE

        private byte[] data;
        private File file;
        private InputStream inputStream;
        private String contentType;
        private long contentLength;
        private MNSProgressCallback callback;

        public ProgressTouchableRequestBody(File file, String contentType, MNSProgressCallback callback) {
            this.file = file;
            this.contentType = contentType;
            this.contentLength = file.length();
            this.callback = callback;
        }

        public ProgressTouchableRequestBody(byte[] data, String contentType, MNSProgressCallback callback) {
            this.data = data;
            this.contentType = contentType;
            this.contentLength = data.length;
            this.callback = callback;
        }

        public ProgressTouchableRequestBody(InputStream input, long contentLength, String contentType, MNSProgressCallback callback) {
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
                    callback.onProgress(MNSRequestTask.this.context.getRequest(), total, contentLength);
                }
            }
            if(source != null){
                source.close();
            }
        }
    }

    public MNSRequestTask(RequestMessage message, ResponseParser parser, ExecutionContext context)
    {
        this.responseParser = parser;
        this.message = message;
        this.context = context;
        this.client = context.getClient();
    }

    @Override
    public T call() throws Exception {
        Request request = null;
        Response response = null;
        Exception exception = null;
        Call call = null;

        try{
            MNSLog.logD("[call] - ");

            if (context.getCancellationHandler().isCancelled()){
                throw new InterruptedIOException("This task is cancelled!");
            }

            Request.Builder requestBuilder = new Request.Builder();

            // build request url
            String url = message.buildCanonicalURL();
            MNSUtils.signRequest(message);
            requestBuilder = requestBuilder.url(url);

            // set request headers
            for (String key : message.getHeaders().keySet()) {
                requestBuilder = requestBuilder.addHeader(key, message.getHeaders().get(key));
            }

            String contentType = message.getHeaders().get(MNSHeaders.CONTENT_TYPE);
            String content = message.getContent();

            // set request body
            if (message.getContent() != null) {
                MNSUtils.assertTrue(contentType != null, "Content type can't be null when send data!");
                requestBuilder = requestBuilder.method(message.getMethod().toString(),
                        new ProgressTouchableRequestBody(message.getContent().getBytes(),
                                contentType, context.getProgressCallback()));
            }
            else{
                switch (message.getMethod())
                {
                    case PUT:
                        requestBuilder = requestBuilder.method(message.getMethod().toString(), RequestBody.create(null, new byte[0]));
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
            }

            request = requestBuilder.build();

            if (MNSLog.isEnableLog()){
                MNSLog.logD("request url: " + request.url());
                Map<String, List<String>> headerMap = request.headers().toMultimap();
                for (String key : headerMap.keySet()){
                    MNSLog.logD("requestHeader " + key + ": " + headerMap.get(key).get(0));
                }
            }

            call = client.newCall(request);
            context.getCancellationHandler().setCall(call);

            // send request
            response = call.execute();

            if (MNSLog.isEnableLog()){
                MNSLog.logD("response code: " + response.code() + " for url: " + request.url());
                Map<String, List<String>> headerMap = response.headers().toMultimap();
                for (String key : headerMap.keySet()) {
                    MNSLog.logD("responseHeader " + key + ": " + headerMap.get(key).get(0));
                }
            }
        }catch(Exception e){
            MNSLog.logE("Encounter local execpiton: " + e.toString());
            if (MNSLog.isEnableLog()) {
                e.printStackTrace();
            }
            exception = new ClientException(e.getMessage(), e);
        }

        if (exception == null && (response.code() == 203 || response.code() >= 300)) {
            try {
                exception = ResponseParsers.parseResponseErrorXML(response);
            } catch (IOException e) {
                exception = new ClientException(e.getMessage(), e);
            }
        } else if (exception == null) {
            try {
                T result = responseParser.parse(response);
                if (context.getCompletedCallback() != null) {
                    context.getCompletedCallback().onSuccess(context.getRequest(), result);
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

        if (exception instanceof ClientException){
            if (context.getCompletedCallback() != null){
                context.getCompletedCallback().onFailure(context.getRequest(), (ClientException) exception, null);
            }
        }
        else{
            if (context.getCompletedCallback() != null){
                context.getCompletedCallback().onFailure(context.getRequest(), null, (ServiceException) exception);
            }
        }
        throw exception;
    }
}
