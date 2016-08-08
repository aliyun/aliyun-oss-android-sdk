package com.alibaba.sdk.android.MNSTest;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.mns.MNS;
import com.alibaba.sdk.android.mns.MNSClient;
import com.alibaba.sdk.android.mns.common.MNSLog;
import com.alibaba.sdk.android.mns.internal.MNSAsyncTask;
import com.alibaba.sdk.android.mns.model.QueueMeta;
import com.alibaba.sdk.android.mns.model.request.ChangeMessageVisibilityRequest;
import com.alibaba.sdk.android.mns.model.request.CreateQueueRequest;
import com.alibaba.sdk.android.mns.model.request.DeleteMessageRequest;
import com.alibaba.sdk.android.mns.model.request.DeleteQueueRequest;
import com.alibaba.sdk.android.mns.model.request.PeekMessageRequest;
import com.alibaba.sdk.android.mns.model.request.ReceiveMessageRequest;
import com.alibaba.sdk.android.mns.model.request.SendMessageRequest;
import com.alibaba.sdk.android.mns.model.Message;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class MessageTest extends AndroidTestCase {
    MNS mns;

    @Override
    public void setUp() throws Exception{
        if (mns == null) {
            Thread.sleep(5 * 1000);
            MNSLog.enableLog();
            mns = new MNSClient(getContext(), MNSTestConfig.ENDPOINT, MNSTestConfig.credentialProvider);
        }

        CreateQueueRequest request = new CreateQueueRequest(MNSTestConfig.QUEUE_NAME);
        QueueMeta queueMeta = new QueueMeta();
        queueMeta.setVisibilityTimeout(60L);
        queueMeta.setLoggingEnabled(1);
        request.setQueueMeta(queueMeta);
        MNSTestConfig.TestCreateQueueCallback callback = new MNSTestConfig.TestCreateQueueCallback();
        MNSAsyncTask task = mns.asyncCreateQueue(request, callback);
        task.waitUntilFinished();
        assertNull(callback.serviceException);
        assertNull(callback.clientException);
        assertTrue(callback.result.getStatusCode() == 204 || callback.result.getStatusCode() == 201 );
    }

    @Override
    public void tearDown() throws Exception{
        DeleteQueueRequest deleteRequest = new DeleteQueueRequest(MNSTestConfig.QUEUE_NAME);
        MNSTestConfig.TestDeleteQueueCallback deleteQueueCallback = new MNSTestConfig.TestDeleteQueueCallback();
        MNSAsyncTask deleteTask = mns.asyncDeleteQueue(deleteRequest, deleteQueueCallback);
        deleteTask.waitUntilFinished();
        assertNull(deleteQueueCallback.serviceException);
        assertNull(deleteQueueCallback.clientException);
        assertEquals(204, deleteQueueCallback.result.getStatusCode());
    }

    public void testSendAndReceiveMessage() throws Exception {
       // SendMessage Test Case
        SendMessageRequest sendRequest = new SendMessageRequest(MNSTestConfig.QUEUE_NAME);
        Message message = new Message();
        message.setMessageBody("TestBody");
        sendRequest.setMessage(message);
        MNSTestConfig.TestSendMessageCallback sendMessageCallback = new MNSTestConfig.TestSendMessageCallback();
        MNSAsyncTask sendMessageTask = mns.asyncSendMessage(sendRequest, sendMessageCallback);
        sendMessageTask.waitUntilFinished();
        assertNull(sendMessageCallback.clientException);
        assertNull(sendMessageCallback.serviceException);
        assertEquals(201, sendMessageCallback.result.getStatusCode());
        assertTrue("F5DCCA6A22CBF7D6A59766721DAEFA44".equals(sendMessageCallback.result.getMessageBodyMd5()));

        // PeekMessage Test Case
        PeekMessageRequest peekRequest = new PeekMessageRequest(MNSTestConfig.QUEUE_NAME);
        MNSTestConfig.TestPeekMessageCallback peekMessageCallback = new MNSTestConfig.TestPeekMessageCallback();
        MNSAsyncTask peekMessageTask = mns.asyncPeekMessage(peekRequest, peekMessageCallback);
        peekMessageTask.waitUntilFinished();;
        assertNull(peekMessageCallback.clientException);
        assertNull(peekMessageCallback.serviceException);
        assertEquals(200, peekMessageCallback.result.getStatusCode());
        assertNull(peekMessageCallback.result.getMessage().getReceiptHandle());
        assertTrue("TestBody".equals(peekMessageCallback.result.getMessage().getMessageBody()));

        // ReceiveMessage Test Case
        ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest(MNSTestConfig.QUEUE_NAME);
        MNSTestConfig.TestReceiveMessageCallback receiveMessageCallback = new MNSTestConfig.TestReceiveMessageCallback();
        MNSAsyncTask receiveMessageTask = mns.asyncReceiveMessage(receiveRequest, receiveMessageCallback);
        receiveMessageTask.waitUntilFinished();;
        assertNull(receiveMessageCallback.clientException);
        assertNull(receiveMessageCallback.serviceException);
        assertEquals(200, receiveMessageCallback.result.getStatusCode());
        System.out.println(receiveMessageCallback.result.getMessage());
        assertTrue("TestBody".equals(receiveMessageCallback.result.getMessage().getMessageBody()));

        // ChangeMessageVisibility Test Case
        ChangeMessageVisibilityRequest changeRequest = new ChangeMessageVisibilityRequest(MNSTestConfig.QUEUE_NAME,
                                                            receiveMessageCallback.result.getMessage().getReceiptHandle(), 50);
        MNSTestConfig.TestChangeMessageVisibilityCallback changeCallback = new MNSTestConfig.TestChangeMessageVisibilityCallback();
        MNSAsyncTask changeTask = mns.asyncChangeMessageVisibility(changeRequest, changeCallback);
        changeTask.waitUntilFinished();
        assertNull(changeCallback.clientException);
        assertNull(changeCallback.serviceException);
        assertEquals(200, changeCallback.result.getStatusCode());

        // DeleteMessage Test Case
        DeleteMessageRequest deleteRequest = new DeleteMessageRequest(MNSTestConfig.QUEUE_NAME, changeCallback.result.getReceiptHandle());
        MNSTestConfig.TestDeleteMessageCallback deleteMessageCallback = new MNSTestConfig.TestDeleteMessageCallback();
        MNSAsyncTask deleteMessageTask = mns.asyncDeleteMessage(deleteRequest, deleteMessageCallback);
        deleteMessageTask.waitUntilFinished();
        assertNull(deleteMessageCallback.clientException);
        assertNull(deleteMessageCallback.serviceException);
        assertEquals(204, deleteMessageCallback.result.getStatusCode());
    }
}