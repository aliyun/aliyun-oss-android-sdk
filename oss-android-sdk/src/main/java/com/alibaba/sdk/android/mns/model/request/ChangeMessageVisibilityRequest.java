package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;

/**
 * Created by pan.zengp on 2016/8/1.
 */
public class ChangeMessageVisibilityRequest extends MNSRequest {
    private String queueName;
    private String receiptHandle;
    private Integer visibleTime;

    public ChangeMessageVisibilityRequest(String queueName, String receiptHandle, Integer visibleTime) {
        setQueueName(queueName);
        setReceiptHandle(receiptHandle);
        setVisibleTime(visibleTime);
    }

    public void setQueueName(String queueName) { this.queueName = queueName; }

    public String getQueueName() { return queueName; }

    public void setReceiptHandle(String receiptHandle) { this.receiptHandle = receiptHandle; }

    public String getReceiptHandle() { return receiptHandle; }

    public void setVisibleTime(Integer visibleTime) { this.visibleTime = visibleTime; }

    public Integer getVisibleTime() { return visibleTime; }
}
