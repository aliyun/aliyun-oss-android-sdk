package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;
import com.alibaba.sdk.android.mns.model.QueueMeta;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class SetQueueAttributesRequest extends MNSRequest {
    // 队列名称
    private String queueName;
    private QueueMeta queueMeta;

    public SetQueueAttributesRequest(String queueName){
        setQueueName(queueName);
    }

    private void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName(){
        return queueName;
    }

    public void setQueueMeta(QueueMeta queueMeta) {this.queueMeta = queueMeta;}

    public QueueMeta getQueueMeta() { return queueMeta;}
}
