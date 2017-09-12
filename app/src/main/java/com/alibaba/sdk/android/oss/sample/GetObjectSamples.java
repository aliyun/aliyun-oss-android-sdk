package com.alibaba.sdk.android.oss.sample;

import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
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

    public void getObjectSample() {

        // Constructs the GetObjectRequest.
        GetObjectRequest get = new GetObjectRequest(testBucket, testObject);

        try {
            // Download the file in the synchronous way
            GetObjectResult getResult = oss.getObject(get);

            Log.d("Content-Length", "" + getResult.getContentLength());

            // Gets the file's input stream.
            InputStream inputStream = getResult.getObjectContent();

            byte[] buffer = new byte[2048];
            int len;

            while ((len = inputStream.read(buffer)) != -1) {
                // Process the downloaded data, here just print the total length
                Log.d("asyncGetObjectSample", "read length: " + len);
            }
            Log.d("asyncGetObjectSample", "download success.");

            // Looks up the metadata---it's included in the getResult object.
            ObjectMetadata metadata = getResult.getMetadata();
            Log.d("ContentType", metadata.getContentType());


        } catch (ClientException e) {
            // Client side exceptions, such as network exception
            e.printStackTrace();
        } catch (ServiceException e) {
            // Service side exception
            Log.e("RequestId", e.getRequestId());
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void asyncGetObjectSample() {

        GetObjectRequest get = new GetObjectRequest(testBucket, testObject);

        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                // 请求成功
                InputStream inputStream = result.getObjectContent();

                byte[] buffer = new byte[2048];
                int len;

                try {
                    while ((len = inputStream.read(buffer)) != -1) {
                        // Process the downloaded data
                        Log.d("asyncGetObjectSample", "read length: " + len);
                    }
                    Log.d("asyncGetObjectSample", "download success.");
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
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
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
                        Log.d("asyncGetObjectSample", "read length: " + len);
                    }
                    Log.d("asyncGetObjectSample", "download success.");
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
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });
    }
}
