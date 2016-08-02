package com.alibaba.sdk.android.mns.model.result;

import com.alibaba.sdk.android.mns.model.MNSResult;

/**
 * Created by pan.zengp on 2016/7/4.
 */
public class CreateQueueResult extends MNSResult {
    private String queueLocation;

    public void setQueueLocation(String location){
        this.queueLocation = location;
    }

    public String getQueueLocation(){
        return queueLocation;
    }
}
