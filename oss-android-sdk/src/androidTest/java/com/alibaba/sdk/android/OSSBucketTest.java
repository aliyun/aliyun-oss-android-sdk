package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSBucketTest extends AndroidTestCase {

    OSS oss;

    @Override
    public void setUp() throws Exception {
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credetialProvider);
        }
    }

    public void testAyncListObjects() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);

        OSSTestConfig.TestListObjectsCallback callback = new OSSTestConfig.TestListObjectsCallback();

        OSSAsyncTask task = oss.asyncListObjects(listObjects, callback);

        task.waitUntilFinished();

        assertEquals(20, callback.result.getObjectSummaries().size());
        for (int i = 0; i < callback.result.getObjectSummaries().size(); i++) {
            OSSLog.logD("object: " + callback.result.getObjectSummaries().get(i).getKey() + " "
                    + callback.result.getObjectSummaries().get(i).getETag() + " "
                    + callback.result.getObjectSummaries().get(i).getLastModified());
        }
    }

    public void testSyncListObjecs() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);

        ListObjectsResult result = oss.listObjects(listObjects);

        assertEquals(20, result.getObjectSummaries().size());
        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            OSSLog.logD("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }
    }

    public void testListObjectSettingPrefix() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);

        listObjects.setPrefix("file");

        ListObjectsResult result = oss.listObjects(listObjects);

        assertEquals(10, result.getObjectSummaries().size());

        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            OSSLog.logD("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }

        assertEquals(0, result.getCommonPrefixes().size());
    }

    public void testListObjectSettingPrefixAndDelimitate() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);

        listObjects.setPrefix("folder");
        listObjects.setDelimiter("/");

        ListObjectsResult result = oss.listObjects(listObjects);

        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            OSSLog.logD("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }

        for (int i = 0; i < result.getCommonPrefixes().size(); i++) {
            OSSLog.logD("prefixe: " + result.getCommonPrefixes().get(i));
        }

        assertEquals(0, result.getObjectSummaries().size());
        assertEquals(10, result.getCommonPrefixes().size());
    }
}
