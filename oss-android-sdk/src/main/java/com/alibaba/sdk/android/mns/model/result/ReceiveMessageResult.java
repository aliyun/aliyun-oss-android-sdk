package com.alibaba.sdk.android.mns.model.result;

import com.alibaba.sdk.android.mns.model.MNSResult;
import com.alibaba.sdk.android.mns.model.Message;

/**
 * Created by pan.zengp on 2016/7/31.
 */
public class ReceiveMessageResult extends MNSResult {
    private Message message;

    public void setMessage(Message message) { this.message = message; }

    public Message getMessage() { return message; }
}
