package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;

/**
 * Created by pan.zengp on 2016/8/1.
 */
public class PeekMessageRequest extends MNSRequest {
    private String queueName;

    public PeekMessageRequest(String queueName) { setQueueName(queueName); }

    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getQueueName() { return queueName; }
}
