package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class DeleteQueueRequest extends MNSRequest {
    // 删除的队列名称
    private String queueName;

    public DeleteQueueRequest(String queueName){
        setQueueName(queueName);
    }

    private void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName(){
        return queueName;
    }

}
