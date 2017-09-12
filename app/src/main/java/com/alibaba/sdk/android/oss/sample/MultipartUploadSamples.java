package com.alibaba.sdk.android.oss.sample;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.app.MainActivity;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by zhouzhuo on 12/4/15.
 */
public class MultipartUploadSamples {

    private String asyncLog = "asyncMultipartUpload";
    private String syncLog = "MultipartUpload";

    private OSS oss;
    private String testBucket;
    private String testObject;
    private String uploadFilePath;

    //current asynchronous task count
    private int asyncTaskCount = 0;
    //lock object for async handling
    private Object lock = new Object();
    private WeakReference<Handler> handler;

    public MultipartUploadSamples(OSS client, String testBucket, String testObject, String uploadFilePath,Handler handler) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
        this.uploadFilePath = uploadFilePath;
        this.handler = new WeakReference<Handler>(handler);
    }

    public void multipartUpload() throws ClientException, ServiceException, IOException {

        long startTime  = System.currentTimeMillis();

        String uploadId;

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(testBucket, testObject);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        uploadId = initResult.getUploadId();

        // part size is 2MB
        long partSize = 10 * 1024 * 1024;

        int currentIndex = 1;

        File uploadFile = new File(uploadFilePath);
        InputStream input = new FileInputStream(uploadFile);
        long fileLength = uploadFile.length();

        long uploadedLength = 0;
        List<PartETag> partETags = new ArrayList<PartETag>();
        while (uploadedLength < fileLength) {
            int partLength = (int)Math.min(partSize, fileLength - uploadedLength);
            byte[] partData = IOUtils.readStreamAsBytesArray(input, partLength);

            UploadPartRequest uploadPart = new UploadPartRequest(testBucket, testObject, uploadId, currentIndex);
            uploadPart.setPartContent(partData);
            UploadPartResult uploadPartResult = oss.uploadPart(uploadPart);
            partETags.add(new PartETag(currentIndex, uploadPartResult.getETag()));

            uploadedLength += partLength;
            currentIndex++;
        }

        CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(testBucket, testObject, uploadId, partETags);
        CompleteMultipartUploadResult completeResult = oss.completeMultipartUpload(complete);

        Log.d(syncLog, "multipart upload success!success Location: " + completeResult.getLocation());

        Log.d(syncLog, "multipartUpload end spend time " + (System.currentTimeMillis() - startTime));
    }


    public void asyncMultipartUpload(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime  = System.currentTimeMillis();
                    Log.d(asyncLog, "asyncMultipartUpload start");

                    String uploadId;
                    //初始化需要上传的文件
                    InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(testBucket, testObject);
                    InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

                    uploadId = initResult.getUploadId();


                    //part size is 10MB
                    long partSize = 10 * 1024 * 1024;

                    int currentIndex = 1;

                    File uploadFile = new File(uploadFilePath);
                    InputStream input = new FileInputStream(uploadFile);
                    long fileLength = uploadFile.length();


                    long chucklength = fileLength % partSize == 0 ? fileLength / partSize : fileLength / partSize + 1;

                    Log.d(asyncLog, "chucklength : " + chucklength);

                    long uploadedLength = 0;
                    final List<PartETag> partETags = new ArrayList<PartETag>();

                    //开始分片上传
                    while (uploadedLength < fileLength) {
                        int partLength = (int)Math.min(partSize, fileLength - uploadedLength);
                        byte[] partData = IOUtils.readStreamAsBytesArray(input, partLength);

                        UploadPartRequest uploadPart = new UploadPartRequest(testBucket, testObject, uploadId, currentIndex);
                        uploadPart.setPartContent(partData);


                        // // Set the success and failure callback and then send the async request
                        OSSAsyncTask task = oss.asyncUploadPart(uploadPart, new OSSCompletedCallback<UploadPartRequest, UploadPartResult>() {
                            @Override
                            public void onSuccess(UploadPartRequest request, UploadPartResult result) {
                                //onSuccess is called in the background thread. Needs the synchronized call to serialize the insert into partETags
                                synchronized(lock){
                                    Log.d(asyncLog, "PartNumber ： " + request.getPartNumber() + " Success! \n" + " ETag ：" + result.getETag());
                                    int partNumber = request.getPartNumber();
                                    int index = -1;
                                    for (int i = 0; i < partETags.size() ; i++){
                                        PartETag partETag = partETags.get(i);
                                        if (partNumber < partETag.getPartNumber()){
                                            index = i;
                                            break;
                                        }
                                    }
                                    if(index > 0){
                                        partETags.add(index,new PartETag(partNumber, result.getETag()));
                                    }else {
                                        partETags.add(new PartETag(partNumber, result.getETag()));
                                    }
                                    asyncTaskCount --;
                                }
                            }

                            @Override
                            public void onFailure(UploadPartRequest request, ClientException clientExcepion, ServiceException serviceException) {
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

                        uploadedLength += partLength;
                        currentIndex++;
                        asyncTaskCount ++;
                        Log.d(asyncLog, "currentIndex : " + currentIndex);

                        try{
                            while (asyncTaskCount > OSSConstants.DEFAULT_BASE_THREAD_POOL_SIZE * 2){
                                Log.d(asyncLog, "asyncTaskCount : " + asyncTaskCount);
                                synchronized(lock) {
                                    lock.wait(1000);
                                }
                            }
                        }catch(Exception e){

                        }
                    }
                    input.close();
                    try{
                        while (partETags.size() < chucklength){
                            Log.d(asyncLog, "partETags.size() : " + partETags.size());
                            synchronized(lock) {
                                lock.wait(500);
                            }
                        }
                    }catch(Exception e){

                    }

                    Log.d(asyncLog, "all task Success!");


                    //分片完成后的接口调用,完成分片上传
                    CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(testBucket, testObject, uploadId, partETags);
                    CompleteMultipartUploadResult completeResult = oss.completeMultipartUpload(complete);

                    Log.d(asyncLog, "multipart upload success! Location: " + completeResult.getLocation());
                    Log.d(asyncLog, "asyncUploadPart end spend time " + (System.currentTimeMillis() - startTime));

                    handler.get().sendEmptyMessage(MainActivity.MULTIPART_SUC);
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.get().sendEmptyMessage(MainActivity.FAIL);
                }
            }
        }).start();

    }

}
