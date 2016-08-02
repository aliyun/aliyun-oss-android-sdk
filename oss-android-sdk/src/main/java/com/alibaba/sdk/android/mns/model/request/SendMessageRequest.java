package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;
import com.alibaba.sdk.android.mns.model.Message;

/**
 * Created by pan.zengp on 2016/7/31.
 */
public class SendMessageRequest extends MNSRequest {
    private String queueName;
    private Message message;

    public SendMessageRequest(String queueName) {
        setQueueName(queueName);
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
