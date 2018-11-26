package com.alibaba.sdk.android.oss.network;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.CRC64;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.internal.OSSRetryHandler;
import com.alibaba.sdk.android.oss.internal.OSSRetryType;
import com.alibaba.sdk.android.oss.internal.RequestMessage;
import com.alibaba.sdk.android.oss.internal.ResponseMessage;
import com.alibaba.sdk.android.oss.internal.ResponseParser;
import com.alibaba.sdk.android.oss.internal.ResponseParsers;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.ListBucketsRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.CheckedInputStream;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        ResponseMessage responseMessage = null;
        Exception exception = null;
        Call call = null;

        try {
            if (context.getApplicationContext() != null) {
                OSSLog.logInfo(OSSUtils.buildBaseLogInfo(context.getApplicationContext()));
            }

            OSSLog.logDebug("[call] - ");

            OSSRequest ossRequest = context.getRequest();

            // validate request
            OSSUtils.ensureRequestValid(ossRequest, message);
            // signing
            OSSUtils.signRequest(message);

            if (context.getCancellationHandler().isCancelled()) {
                throw new InterruptedIOException("This task is cancelled!");
            }

            Request.Builder requestBuilder = new Request.Builder();

            // build request url
            String url;
            //区分是否按Endpoint进行URL初始化
            if (ossRequest instanceof ListBucketsRequest) {
                url = message.buildOSSServiceURL();
            } else {
                url = message.buildCanonicalURL();
            }
            requestBuilder = requestBuilder.url(url);

            // set request headers
            for (String key : message.getHeaders().keySet()) {
                requestBuilder = requestBuilder.addHeader(key, message.getHeaders().get(key));
            }

            String contentType = message.getHeaders().get(OSSHeaders.CONTENT_TYPE);
            OSSLog.logDebug("request method = " + message.getMethod());
            // set request body
            switch (message.getMethod()) {
                case POST:
                case PUT:
                    OSSUtils.assertTrue(contentType != null, "Content type can't be null when upload!");
                    InputStream inputStream = null;
                    String stringBody = null;
                    long length = 0;
                    if (message.getUploadData() != null) {
                        inputStream = new ByteArrayInputStream(message.getUploadData());
                        length = message.getUploadData().length;
                    } else if (message.getUploadFilePath() != null) {
                        File file = new File(message.getUploadFilePath());
                        inputStream = new FileInputStream(file);
                        length = file.length();
                        if (length <= 0) {
                            throw new ClientException("the length of file is 0!");
                        }
                    } else if (message.getContent() != null) {
                        inputStream = message.getContent();
                        length = message.getContentLength();
                    } else {
                        stringBody = message.getStringBody();
                    }

                    if (inputStream != null) {
                        if (message.isCheckCRC64()) {
                            inputStream = new CheckedInputStream(inputStream, new CRC64());
                        }
                        message.setContent(inputStream);
                        message.setContentLength(length);
                        requestBuilder = requestBuilder.method(message.getMethod().toString(),
                                NetworkProgressHelper.addProgressRequestBody(inputStream, length, contentType, context));
                    } else if (stringBody != null) {
                        requestBuilder = requestBuilder.method(message.getMethod().toString()
                                , RequestBody.create(MediaType.parse(contentType), stringBody.getBytes("UTF-8")));
                    } else {
                        requestBuilder = requestBuilder.method(message.getMethod().toString()
                                , RequestBody.create(null, new byte[0]));
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

            if (ossRequest instanceof GetObjectRequest) {
                client = NetworkProgressHelper.addProgressResponseListener(client, context);
                OSSLog.logDebug("getObject");
            }

            call = client.newCall(request);

            context.getCancellationHandler().setCall(call);

            // send sync request
            Response response = call.execute();

            if (OSSLog.isEnableLog()) {
                // response log
                Map<String, List<String>> headerMap = response.headers().toMultimap();
                StringBuilder printRsp = new StringBuilder();
                printRsp.append("response:---------------------\n");
                printRsp.append("response code: " + response.code() + " for url: " + request.url() + "\n");
//                printRsp.append("response body: " + response.body().string() + "\n");
                for (String key : headerMap.keySet()) {
                    printRsp.append("responseHeader [" + key + "]: ").append(headerMap.get(key).get(0) + "\n");
                }
                OSSLog.logDebug(printRsp.toString());
            }

            // create response message
            responseMessage = buildResponseMessage(message, response);

        } catch (Exception e) {
            OSSLog.logError("Encounter local execpiton: " + e.toString());
            if (OSSLog.isEnableLog()) {
                e.printStackTrace();
            }
            exception = new ClientException(e.getMessage(), e);
        }

        if (exception == null && (responseMessage.getStatusCode() == 203 || responseMessage.getStatusCode() >= 300)) {
            exception = ResponseParsers.parseResponseErrorXML(responseMessage, request.method().equals("HEAD"));
        } else if (exception == null) {
            try {
                T result = responseParser.parse(responseMessage);

                if (context.getCompletedCallback() != null) {
                    try {
                        context.getCompletedCallback().onSuccess(context.getRequest(), result);
                    } catch (Exception ignore) {
                        // The callback throws the exception, ignore it
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
        OSSLog.logError("[run] - retry, retry type: " + retryType);
        if (retryType == OSSRetryType.OSSRetryTypeShouldRetry) {
            this.currentRetryCount++;
            if (context.getRetryCallback() != null) {
                context.getRetryCallback().onRetryCallback();
            }

            try {
                Thread.sleep(retryHandler.timeInterval(currentRetryCount, retryType));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }

            return call();
        } else if (retryType == OSSRetryType.OSSRetryTypeShouldFixedTimeSkewedAndRetry) {
            // Updates the DATE header value and try again
            if (responseMessage != null) {
                String responseDateString = responseMessage.getHeaders().get(OSSHeaders.DATE);
                try {
                    // update the server time after every response
                    long serverTime = DateUtil.parseRfc822Date(responseDateString).getTime();
                    DateUtil.setCurrentServerTime(serverTime);
                    message.getHeaders().put(OSSHeaders.DATE, responseDateString);
                } catch (Exception ignore) {
                    // Fail to parse the time, ignore it
                    OSSLog.logError("[error] - synchronize time, reponseDate:" + responseDateString);
                }
            }

            this.currentRetryCount++;
            if (context.getRetryCallback() != null) {
                context.getRetryCallback().onRetryCallback();
            }
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

    private ResponseMessage buildResponseMessage(RequestMessage request, Response response) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setRequest(request);
        responseMessage.setResponse(response);
        Map<String, String> headers = new HashMap<String, String>();
        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            headers.put(responseHeaders.name(i), responseHeaders.value(i));
        }
        responseMessage.setHeaders(headers);
        responseMessage.setStatusCode(response.code());
        responseMessage.setContentLength(response.body().contentLength());
        responseMessage.setContent(response.body().byteStream());
        return responseMessage;
    }
}
