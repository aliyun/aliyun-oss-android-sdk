package com.alibaba.sdk.android.oss.sample;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.app.ProgressCallback;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class PutObjectSamples extends BaseSamples {

    private String testBucket;
    private String testObject;
    private String uploadFilePath;

    public PutObjectSamples(OSS client, String testBucket, String testObject, String uploadFilePath) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
        this.uploadFilePath = uploadFilePath;
    }

    // upload from local files. Use synchronous API
    public void putObjectFromLocalFile() {
        // Creates the upload request
        PutObjectRequest put = new PutObjectRequest(testBucket, testObject, uploadFilePath);

        try {
            PutObjectResult putResult = oss.putObject(put);
            OSSLog.logError("PutObject", "UploadSuccess");
            OSSLog.logError("ETag", putResult.getETag());
            OSSLog.logError("RequestId", putResult.getRequestId());
        } catch (ClientException e) {
            // client side exception,  such as network exception
            e.printStackTrace();
        } catch (ServiceException e) {
            // service side exception
            OSSLog.logError("RequestId", e.getRequestId());
            OSSLog.logError("ErrorCode", e.getErrorCode());
            OSSLog.logError("HostId", e.getHostId());
            OSSLog.logError("RawMessage", e.getRawMessage());
        }
    }

    // upload from local files. Use asynchronous API
    public void asyncPutObjectFromLocalFile(final ProgressCallback<PutObjectRequest, PutObjectResult> progressCallback) {
        // Creates the upload request
        PutObjectRequest put = new PutObjectRequest(testBucket, testObject, uploadFilePath);

        // Sets the progress callback and upload file asynchronously
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                progressCallback.onProgress(request, currentSize, totalSize);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                progressCallback.onSuccess(request, result);
                OSSLog.logDebug("PutObject", "UploadSuccess");
                OSSLog.logDebug("ETag", result.getETag());
                OSSLog.logDebug("RequestId", result.getRequestId());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                progressCallback.onFailure(request, clientExcepion, serviceException);
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
    }

    // Uploads from in-memory data. Use synchronous API
    public void putObjectFromByteArray() {
        // Creates the test data
        byte[] uploadData = new byte[100 * 1024];
        new Random().nextBytes(uploadData);

        // Creates the request to upload data
        PutObjectRequest put = new PutObjectRequest(testBucket, testObject, uploadData);

        try {
            PutObjectResult putResult = oss.putObject(put);

            OSSLog.logDebug("PutObject", "UploadSuccess");

            OSSLog.logDebug("ETag", putResult.getETag());
            OSSLog.logDebug("RequestId", putResult.getRequestId());
        } catch (ClientException e) {
            // client side exception,  such as network exception
            e.printStackTrace();
        } catch (ServiceException e) {
            // service side exception
            OSSLog.logError("RequestId", e.getRequestId());
            OSSLog.logError("ErrorCode", e.getErrorCode());
            OSSLog.logError("HostId", e.getHostId());
            OSSLog.logError("RawMessage", e.getRawMessage());
        }
    }

    // upload file with specified content-type and metadata.
    public void putObjectWithMetadataSetting() {
        // Creates the request object
        PutObjectRequest put = new PutObjectRequest(testBucket, testObject, uploadFilePath);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");
        metadata.addUserMetadata("x-oss-meta-name1", "value1");

        put.setMetadata(metadata);

        // sets the progress callback
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                OSSLog.logDebug("PutObject", "UploadSuccess");

                OSSLog.logDebug("ETag", result.getETag());
                OSSLog.logDebug("RequestId", result.getRequestId());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
    }

    // Uploads file with server side callback
    public void asyncPutObjectWithServerCallback() {
        // Creates the request object
        final PutObjectRequest put = new PutObjectRequest(testBucket, testObject, uploadFilePath);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");

        put.setMetadata(metadata);

        put.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", "110.75.82.106/mbaas/callback");
                put("callbackBody", "test");
            }
        });

        // Sets the progress callback
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                OSSLog.logDebug("PutObject", "UploadSuccess");

                // getServerCallbackReturnBody returns the data only when servercallback is set.
                // It's the callback response.
                String serverCallbackReturnJson = result.getServerCallbackReturnBody();

                OSSLog.logDebug("servercallback", serverCallbackReturnJson);
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
    }

    public void asyncPutObjectWithMD5Verify() {
        // Creates the object
        PutObjectRequest put = new PutObjectRequest(testBucket, testObject, uploadFilePath);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");
        try {
            // Sets MD5 value
            metadata.setContentMD5(BinaryUtil.calculateBase64Md5(uploadFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        put.setMetadata(metadata);

        // Sets the callback
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                OSSLog.logDebug("PutObject", "UploadSuccess");

                OSSLog.logDebug("ETag", result.getETag());
                OSSLog.logDebug("RequestId", result.getRequestId());
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
    }

    // Appends file to the OSS object.
    public void appendObject() {
        // If the object key exists, delete it.
        try {
            DeleteObjectRequest delete = new DeleteObjectRequest(testBucket, testObject);
            DeleteObjectResult result = oss.deleteObject(delete);
        } catch (ClientException clientException) {
            clientException.printStackTrace();
        } catch (ServiceException serviceException) {
            OSSLog.logError("ErrorCode", serviceException.getErrorCode());
            OSSLog.logError("RequestId", serviceException.getRequestId());
            OSSLog.logError("HostId", serviceException.getHostId());
            OSSLog.logError("RawMessage", serviceException.getRawMessage());
        }
        AppendObjectRequest append = new AppendObjectRequest(testBucket, testObject, uploadFilePath);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/octet-stream");
        append.setMetadata(metadata);

        // Sets the start position for the first call. It's the new file and thus the start position is 0.
        append.setPosition(0);

        append.setProgressCallback(new OSSProgressCallback<AppendObjectRequest>() {
            @Override
            public void onProgress(AppendObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("AppendObject", "currentSize: " + currentSize + " totalSize: " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncAppendObject(append, new OSSCompletedCallback<AppendObjectRequest, AppendObjectResult>() {
            @Override
            public void onSuccess(AppendObjectRequest request, AppendObjectResult result) {
                OSSLog.logDebug("AppendObject", "AppendSuccess");
                OSSLog.logDebug("NextPosition", "" + result.getNextPosition());
            }

            @Override
            public void onFailure(AppendObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
