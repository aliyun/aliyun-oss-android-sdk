package com.alibaba.sdk.android.mns.internal;

import android.content.Context;
import android.os.Build;

import com.alibaba.sdk.android.common.ClientConfiguration;
import com.alibaba.sdk.android.common.HttpMethod;
import com.alibaba.sdk.android.common.auth.CredentialProvider;
import com.alibaba.sdk.android.common.utils.DateUtil;
import com.alibaba.sdk.android.common.utils.HttpHeaders;
import com.alibaba.sdk.android.common.utils.VersionInfoUtils;
import com.alibaba.sdk.android.mns.common.MNSConstants;
import com.alibaba.sdk.android.mns.model.request.ChangeMessageVisibilityRequest;
import com.alibaba.sdk.android.mns.model.request.CreateQueueRequest;
import com.alibaba.sdk.android.mns.model.request.DeleteMessageRequest;
import com.alibaba.sdk.android.mns.model.request.DeleteQueueRequest;
import com.alibaba.sdk.android.mns.model.request.GetQueueAttributesRequest;
import com.alibaba.sdk.android.mns.model.request.ListQueueRequest;
import com.alibaba.sdk.android.mns.model.request.PeekMessageRequest;
import com.alibaba.sdk.android.mns.model.request.ReceiveMessageRequest;
import com.alibaba.sdk.android.mns.model.request.SendMessageRequest;
import com.alibaba.sdk.android.mns.model.request.SetQueueAttributesRequest;
import com.alibaba.sdk.android.mns.model.result.ChangeMessageVisibilityResult;
import com.alibaba.sdk.android.mns.model.result.CreateQueueResult;
import com.alibaba.sdk.android.mns.callback.MNSCompletedCallback;
import com.alibaba.sdk.android.mns.model.result.DeleteMessageResult;
import com.alibaba.sdk.android.mns.model.result.DeleteQueueResult;
import com.alibaba.sdk.android.mns.model.result.GetQueueAttributesResult;
import com.alibaba.sdk.android.mns.model.result.ListQueueResult;
import com.alibaba.sdk.android.mns.model.result.PeekMessageResult;
import com.alibaba.sdk.android.mns.model.result.ReceiveMessageResult;
import com.alibaba.sdk.android.mns.model.result.SendMessageResult;
import com.alibaba.sdk.android.mns.model.result.SetQueueAttributesResult;
import com.alibaba.sdk.android.mns.model.serialize.MessageSerializer;
import com.alibaba.sdk.android.mns.model.serialize.QueueMetaSerializer;
import com.alibaba.sdk.android.mns.common.MNSHeaders;
import com.alibaba.sdk.android.mns.network.ExecutionContext;
import com.alibaba.sdk.android.mns.internal.ResponseParser;
import com.alibaba.sdk.android.mns.network.MNSRequestTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import okhttp3.Dispatcher;
import okhttp3.Response;

/**
 * Created by pan.zengp on 2016/6/29.
 */
public class MNSInternalRequestOperation {
    private volatile URI endpoint;
    private OkHttpClient innerClient;
    private Context applicationContext;
    private CredentialProvider credentialProvider;
    private int maxRetryCount = MNSConstants.DEFAULT_RETRY_COUNT;
    private ClientConfiguration conf;

    private static ExecutorService executorService = Executors.newFixedThreadPool(MNSConstants.DEFAULT_BASE_THREAD_POOL_SIZE);

    private MNSInternalRequestOperation() {}

    public MNSInternalRequestOperation(Context context, final URI endpoint, CredentialProvider credentialProvider, ClientConfiguration conf){
        this.applicationContext = context;
        this.endpoint = endpoint;
        this.credentialProvider = credentialProvider;
        this.conf = conf;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .cache(null)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                       return HttpsURLConnection.getDefaultHostnameVerifier().verify(endpoint.getHost(), session);
                    }
                });

        if (conf != null)
        {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(conf.getMaxConcurrentRequest());

            builder.connectTimeout(conf.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .dispatcher(dispatcher);

            this.maxRetryCount = conf.getMaxErrorRetry();
        }

        this.innerClient = builder.build();
    }

    public MNSAsyncTask<CreateQueueResult> createQueue(
            CreateQueueRequest request, MNSCompletedCallback<CreateQueueRequest, CreateQueueResult> completedCallback){

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.setType(MNSConstants.MNSType.QUEUE);
        QueueMetaSerializer serializer = new QueueMetaSerializer();
        try{
            String str = serializer.serialize(request.getQueueMeta(), MNSConstants.DEFAULT_CHARSET_NAME);
            requestMessage.setContent(str);
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        addRequiredHeader(requestMessage);
        ExecutionContext<CreateQueueRequest> executionContext = new ExecutionContext<CreateQueueRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<CreateQueueResult> parser = new ResponseParsers.CreateQueueResponseParser();
        Callable<CreateQueueResult> callable = new MNSRequestTask<CreateQueueResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public MNSAsyncTask<DeleteQueueResult> deleteQueue(
            DeleteQueueRequest request, MNSCompletedCallback<DeleteQueueRequest, DeleteQueueResult> completedCallback){

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.DELETE);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.setType(MNSConstants.MNSType.QUEUE);

        addRequiredHeader(requestMessage);
        ExecutionContext<DeleteQueueRequest> executionContext = new ExecutionContext<DeleteQueueRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<DeleteQueueResult> parser = new ResponseParsers.DeleteQueueResponseParser();
        Callable<DeleteQueueResult> callable = new MNSRequestTask<DeleteQueueResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public MNSAsyncTask<SetQueueAttributesResult> setQueueAttr(
            SetQueueAttributesRequest request, MNSCompletedCallback<SetQueueAttributesRequest, SetQueueAttributesResult> completedCallback){

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.getParameters().put(MNSHeaders.MNS_META_OVERRIDE, "true");
        requestMessage.setType(MNSConstants.MNSType.QUEUE);
        QueueMetaSerializer serializer = new QueueMetaSerializer();
        try{
            String str = serializer.serialize(request.getQueueMeta(), MNSConstants.DEFAULT_CHARSET_NAME);
            requestMessage.setContent(str);
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
        addRequiredHeader(requestMessage);
        ExecutionContext<SetQueueAttributesRequest> executionContext = new ExecutionContext<SetQueueAttributesRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<CreateQueueResult> parser = new ResponseParsers.CreateQueueResponseParser();
        Callable<CreateQueueResult> callable = new MNSRequestTask<CreateQueueResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public MNSAsyncTask<GetQueueAttributesResult> getQueueAttr(
            GetQueueAttributesRequest request, MNSCompletedCallback<GetQueueAttributesRequest, GetQueueAttributesResult> completedCallback){

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.setType(MNSConstants.MNSType.QUEUE);

        addRequiredHeader(requestMessage);
        ExecutionContext<GetQueueAttributesRequest> executionContext = new ExecutionContext<GetQueueAttributesRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<GetQueueAttributesResult> parser = new ResponseParsers.GetQueueAttributesResponseParser();
        Callable<CreateQueueResult> callable = new MNSRequestTask<CreateQueueResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public MNSAsyncTask<ListQueueResult> listQueue(
            ListQueueRequest request, MNSCompletedCallback<ListQueueRequest, ListQueueResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setType(MNSConstants.MNSType.QUEUE);
        if (!(request.getPrefix().isEmpty())) {
            requestMessage.getHeaders().put(MNSHeaders.MNS_PREFIX, request.getPrefix());
        }
        if (!(request.getMarker().isEmpty())) {
            requestMessage.getHeaders().put(MNSHeaders.MNS_MARKER, request.getMarker());
        }
        requestMessage.getHeaders().put(MNSHeaders.MNS_RET_NUMBERS, request.getRetNum().toString());

        addRequiredHeader(requestMessage);
        ExecutionContext<ListQueueRequest> executionContext = new ExecutionContext<ListQueueRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ListQueueResult> parser = new ResponseParsers.ListQueueResponseParser();
        Callable<CreateQueueResult> callable = new MNSRequestTask<CreateQueueResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public MNSAsyncTask<SendMessageResult> sendMessage(
            SendMessageRequest request, MNSCompletedCallback<SendMessageRequest, SendMessageResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.setType(MNSConstants.MNSType.MESSAGE);

        MessageSerializer serializer = new MessageSerializer();
        try{
            String str = serializer.serialize(request.getMessage(), MNSConstants.DEFAULT_CHARSET_NAME);
            requestMessage.setContent(str);
        }catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }

        addRequiredHeader(requestMessage);
        ExecutionContext<SendMessageRequest> executionContext = new ExecutionContext<SendMessageRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<SendMessageResult> parser = new ResponseParsers.SendMessageResponseParser();
        Callable<SendMessageResult> callable = new MNSRequestTask<SendMessageResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);

    }

    public MNSAsyncTask<ReceiveMessageResult> receiveMessage(
            ReceiveMessageRequest request, MNSCompletedCallback<ReceiveMessageRequest, ReceiveMessageResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.setType(MNSConstants.MNSType.MESSAGE);

        addRequiredHeader(requestMessage);
        ExecutionContext<ReceiveMessageRequest> executionContext = new ExecutionContext<ReceiveMessageRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ReceiveMessageResult> parser = new ResponseParsers.ReceiveMessageParser();
        Callable<ReceiveMessageResult> callable = new MNSRequestTask<ReceiveMessageResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);

    }

    public MNSAsyncTask<DeleteMessageResult> deleteMessage(
            DeleteMessageRequest request, MNSCompletedCallback<DeleteMessageRequest, DeleteMessageResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.DELETE);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.setType(MNSConstants.MNSType.MESSAGE);
        requestMessage.getParameters().put(MNSConstants.RECEIPT_HANDLE_TAG, request.getReceiptHandle());

        addRequiredHeader(requestMessage);
        ExecutionContext<DeleteMessageRequest> executionContext = new ExecutionContext<DeleteMessageRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<DeleteMessageResult> parser = new ResponseParsers.DeleteMessageParser();
        Callable<DeleteMessageResult> callable = new MNSRequestTask<DeleteMessageResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);

    }

    public MNSAsyncTask<PeekMessageResult> peekMessage(
            PeekMessageRequest request, MNSCompletedCallback<PeekMessageRequest, PeekMessageResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.setType(MNSConstants.MNSType.MESSAGE);
        requestMessage.getParameters().put(MNSHeaders.MNS_PEEK_ONLY, "true");

        addRequiredHeader(requestMessage);
        ExecutionContext<PeekMessageRequest> executionContext = new ExecutionContext<PeekMessageRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<PeekMessageResult> parser = new ResponseParsers.PeekMessageParser();
        Callable<PeekMessageResult> callable = new MNSRequestTask<PeekMessageResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);

    }

    public MNSAsyncTask<ChangeMessageVisibilityResult> changeMessageVisibility(
            ChangeMessageVisibilityRequest request, MNSCompletedCallback<ChangeMessageVisibilityRequest, ChangeMessageVisibilityResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(requestMessage.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setQueueName(request.getQueueName());
        requestMessage.setType(MNSConstants.MNSType.MESSAGE);
        requestMessage.getParameters().put(MNSConstants.RECEIPT_HANDLE_TAG, request.getReceiptHandle());
        requestMessage.getParameters().put(MNSConstants.VISIBILITY_TIMEOUT, request.getVisibleTime().toString());

        addRequiredHeader(requestMessage);
        ExecutionContext<ChangeMessageVisibilityRequest> executionContext = new ExecutionContext<ChangeMessageVisibilityRequest>(getInnerClient(), request);
        if (completedCallback != null)
        {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ChangeMessageVisibilityResult> parser = new ResponseParsers.ChangeMessageVisibilityParser();
        Callable<ChangeMessageVisibilityResult> callable = new MNSRequestTask<ChangeMessageVisibilityResult>(requestMessage, parser, executionContext);

        return MNSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);

    }

    private boolean checkIfHttpdnsAwailable() {
        if (applicationContext == null) {
            return false;
        }

        boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

        String proxyHost;

        if (IS_ICS_OR_LATER) {
            proxyHost = System.getProperty("http.proxyHost");
        } else {
            proxyHost = android.net.Proxy.getHost(applicationContext);
        }
        return proxyHost == null;
    }

    public OkHttpClient getInnerClient() {
        return innerClient;
    }

    private void addRequiredHeader(RequestMessage message) {
        Map<String, String> header = message.getHeaders();

        if (header.get(MNSHeaders.DATE) == null) {
            header.put(MNSHeaders.DATE, DateUtil.currentFixedSkewedTimeInRFC822Format());
        }

        if (header.get(MNSHeaders.CONTENT_TYPE) == null) {
            header.put(MNSHeaders.CONTENT_TYPE, MNSConstants.DEFAULT_CONTENT_TYPE);
        }

        header.put(MNSConstants.X_HEADER_MNS_API_VERSION, MNSConstants.X_HEADER_MNS_API_VERSION_VALUE);

        message.setCredentialProvider(credentialProvider);
    }

    public void setCredentialProvider(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }
}
