package com.alibaba.sdk.android.oss.sample;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.app.Callback;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.Range;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class GetObjectSamples {

    private OSS oss;
    private String testBucket;
    private String testObject;

    public GetObjectSamples(OSS client, String testBucket, String testObject) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
    }

    public void getObjectSample(final Callback<GetObjectRequest,GetObjectResult> callback) {

        // Constructs the GetObjectRequest.
        GetObjectRequest get = new GetObjectRequest(testBucket, testObject);
        GetObjectResult getResult = null;
        try {
            // Download the file in the synchronous way
            getResult = oss.getObject(get);
            callback.onSuccess(get,getResult);
            OSSLog.logDebug("Content-Length", "" + getResult.getContentLength());

            // Gets the file's input stream.
            InputStream inputStream = getResult.getObjectContent();

            byte[] buffer = new byte[2048];
            int len;

            while ((len = inputStream.read(buffer)) != -1) {
                // Process the downloaded data, here just print the total length
                OSSLog.logDebug("asyncGetObjectSample", "read length: " + len, false);
            }
            OSSLog.logDebug("asyncGetObjectSample", "download success.");

            // Looks up the metadata---it's included in the getResult object.
            ObjectMetadata metadata = getResult.getMetadata();
            OSSLog.logDebug("ContentType", metadata.getContentType());

        } catch (ClientException e) {
            // Client side exceptions, such as network exception
            e.printStackTrace();
            callback.onFailure(get,e,null);
        } catch (ServiceException e) {
            // Service side exception
            OSSLog.logError("RequestId", e.getRequestId());
            OSSLog.logError("ErrorCode", e.getErrorCode());
            OSSLog.logError("HostId", e.getHostId());
            OSSLog.logError("RawMessage", e.getRawMessage());
            callback.onFailure(get,null,e);
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailure(get,new ClientException(e),null);
        } finally {
            if(getResult != null){
                try {
                    getResult.getObjectContent().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void asyncGetObjectSample(final Callback<GetObjectRequest,GetObjectResult> callback) {

        GetObjectRequest get = new GetObjectRequest(testBucket, testObject);
        get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize+"  total_size: " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                callback.onSuccess(request,result);
                // request sucess
                InputStream inputStream = result.getObjectContent();

                byte[] buffer = new byte[2048];
                int len;

                try {
                    while ((len = inputStream.read(buffer)) != -1) {
                        // Process the downloaded data
                        OSSLog.logDebug("asyncGetObjectSample", "read length: " + len, false);
                    }
                    OSSLog.logDebug("asyncGetObjectSample", "download success.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                callback.onFailure(request,clientExcepion,serviceException);
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
    }

    public void asyncGetObjectRangeSample() {

        GetObjectRequest get = new GetObjectRequest(testBucket, testObject);

        // Sets the range to download
        get.setRange(new Range(0, 99)); // downloads first to 100th bytes.

        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                // The request succeeds, get the data
                InputStream inputStream = result.getObjectContent();

                byte[] buffer = new byte[2048];
                int len;

                try {
                    while ((len = inputStream.read(buffer)) != -1) {
                        // Process the downloaded data. Here just print the total length
                        OSSLog.logDebug("asyncGetObjectSample", "read length: " + len, false);
                    }
                    OSSLog.logDebug("asyncGetObjectSample", "download success.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
    }
}
