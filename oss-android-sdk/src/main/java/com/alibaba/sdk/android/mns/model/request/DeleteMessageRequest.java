package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;

/**
 * Created by pan.zengp on 2016/8/1.
 */
public class DeleteMessageRequest extends MNSRequest {
    private String queueName;
    private String receiptHandle;

    public DeleteMessageRequest(String queueName, String receiptHandle) {
        setQueueName(queueName);
        setReceiptHandle(receiptHandle);
    }

    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getQueueName() { return queueName; }

    public void setReceiptHandle(String receiptHandle) { this.receiptHandle = receiptHandle; }

    public String getReceiptHandle() { return receiptHandle; }
}
