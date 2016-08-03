package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;
import com.alibaba.sdk.android.mns.model.QueueMeta;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Created by pan.zengp on 2016/7/4.
 */
public class CreateQueueRequest extends MNSRequest {

    // 创建的队列名称
    private String queueName;
    private QueueMeta queueMeta;

    public CreateQueueRequest(String queueName){
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
