package com.alibaba.sdk.android.mns.model.result;

import com.alibaba.sdk.android.mns.model.MNSResult;
import com.alibaba.sdk.android.mns.model.QueueMeta;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class GetQueueAttributesResult extends MNSResult {
    private QueueMeta queueMeta;

    public void setQueueMeta(QueueMeta queueMeta) {
        this.queueMeta = queueMeta;
    }

    public QueueMeta getQueueMeta() {
        return queueMeta;
    }
}