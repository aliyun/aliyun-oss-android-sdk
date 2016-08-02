package com.alibaba.sdk.android.mns;

/**
 * Created by pan.zengp on 2016/6/21.
 */

import android.content.Context;

import com.alibaba.sdk.android.common.ClientConfiguration;
import com.alibaba.sdk.android.common.ClientException;
import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.common.auth.CredentialProvider;
import com.alibaba.sdk.android.mns.callback.MNSCompletedCallback;
import com.alibaba.sdk.android.mns.internal.MNSAsyncTask;
import com.alibaba.sdk.android.mns.internal.MNSInternalRequestOperation;
import com.alibaba.sdk.android.mns.model.request.ChangeMessageVisibilityRequest;
import com.alibaba.sdk.android.mns.model.request.CreateQueueRequest;
import com.alibaba.sdk.android.mns.model.request.DeleteMessageRequest;
import com.alibaba.sdk.android.mns.model.request.GetQueueAttributesRequest;
import com.alibaba.sdk.android.mns.model.request.ListQueueRequest;
import com.alibaba.sdk.android.mns.model.request.PeekMessageRequest;
import com.alibaba.sdk.android.mns.model.request.ReceiveMessageRequest;
import com.alibaba.sdk.android.mns.model.request.SendMessageRequest;
import com.alibaba.sdk.android.mns.model.request.SetQueueAttributesRequest;
import com.alibaba.sdk.android.mns.model.result.ChangeMessageVisibilityResult;
import com.alibaba.sdk.android.mns.model.result.CreateQueueResult;
import com.alibaba.sdk.android.mns.model.request.DeleteQueueRequest;
import com.alibaba.sdk.android.mns.model.result.DeleteMessageResult;
import com.alibaba.sdk.android.mns.model.result.DeleteQueueResult;
import com.alibaba.sdk.android.mns.model.result.GetQueueAttributesResult;
import com.alibaba.sdk.android.mns.model.result.ListQueueResult;
import com.alibaba.sdk.android.mns.model.result.PeekMessageResult;
import com.alibaba.sdk.android.mns.model.result.ReceiveMessageResult;
import com.alibaba.sdk.android.mns.model.result.SendMessageResult;
import com.alibaba.sdk.android.mns.model.result.SetQueueAttributesResult;

import java.net.URI;
import java.net.URISyntaxException;

public class MNSClient implements MNS {
    private URI endpointURI;
    private CredentialProvider credentialProvider;
    private MNSInternalRequestOperation internalRequestOperation;
    private ClientConfiguration conf;

    /**
     * 构造一个OSSClient实例
     *
     * @param context android应用的applicationContext
     * @param endpoint MNS访问域名
     * @param credentialProvider 鉴权设置
     */
    public MNSClient(Context context, String endpoint, CredentialProvider credentialProvider) {
        this(context, endpoint, credentialProvider, null);
    }

    /**
     * 构造一个OSSClient实例
     *
     * @param context android应用的applicationContext
     * @param endpoint MNS访问域名
     * @param credentialProvider 鉴权设置
     * @param conf 网络参数设置
     */
    public MNSClient(Context context, String endpoint, CredentialProvider credentialProvider, ClientConfiguration conf) {
        try {
            endpoint = endpoint.trim();
            if (!endpoint.startsWith("http")) {
                endpoint = "http://" + endpoint;
            }
            this.endpointURI = new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Endpoint must be a string like 'http://mns.cn-****.aliyuncs.com'!");
        }
        if (credentialProvider == null) {
            throw new IllegalArgumentException("CredentialProvider can't be null.");
        }
        this.credentialProvider = credentialProvider;
        this.conf = (conf == null ? ClientConfiguration.getDefaultConf() : conf);

        internalRequestOperation = new MNSInternalRequestOperation(context, endpointURI, credentialProvider, this.conf);
    }

    @Override
    public MNSAsyncTask<CreateQueueResult> asyncCreateQueue(
            CreateQueueRequest request, MNSCompletedCallback<CreateQueueRequest, CreateQueueResult> completedCallback) {

        return internalRequestOperation.createQueue(request, completedCallback);
    }

    @Override
    public CreateQueueResult createQueue(CreateQueueRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.createQueue(request, null).getResult();
    }

    @Override
    public MNSAsyncTask<DeleteQueueResult> asyncDeleteQueue(
            DeleteQueueRequest request, MNSCompletedCallback<DeleteQueueRequest, DeleteQueueResult> completedCallback) {

        return internalRequestOperation.deleteQueue(request, completedCallback);
    }

    @Override
    public DeleteQueueResult deleteQueue(DeleteQueueRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.deleteQueue(request, null).getResult();
    }

    @Override
    public MNSAsyncTask<SetQueueAttributesResult> asyncSetQueueAttributes(
            SetQueueAttributesRequest request, MNSCompletedCallback<SetQueueAttributesRequest, SetQueueAttributesResult> completedCallback){
        return internalRequestOperation.setQueueAttr(request, completedCallback);
    }

    @Override
    public SetQueueAttributesResult setQueueAttributes(SetQueueAttributesRequest request)
            throws ClientException, ServiceException {
        return internalRequestOperation.setQueueAttr(request, null).getResult();
    }

    @Override
    public MNSAsyncTask<GetQueueAttributesResult> asyncGetQueueAttributes(
            GetQueueAttributesRequest request, MNSCompletedCallback<GetQueueAttributesRequest, GetQueueAttributesResult> completedCallback){
        return internalRequestOperation.getQueueAttr(request, completedCallback);
    }

    @Override
    public GetQueueAttributesResult getQueueAttributes(GetQueueAttributesRequest request)
            throws ClientException, ServiceException{
        return internalRequestOperation.getQueueAttr(request, null).getResult();
    }

    public MNSAsyncTask<ListQueueResult> asyncListQueue(
            ListQueueRequest request, MNSCompletedCallback<ListQueueRequest, ListQueueResult> completedCallback) {
        return internalRequestOperation.listQueue(request, completedCallback);
    }

    public ListQueueResult listQueue(ListQueueRequest request)
            throws ClientException, ServiceException {
        return internalRequestOperation.listQueue(request, null).getResult();
    }

    public MNSAsyncTask<SendMessageResult> asyncSendMessage(
            SendMessageRequest request, MNSCompletedCallback<SendMessageRequest, SendMessageResult> completedCallback) {
        return internalRequestOperation.sendMessage(request, completedCallback);
    }

    public SendMessageResult sendMessage(SendMessageRequest request)
            throws ClientException, ServiceException {
        return internalRequestOperation.sendMessage(request, null).getResult();
    }

    public MNSAsyncTask<ReceiveMessageResult> asyncReceiveMessage(
            ReceiveMessageRequest request, MNSCompletedCallback<ReceiveMessageRequest, ReceiveMessageResult> completedCallback){
        return internalRequestOperation.receiveMessage(request, completedCallback);
    }

    public ReceiveMessageResult receiveMessage(ReceiveMessageRequest request)
            throws ClientException, ServiceException {
        return internalRequestOperation.receiveMessage(request, null).getResult();
    }

    public MNSAsyncTask<DeleteMessageResult> asyncDeleteMessage(
            DeleteMessageRequest request, MNSCompletedCallback<DeleteMessageRequest, DeleteMessageResult> completedCallback){
        return internalRequestOperation.deleteMessage(request, completedCallback);
    }

    public DeleteMessageResult deleteMessage(DeleteMessageRequest request)
            throws ClientException, ServiceException {
        return internalRequestOperation.deleteMessage(request, null).getResult();
    }

    public MNSAsyncTask<PeekMessageResult> asyncPeekMessage(
            PeekMessageRequest request, MNSCompletedCallback<PeekMessageRequest, PeekMessageResult> completedCallback){
        return internalRequestOperation.peekMessage(request, completedCallback);
    }

    public PeekMessageResult peekMessage(PeekMessageRequest request)
            throws ClientException, ServiceException {
        return internalRequestOperation.peekMessage(request, null).getResult();
    }

    public MNSAsyncTask<ChangeMessageVisibilityResult> asyncChangeMessageVisibility(
            ChangeMessageVisibilityRequest request, MNSCompletedCallback<ChangeMessageVisibilityRequest, ChangeMessageVisibilityResult> completedCallback) {
        return internalRequestOperation.changeMessageVisibility(request, completedCallback);
    }

    public ChangeMessageVisibilityResult changeMessageVisibility(ChangeMessageVisibilityRequest request)
            throws ClientException, ServiceException {
        return internalRequestOperation.changeMessageVisibility(request, null).getResult();
    }
}
