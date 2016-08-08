/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 *
 * 版权所有 （C）阿里巴巴云计算，2015
 */
package com.alibaba.sdk.android.mns;

import com.alibaba.sdk.android.common.ClientException;
import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.mns.callback.MNSCompletedCallback;
import com.alibaba.sdk.android.mns.internal.MNSAsyncTask;
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
 * 阿里云消息服务（MNS）的访问接口
 * <p>
 *     阿里云消息服务（MNS），是一种高效、可靠、安全、便捷、可弹性扩展的分布式消息服务。
 *     MNS能够帮助应用开发者在他们应用的分布式组件上自由的传递数据，构建松耦合系统。
 * </p>
 * <p>
 *     MNS为SDK的接口类，封装了MNS的RESTful API接口，考虑到移动端不能在UI线程发起网络请求的编程规范，
 *     SDK为所有接口提供了异步的调用形式，也提供了同步接口。
 * </p>
 */
public interface MNS {
    /**
     *  异步创建队列
     *  Create Queue用于创建队列。
     */
    public MNSAsyncTask<CreateQueueResult> asyncCreateQueue(
            CreateQueueRequest reqeust, MNSCompletedCallback<CreateQueueRequest, CreateQueueResult> completedCallback);
    /**
     * 同步创建队列
     * Create Queue用于创建队列。
     */
    public CreateQueueResult createQueue(CreateQueueRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步创建队列
     * Delete Queue用于删除队列。
     */
    public MNSAsyncTask<DeleteQueueResult> asyncDeleteQueue(
            DeleteQueueRequest request, MNSCompletedCallback<DeleteQueueRequest, DeleteQueueResult> completedCallback);

    /**
     * 异步创建队列
     * Delete Queue用于删除队列。
     */
    public DeleteQueueResult deleteQueue(DeleteQueueRequest request)
        throws ClientException, ServiceException;

    /**
     * 异步设置队列属性
     */
    public MNSAsyncTask<SetQueueAttributesResult> asyncSetQueueAttributes(
            SetQueueAttributesRequest request, MNSCompletedCallback<SetQueueAttributesRequest, SetQueueAttributesResult> completedCallback);

    /**
     * 同步设置队列属性
     */
    public SetQueueAttributesResult setQueueAttributes(SetQueueAttributesRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步获取队列属性
     */
    public MNSAsyncTask<GetQueueAttributesResult> asyncGetQueueAttributes(
            GetQueueAttributesRequest request, MNSCompletedCallback<GetQueueAttributesRequest, GetQueueAttributesResult> completedCallback);

    /**
     * 同步获取队列属性
     */
    public GetQueueAttributesResult getQueueAttributes(GetQueueAttributesRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步列出队列列表
     */
    public MNSAsyncTask<ListQueueResult> asyncListQueue(
            ListQueueRequest request, MNSCompletedCallback<ListQueueRequest, ListQueueResult> completedCallback);

    /**
     * 同步列出队列列表
     */
    public ListQueueResult listQueue(ListQueueRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步发送消息
     */
    public MNSAsyncTask<SendMessageResult> asyncSendMessage(
            SendMessageRequest request, MNSCompletedCallback<SendMessageRequest, SendMessageResult> completedCallback);

    /**
     * 同步发送消息
     */
    public SendMessageResult sendMessage(SendMessageRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步接收消息
     */
    public MNSAsyncTask<ReceiveMessageResult> asyncReceiveMessage(
            ReceiveMessageRequest request, MNSCompletedCallback<ReceiveMessageRequest, ReceiveMessageResult> completedCallback);

    /**
     * 同步接收消息
     */
    public ReceiveMessageResult receiveMessage(ReceiveMessageRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步删除消息
     */
    public MNSAsyncTask<DeleteMessageResult> asyncDeleteMessage(
            DeleteMessageRequest request, MNSCompletedCallback<DeleteMessageRequest, DeleteMessageResult> completedCallback);

    /**
     * 同步删除消息
     */
    public DeleteMessageResult deleteMessage(DeleteMessageRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步查看消息
     */
    public MNSAsyncTask<PeekMessageResult> asyncPeekMessage(
            PeekMessageRequest request, MNSCompletedCallback<PeekMessageRequest, PeekMessageResult> completedCallback);

    /**
     * 同步查看消息
     */
    public PeekMessageResult peekMessage(PeekMessageRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步修改消息可见时间
     */
    public MNSAsyncTask<ChangeMessageVisibilityResult> asyncChangeMessageVisibility(
            ChangeMessageVisibilityRequest request, MNSCompletedCallback<ChangeMessageVisibilityRequest, ChangeMessageVisibilityResult> completedCallback);

    /**
     * 同步修改消息可见时间
     */
    public ChangeMessageVisibilityResult changeMessageVisibility(ChangeMessageVisibilityRequest request)
            throws ClientException, ServiceException;
}
