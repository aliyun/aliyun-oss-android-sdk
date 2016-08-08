package com.alibaba.sdk.android.MNSTest;

import com.alibaba.sdk.android.common.ClientException;
import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.common.auth.CredentialProvider;
import com.alibaba.sdk.android.common.auth.PlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.mns.callback.MNSCompletedCallback;
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
import com.alibaba.sdk.android.mns.model.result.DeleteMessageResult;
import com.alibaba.sdk.android.mns.model.result.DeleteQueueResult;
import com.alibaba.sdk.android.mns.model.result.GetQueueAttributesResult;
import com.alibaba.sdk.android.mns.model.result.ListQueueResult;
import com.alibaba.sdk.android.mns.model.result.PeekMessageResult;
import com.alibaba.sdk.android.mns.model.result.ReceiveMessageResult;
import com.alibaba.sdk.android.mns.model.result.SendMessageResult;
import com.alibaba.sdk.android.mns.model.result.SetQueueAttributesResult;

/**
 * Created by pan.zengp on 2016/7/28.
 */
public class MNSTestConfig {
    public static final String ENDPOINT = "http://*************.mns.cn-region.aliyuncs.com";

    public static final String AK = "***************";

    public static final String SK = "*********************";

    public static final CredentialProvider credentialProvider = newAKSKCredentialProvider();

    public static final String QUEUE_NAME = "TestAndroidQueue";

    public static CredentialProvider newAKSKCredentialProvider() {
        return new PlainTextAKSKCredentialProvider(AK, SK);
    }

    public final static class TestCreateQueueCallback implements MNSCompletedCallback<CreateQueueRequest, CreateQueueResult> {
        public CreateQueueRequest request;
        public CreateQueueResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(CreateQueueRequest request, CreateQueueResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(CreateQueueRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestDeleteQueueCallback implements MNSCompletedCallback<DeleteQueueRequest, DeleteQueueResult> {
        public DeleteQueueRequest request;
        public DeleteQueueResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(DeleteQueueRequest request, DeleteQueueResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(DeleteQueueRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestGetQueueAttributesCallback implements MNSCompletedCallback<GetQueueAttributesRequest, GetQueueAttributesResult> {
        public GetQueueAttributesRequest request;
        public GetQueueAttributesResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(GetQueueAttributesRequest request, GetQueueAttributesResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(GetQueueAttributesRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestSetQueueAttributesCallback implements MNSCompletedCallback<SetQueueAttributesRequest, SetQueueAttributesResult> {
        public SetQueueAttributesRequest request;
        public SetQueueAttributesResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(SetQueueAttributesRequest request, SetQueueAttributesResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(SetQueueAttributesRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestListQueueCallback implements MNSCompletedCallback<ListQueueRequest, ListQueueResult> {
        public ListQueueRequest request;
        public ListQueueResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(ListQueueRequest request, ListQueueResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(ListQueueRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestSendMessageCallback implements MNSCompletedCallback<SendMessageRequest, SendMessageResult> {
        public SendMessageRequest request;
        public SendMessageResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(SendMessageRequest request, SendMessageResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(SendMessageRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestReceiveMessageCallback implements MNSCompletedCallback<ReceiveMessageRequest, ReceiveMessageResult> {
        public ReceiveMessageRequest request;
        public ReceiveMessageResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(ReceiveMessageRequest request, ReceiveMessageResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(ReceiveMessageRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestDeleteMessageCallback implements MNSCompletedCallback<DeleteMessageRequest, DeleteMessageResult> {
        public DeleteMessageRequest request;
        public DeleteMessageResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(DeleteMessageRequest request, DeleteMessageResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(DeleteMessageRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestPeekMessageCallback implements MNSCompletedCallback<PeekMessageRequest, PeekMessageResult> {
        public PeekMessageRequest request;
        public PeekMessageResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(PeekMessageRequest request, PeekMessageResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(PeekMessageRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }

    public final static class TestChangeMessageVisibilityCallback implements MNSCompletedCallback<ChangeMessageVisibilityRequest, ChangeMessageVisibilityResult> {
        public ChangeMessageVisibilityRequest request;
        public ChangeMessageVisibilityResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(ChangeMessageVisibilityRequest request, ChangeMessageVisibilityResult result){
            System.out.println("OnSuccess");
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(ChangeMessageVisibilityRequest request, ClientException clientException, ServiceException serviceException)
        {
            System.out.println("OnFail");
            this.request = request;
            this.clientException = clientException;
            this.serviceException = serviceException;
        }
    }
}
