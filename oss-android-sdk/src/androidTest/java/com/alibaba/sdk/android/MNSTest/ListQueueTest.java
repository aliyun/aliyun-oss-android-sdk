package com.alibaba.sdk.android.MNSTest;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.mns.MNS;
import com.alibaba.sdk.android.mns.MNSClient;
import com.alibaba.sdk.android.mns.common.MNSLog;
import com.alibaba.sdk.android.mns.internal.MNSAsyncTask;
import com.alibaba.sdk.android.mns.model.PagingListResult;
import com.alibaba.sdk.android.mns.model.request.ListQueueRequest;

import java.util.List;

/**
 * Created by pan.zengp on 2016/7/31.
 */
public class ListQueueTest extends AndroidTestCase {
    MNS mns;

    @Override
    public void setUp() throws Exception {
        if (mns == null) {
            Thread.sleep(5 * 1000);
            MNSLog.enableLog();
            mns = new MNSClient(getContext(), MNSTestConfig.ENDPOINT, MNSTestConfig.credentialProvider);
        }
    }

    public void testListQueue() throws Exception {
        ListQueueRequest request = new ListQueueRequest("", 100, "");
        MNSTestConfig.TestListQueueCallback listQueueCallback = new MNSTestConfig.TestListQueueCallback();
        MNSAsyncTask setQueueAttrTask = mns.asyncListQueue(request, listQueueCallback);
        setQueueAttrTask.waitUntilFinished();
        assertNull(listQueueCallback.clientException);
        assertNull(listQueueCallback.serviceException);
        assertEquals(200, listQueueCallback.result.getStatusCode());
    }
}
