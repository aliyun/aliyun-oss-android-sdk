package com.alibaba.sdk.android.oss.sample;

import android.util.Log;

import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;

import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class ManageObjectSamples {

    private OSS oss;
    private String testBucket;
    private String testObject;

    public ManageObjectSamples(OSS client, String testBucket, String testObject) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
    }

    // Checks if the object exists
    public void checkIsObjectExist() {
        try {
            if (oss.doesObjectExist(testBucket, testObject)) {
                Log.d("doesObjectExist", "object exist.");
            } else {
                Log.d("doesObjectExist", "object does not exist.");
            }
        } catch (ClientException e) {
            // client side exception,  such as network exception
            e.printStackTrace();
        } catch (ServiceException e) {
            // service side exception
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("RequestId", e.getRequestId());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
        }
    }

    // Gets file's metadata
    public void headObject() {
        // Creates a request to get the file's metadata
        HeadObjectRequest head = new HeadObjectRequest(testBucket, testObject);

        OSSAsyncTask task = oss.asyncHeadObject(head, new OSSCompletedCallback<HeadObjectRequest, HeadObjectResult>() {
            @Override
            public void onSuccess(HeadObjectRequest request, HeadObjectResult result) {
                Log.d("headObject", "object Size: " + result.getMetadata().getContentLength());
                Log.d("headObject", "object Content Type: " + result.getMetadata().getContentType());
            }

            @Override
            public void onFailure(HeadObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // request exception
                if (clientExcepion != null) {
                    // client side exception,  such as network exception
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

        task.waitUntilFinished();
    }

    // Copies the object to a new one and then deletes the new object.
    public void CopyAndDeleteObject() {
        // Creates the copy request
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(testBucket, testObject,
                testBucket, "testCopy");
        // Sets the target file's content-type
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("application/octet-stream");
        copyObjectRequest.setNewObjectMetadata(objectMetadata);

        try {
            // copy the file (in the synchronous way)
            CopyObjectResult copyResult = oss.copyObject(copyObjectRequest);
            // get the metadata of the new file
            HeadObjectRequest head = new HeadObjectRequest(testBucket, "testCopy");
            HeadObjectResult result = oss.headObject(head);

            // delete the file
            DeleteObjectRequest delete = new DeleteObjectRequest(testBucket, "testCopy");
            DeleteObjectResult deleteResult = oss.deleteObject(delete);
            if (deleteResult.getStatusCode() == 204) {
                Log.d("CopyAndDeleteObject", "Success.");
            }
        }
        // client side exception
        catch (ClientException e) {
            e.printStackTrace();
        }
        // service side exception
        catch (ServiceException e) {
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("RequestId", e.getRequestId());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
        }

    }

    // Copies the object to a new one and then deletes the new object.
    // Use the asynchronous API.
    public void asyncCopyAndDeleteObject() {
        // Creates the copy request
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(testBucket, testObject,
                testBucket, "testCopy");

        //Sets the new file's content-type
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("application/octet-stream");
        copyObjectRequest.setNewObjectMetadata(objectMetadata);

        // copy the file in the asynchronous way.
        OSSAsyncTask copyTask = oss.asyncCopyObject(copyObjectRequest, new OSSCompletedCallback<CopyObjectRequest, CopyObjectResult>() {
            @Override
            public void onSuccess(CopyObjectRequest request, CopyObjectResult result) {
                    Log.d("copyObject", "copy success!");
            }

            @Override
            public void onFailure(CopyObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // request exception
                if (clientExcepion != null) {
                    // client side exception,  such as network exception
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

        // Waits until the operation finished.
        copyTask.waitUntilFinished();

        // Creates the file deletion request
        DeleteObjectRequest delete = new DeleteObjectRequest(testBucket, "testCopy");
        // Deletes the file asynchronously.
        OSSAsyncTask deleteTask = oss.asyncDeleteObject(delete, new OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult>() {
            @Override
            public void onSuccess(DeleteObjectRequest request, DeleteObjectResult result) {
                Log.d("asyncCopyAndDelObject", "success!");
            }

            @Override
            public void onFailure(DeleteObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                // request exception
                if (clientExcepion != null) {
                    // client side exception,  such as network exception
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
        deleteTask.waitUntilFinished();
    }
}
