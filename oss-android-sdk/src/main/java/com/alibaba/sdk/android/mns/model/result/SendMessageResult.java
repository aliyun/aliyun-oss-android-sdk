package com.alibaba.sdk.android.mns.model.result;

import com.alibaba.sdk.android.mns.model.MNSResult;
import com.alibaba.sdk.android.mns.model.Message;

/**
 * Created by pan.zengp on 2016/7/31.
 */
public class SendMessageResult extends MNSResult {
    private String messageId;
    private String messageBodyMd5;
    private String receiptHandle;

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageBodyMd5(String messageBodyMd5) {
        this.messageBodyMd5 = messageBodyMd5;
    }

    public String getMessageBodyMd5() {
        return messageBodyMd5;
    }

    public void setReceiptHandle(String receiptHandle) { this.receiptHandle = receiptHandle; }

    public String getReceiptHandle() { return receiptHandle; }

    public void setMessageResponse(Message message) {
        setMessageId(message.getMessageId());
        setMessageBodyMd5(message.getMessageBodyMd5());
        if (message.getReceiptHandle() != null) {
            setReceiptHandle(message.getReceiptHandle());
        }
    }
}
