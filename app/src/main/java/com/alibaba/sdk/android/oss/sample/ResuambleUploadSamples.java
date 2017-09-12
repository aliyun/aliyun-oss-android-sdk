package com.alibaba.sdk.android.oss.sample;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.app.MainActivity;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;

import java.io.File;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class ResuambleUploadSamples {

    private OSS oss;
    private String testBucket;
    private String testObject;
    private String uploadFilePath;

    public ResuambleUploadSamples(OSS client, String testBucket, String testObject, String uploadFilePath) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
        this.uploadFilePath = uploadFilePath;
    }

    // Resumable upload without checkpoint directory.
    public void resumableUpload(final Handler handler) {
        Log.d("thread",Thread.currentThread().getName());
        // Creates the request
        ResumableUploadRequest request = new ResumableUploadRequest(testBucket, testObject, uploadFilePath);
        // sets the callback
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                Log.d("resumableUpload", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        // call the async upload
        OSSAsyncTask resumableTask = oss.asyncResumableUpload(request, new OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult>() {
            @Override
            public void onSuccess(ResumableUploadRequest request, ResumableUploadResult result) {
                Log.d("resumableUpload", "success!");
                handler.sendEmptyMessage(MainActivity.RESUMABLE_SUC);
            }

            @Override
            public void onFailure(ResumableUploadRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
                handler.sendEmptyMessage(MainActivity.FAIL);
            }
        });
    }

    // resumable upload with checkpoint directory.
    public void resumableUploadWithRecordPathSetting() {

        String recordDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/oss_record/";

        File recordDir = new File(recordDirectory);

        // If the directory does not exist, creates one.
        if (!recordDir.exists()) {
            recordDir.mkdirs();
        }

        // Creates the request with checkpoint directory--it is an absolute directory.
        ResumableUploadRequest request = new ResumableUploadRequest(testBucket, testObject, uploadFilePath, recordDirectory);
        // Sets the callback
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                Log.d("resumableUpload", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });


        OSSAsyncTask resumableTask = oss.asyncResumableUpload(request, new OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult>() {
            @Override
            public void onSuccess(ResumableUploadRequest request, ResumableUploadResult result) {
                Log.d("resumableUpload", "success!");
            }

            @Override
            public void onFailure(ResumableUploadRequest request, ClientException clientExcepion, ServiceException serviceException) {
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

        resumableTask.waitUntilFinished();
    }
}
