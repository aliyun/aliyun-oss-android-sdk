package com.alibaba.sdk.android.mns.model.result;

import com.alibaba.sdk.android.mns.model.MNSResult;
import com.alibaba.sdk.android.mns.model.PagingListResult;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class ListQueueResult extends MNSResult {
    private PagingListResult<String> queueLists;

    public void setQueueLists(PagingListResult<String> queueLists) {
        this.queueLists = queueLists;
    }

    public PagingListResult<String> getQueueLists() {
        return queueLists;
    }
}
