package com.alibaba.sdk.android.oss.sample;

import android.os.Environment;
import android.os.Handler;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.app.MainActivity;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class ResuambleUploadSamples extends BaseSamples{

    private String testBucket;
    private String testObject;
    private String uploadFilePath;
    private WeakReference<Handler> handler;

    public ResuambleUploadSamples(OSS client, String testBucket, String testObject, String uploadFilePath,Handler handler) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
        this.uploadFilePath = uploadFilePath;
        this.handler = new WeakReference<Handler>(handler);
    }

    // Resumable upload without checkpoint directory.
    public void resumableUpload() {
        OSSLog.logDebug("thread",Thread.currentThread().getName());
        // Creates the request
        ResumableUploadRequest request = new ResumableUploadRequest(testBucket, testObject, uploadFilePath);
        request.setCRC64(OSSRequest.CRC64Config.YES);
        // sets the callback
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("resumableUpload", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        // call the async upload
        OSSAsyncTask resumableTask = oss.asyncResumableUpload(request, new OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult>() {
            @Override
            public void onSuccess(ResumableUploadRequest request, ResumableUploadResult result) {
                OSSLog.logDebug("resumableUpload", "success!");
                handler.get().sendEmptyMessage(MainActivity.RESUMABLE_SUC);
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
                    OSSLog.logError("ErrorCode", serviceException.getErrorCode());
                    OSSLog.logError("RequestId", serviceException.getRequestId());
                    OSSLog.logError("HostId", serviceException.getHostId());
                    OSSLog.logError("RawMessage", serviceException.getRawMessage());
                }
                handler.get().sendEmptyMessage(MainActivity.FAIL);
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
                OSSLog.logDebug("resumableUpload", "currentSize: " + currentSize + " totalSize: " + totalSize, false);
            }
        });


        OSSAsyncTask resumableTask = oss.asyncResumableUpload(request, new OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult>() {
            @Override
            public void onSuccess(ResumableUploadRequest request, ResumableUploadResult result) {
                OSSLog.logDebug("resumableUpload", "success!");
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
                    OSSLog.logError("ErrorCode", serviceException.getErrorCode());
                    OSSLog.logError("RequestId", serviceException.getRequestId());
                    OSSLog.logError("HostId", serviceException.getHostId());
                    OSSLog.logError("RawMessage", serviceException.getRawMessage());
                }
            }
        });

        resumableTask.waitUntilFinished();
    }

    public void setTestBucket(String testBucket) {
        this.testBucket = testBucket;
    }

    public void setTestObject(String testObject) {
        this.testObject = testObject;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }
}
