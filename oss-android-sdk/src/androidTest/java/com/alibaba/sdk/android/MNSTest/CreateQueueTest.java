package com.alibaba.sdk.android.MNSTest;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.mns.MNS;
import com.alibaba.sdk.android.mns.MNSClient;
import com.alibaba.sdk.android.mns.common.MNSLog;
import com.alibaba.sdk.android.mns.internal.MNSAsyncTask;
import com.alibaba.sdk.android.mns.model.QueueMeta;
import com.alibaba.sdk.android.mns.model.request.CreateQueueRequest;
import com.alibaba.sdk.android.mns.model.request.DeleteQueueRequest;

/**
 * Created by pan.zengp on 2016/7/28.
 */
public class CreateQueueTest extends AndroidTestCase {
        MNS mns;

        @Override
        public void setUp() throws Exception{
            if (mns == null) {
                Thread.sleep(5 * 1000);
                MNSLog.enableLog();
                mns = new MNSClient(getContext(), MNSTestConfig.ENDPOINT, MNSTestConfig.credentialProvider);
            }
        }

        public void testCreateQueue() throws Exception {
            CreateQueueRequest request = new CreateQueueRequest(MNSTestConfig.QUEUE_NAME);
            QueueMeta queueMeta = new QueueMeta();
            queueMeta.setVisibilityTimeout(60L);
            queueMeta.setLoggingEnabled(1);
            request.setQueueMeta(queueMeta);
            MNSTestConfig.TestCreateQueueCallback callback = new MNSTestConfig.TestCreateQueueCallback();
            MNSAsyncTask task = mns.asyncCreateQueue(request, callback);
            task.waitUntilFinished();
            assertNull(callback.serviceException);
            assertNull(callback.clientException);
            assertTrue(callback.result.getStatusCode() == 204 || callback.result.getStatusCode() == 201 );

            DeleteQueueRequest deleteRequest = new DeleteQueueRequest(MNSTestConfig.QUEUE_NAME);
            MNSTestConfig.TestDeleteQueueCallback deleteQueueCallback = new MNSTestConfig.TestDeleteQueueCallback();
            MNSAsyncTask deleteTask = mns.asyncDeleteQueue(deleteRequest, deleteQueueCallback);
            deleteTask.waitUntilFinished();
            assertNull(deleteQueueCallback.serviceException);
            assertNull(deleteQueueCallback.clientException);
            assertEquals(204, deleteQueueCallback.result.getStatusCode());
        }
}
