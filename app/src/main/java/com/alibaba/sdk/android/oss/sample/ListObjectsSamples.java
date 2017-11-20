package com.alibaba.sdk.android.oss.sample;

import android.os.Handler;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.app.MainActivity;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;

import java.lang.ref.WeakReference;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class ListObjectsSamples {

    private OSS oss;
    private String testBucket;
    private WeakReference<Handler> handler;

    public ListObjectsSamples(OSS client, String testBucket,Handler handler) {
        this.oss = client;
        this.testBucket = testBucket;
        this.handler = new WeakReference<Handler>(handler);
    }

    // list the files under the bucket in the asynchronous way.
    public void AyncListObjects() {
        // Creates the request object to list objects
        ListObjectsRequest listObjects = new ListObjectsRequest(testBucket);
        // Sets the success or failure callback. Sends async request
        OSSAsyncTask task = oss.asyncListObjects(listObjects, new OSSCompletedCallback<ListObjectsRequest, ListObjectsResult>() {
            @Override
            public void onSuccess(ListObjectsRequest request, ListObjectsResult result) {
                OSSLog.logDebug("AyncListObjects", "Success!");
                for (int i = 0; i < result.getObjectSummaries().size(); i++) {
                    OSSLog.logDebug("AyncListObjects", "object: " + result.getObjectSummaries().get(i).getKey() + " "
                            + result.getObjectSummaries().get(i).getETag() + " "
                            + result.getObjectSummaries().get(i).getLastModified());
                }
            }

            @Override
            public void onFailure(ListObjectsRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // request exception
                if (clientExcepion != null) {
                    // client side exception
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // service side exception
                    OSSLog.logError("ErrorCode", serviceException.getErrorCode());
                    OSSLog.logError("RequestId", serviceException.getRequestId());
                    OSSLog.logError("HostId", serviceException.getHostId());
                    OSSLog.logError("RawMessage", serviceException.getRawMessage());
                }
            }
        });
        task.waitUntilFinished();
    }

    // lists the file with specified prefix/delimiter
    public void listObjectsWithPrefix() {
        ListObjectsRequest listObjects = new ListObjectsRequest(testBucket);
        // Sets the prefix
        listObjects.setPrefix("folder");
        listObjects.setDelimiter("/");

        try {
            // list the objects in the synchronous way.
            ListObjectsResult result = oss.listObjects(listObjects);
            for (int i = 0; i < result.getObjectSummaries().size(); i++) {
                OSSLog.logDebug("listObjectsWithPrefix", "object: " + result.getObjectSummaries().get(i).getKey() + " "
                        + result.getObjectSummaries().get(i).getETag() + " "
                        + result.getObjectSummaries().get(i).getLastModified());
            }

            for (int i = 0; i < result.getCommonPrefixes().size(); i++) {
                OSSLog.logDebug("listObjectsWithPrefix", "prefixes: " + result.getCommonPrefixes().get(i));
            }
        }
        catch (ClientException clientException) {
            clientException.printStackTrace();
        }
        catch (ServiceException serviceException) {
            OSSLog.logError("ErrorCode", serviceException.getErrorCode());
            OSSLog.logError("RequestId", serviceException.getRequestId());
            OSSLog.logError("HostId", serviceException.getHostId());
            OSSLog.logError("RawMessage", serviceException.getRawMessage());
        }
    }

    // Downloads the files with specified prefix in the asynchronous way.
    public void asyncListObjectsWithPrefix() {
        ListObjectsRequest listObjects = new ListObjectsRequest(testBucket);
        // Sets the prefix
        listObjects.setPrefix("file");
        listObjects.setDelimiter("/");
        // Sets the success and failure callback. calls the Async API
        OSSAsyncTask task = oss.asyncListObjects(listObjects, new OSSCompletedCallback<ListObjectsRequest, ListObjectsResult>() {
            @Override
            public void onSuccess(ListObjectsRequest request, ListObjectsResult result) {
                OSSLog.logDebug("AyncListObjects", "Success!");
                for (int i = 0; i < result.getObjectSummaries().size(); i++) {
                    OSSLog.logDebug("AyncListObjects", "object: " + result.getObjectSummaries().get(i).getKey() + " "
                            + result.getObjectSummaries().get(i).getETag() + " "
                            + result.getObjectSummaries().get(i).getLastModified());
                }
                handler.get().sendEmptyMessage(MainActivity.LIST_SUC);
            }

            @Override
            public void onFailure(ListObjectsRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // request exception
                if (clientExcepion != null) {
                    // client side exception such as network exception
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // service side exception.
                    OSSLog.logError("ErrorCode", serviceException.getErrorCode());
                    OSSLog.logError("RequestId", serviceException.getRequestId());
                    OSSLog.logError("HostId", serviceException.getHostId());
                    OSSLog.logError("RawMessage", serviceException.getRawMessage());
                }
                handler.get().sendEmptyMessage(MainActivity.FAIL);
            }
        });
    }
}
