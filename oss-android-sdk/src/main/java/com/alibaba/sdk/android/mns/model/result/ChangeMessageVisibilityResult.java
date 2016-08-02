package com.alibaba.sdk.android.mns.model.result;

import com.alibaba.sdk.android.mns.model.MNSResult;
import com.alibaba.sdk.android.mns.model.Message;

import java.util.Date;

/**
 * Created by pan.zengp on 2016/8/1.
 */
public class ChangeMessageVisibilityResult extends MNSResult {
    private String receiptHandle;
    private Date nextVisibleTime;

    public void setReceiptHandle(String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public void setNextVisibleTime(Date nextVisibleTime) {
        this.nextVisibleTime = nextVisibleTime;
    }

    public Date getNextVisibleTime() {
        return nextVisibleTime;
    }

    public void setChangeVisibleResponse(Message message) {
        setReceiptHandle(message.getReceiptHandle());
        setNextVisibleTime(message.getNextVisibleTime());
    }
}
