package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;
import com.alibaba.sdk.android.mns.model.QueueMeta;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class GetQueueAttributesRequest extends MNSRequest {
    private String queueName;
    private QueueMeta queueMeta;

    public GetQueueAttributesRequest(String queueName){
        setQueueName(queueName);
    }

    private void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName(){
        return queueName;
    }
}
