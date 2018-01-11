package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PartSummary;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class MultipartUploadTest extends AndroidTestCase {

    OSS oss;
    String MULTIPART_OBJECTKEY_10M = "multipart10m";
    String MULTIPART_OBJECTKEY_1M = "multipart1m";

    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
    }

    public void testAsyncInitAndDeleteMultipartUpload() throws Exception {
        String objectKey = "multipart";
        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);

        OSSTestConfig.TestInitiateMultipartCallback initiateMultipartCallback = new OSSTestConfig.TestInitiateMultipartCallback();
        OSSAsyncTask<InitiateMultipartUploadResult> initTask = oss.asyncInitMultipartUpload(init, initiateMultipartCallback);
        initTask.waitUntilFinished();

        assertNotNull(initiateMultipartCallback.result.getUploadId());
        String uploadId = initiateMultipartCallback.result.getUploadId();

        AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);

        OSSTestConfig.TestAbortMultipartCallback abortMultipartCallback = new OSSTestConfig.TestAbortMultipartCallback();

        OSSAsyncTask<AbortMultipartUploadResult> abortTask = oss.asyncAbortMultipartUpload(abort, abortMultipartCallback);
        abortTask.waitUntilFinished();

        assertNotNull(abortMultipartCallback.request);

        ListPartsRequest listpart = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);
        listpart.setMaxParts(1000);
        listpart.setPartNumberMarker(1);

        OSSTestConfig.TestListPartsCallback listPartsCallback = new OSSTestConfig.TestListPartsCallback();

        OSSAsyncTask<ListPartsResult> partsTask = oss.asyncListParts(listpart, listPartsCallback);
        partsTask.waitUntilFinished();

        assertNotNull(listPartsCallback.serviceException);
    }

    public void testInitAndDeleteMultipartUpload() throws Exception {
        String objectKey = "multipart";
        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        OSSLog.logDebug(initResult.getBucketName(), false);
        OSSLog.logDebug(initResult.getObjectKey(), false);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        OSSLog.logDebug("uploadid - " + uploadId);
        AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);
        AbortMultipartUploadResult abortResult = oss.abortMultipartUpload(abort);

        assertNotNull(abortResult);

        ListPartsRequest listpart = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);
        listpart.setMaxParts(1000);
        listpart.setPartNumberMarker(1);
        try {
            oss.listParts(listpart);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
    }

    public void testInitAndListEmptyUploadId() throws Exception {
        String objectKey = "multipart";
        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        OSSLog.logDebug("uploadid - " + uploadId);

        ListPartsRequest listpart = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);
        ListPartsResult listResult = oss.listParts(listpart);
        assertEquals(0, listResult.getParts().size());

        AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);
        AbortMultipartUploadResult abortResult = oss.abortMultipartUpload(abort);

        assertNotNull(abortResult);
    }

    public void testAsyncUploadPartsAndListAndComplete() throws Exception {
        String objectKey = "multipart";

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        byte[] data = new byte[100 * 1024];
        UploadPartRequest uploadPart = new UploadPartRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, 1);
        uploadPart.setPartContent(data);

        OSSTestConfig.TestUploadPartsCallback uploadPartsCallback = new OSSTestConfig.TestUploadPartsCallback();

        OSSAsyncTask<UploadPartResult> part1Tast = oss.asyncUploadPart(uploadPart, uploadPartsCallback);
        part1Tast.waitUntilFinished();

        uploadPart = new UploadPartRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId, 2);
        uploadPart.setPartContent(data);

        OSSTestConfig.TestUploadPartsCallback uploadPartsCallback1 = new OSSTestConfig.TestUploadPartsCallback();

        OSSAsyncTask<UploadPartResult> part2Tast = oss.asyncUploadPart(uploadPart, uploadPartsCallback1);
        part2Tast.waitUntilFinished();

        ListPartsRequest listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        ListPartsResult result = oss.listParts(listParts);

        for (int i = 0; i < result.getParts().size(); i++) {
            OSSLog.logDebug("listParts", "partNum: " + result.getParts().get(i).getPartNumber(), false);
            OSSLog.logDebug("listParts", "partEtag: " + result.getParts().get(i).getETag(), false);
            OSSLog.logDebug("listParts", "lastModified: " + result.getParts().get(i).getLastModified(), false);
            OSSLog.logDebug("listParts", "partSize: " + result.getParts().get(i).getSize(), false);
        }

        assertEquals(2, result.getParts().size());

        List<PartETag> partETagList = new ArrayList<PartETag>();
        for (PartSummary part : result.getParts()) {
            partETagList.add(new PartETag(part.getPartNumber(), part.getETag()));
        }

        CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, partETagList);

        OSSTestConfig.TestCompleteMultipartCallback completeMultipartCallback = new OSSTestConfig.TestCompleteMultipartCallback();

        OSSAsyncTask<CompleteMultipartUploadResult> completeMultipartUpload = oss.asyncCompleteMultipartUpload(complete, completeMultipartCallback);
        completeMultipartUpload.waitUntilFinished();

        assertNotNull(completeMultipartCallback.result.getLocation());

        listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        try {
            oss.listParts(listParts);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
    }

    public void testUploadPartsAndListAndComplete() throws Exception {
        String objectKey = "multipart";

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        byte[] data = new byte[100 * 1024];
        UploadPartRequest uploadPart = new UploadPartRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, 1);
        uploadPart.setPartContent(data);

        oss.uploadPart(uploadPart);

        uploadPart = new UploadPartRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId, 2);
        uploadPart.setPartContent(data);

        oss.uploadPart(uploadPart);

        ListPartsRequest listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        ListPartsResult result = oss.listParts(listParts);

        for (int i = 0; i < result.getParts().size(); i++) {
            OSSLog.logDebug("listParts", "partNum: " + result.getParts().get(i).getPartNumber(), false);
            OSSLog.logDebug("listParts", "partEtag: " + result.getParts().get(i).getETag(), false);
            OSSLog.logDebug("listParts", "lastModified: " + result.getParts().get(i).getLastModified(), false);
            OSSLog.logDebug("listParts", "partSize: " + result.getParts().get(i).getSize(), false);
        }
        OSSLog.logDebug("listParts", "bucketName: " + result.getBucketName(), false);
        OSSLog.logDebug("listParts", "key: " + result.getKey(), false);
        OSSLog.logDebug("listParts", "uploadId: " + result.getUploadId(), false);
        OSSLog.logDebug("listParts", "PartNumberMarker: " + result.getPartNumberMarker(), false);
        OSSLog.logDebug("listParts", "NextPartNumberMarker: " + result.getNextPartNumberMarker(), false);
        OSSLog.logDebug("listParts", "MaxParts: " + result.getMaxParts(), false);
        OSSLog.logDebug("listParts", "StorageClass: " + result.getStorageClass(), false);
        OSSLog.logDebug("listParts", "isTruncated: " + result.isTruncated(), false);

        assertEquals(2, result.getParts().size());

        List<PartETag> partETagList = new ArrayList<PartETag>();
        for (PartSummary part : result.getParts()) {
            partETagList.add(new PartETag(part.getPartNumber(), part.getETag()));
        }

        CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, partETagList);

        CompleteMultipartUploadResult completeResult = oss.completeMultipartUpload(complete);

        assertNotNull(completeResult.getLocation());

        listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        try {
            oss.listParts(listParts);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
    }

    public void testUploadPartsAndCompleteWithServerCallback() throws Exception {
        String objectKey = "multipart";

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        byte[] data = new byte[100 * 1024];
        UploadPartRequest uploadPart = new UploadPartRequest();
        uploadPart.setBucketName(OSSTestConfig.ANDROID_TEST_BUCKET);
        uploadPart.setObjectKey(objectKey);
        uploadPart.setUploadId(uploadId);
        uploadPart.setPartNumber(1);
        uploadPart.setPartContent(data);

        oss.uploadPart(uploadPart);

        ListPartsRequest listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        ListPartsResult result = oss.listParts(listParts);
        assertEquals(1, result.getParts().size());

        List<PartETag> partETagList = new ArrayList<PartETag>();
        for (PartSummary part : result.getParts()) {
            partETagList.add(new PartETag(part.getPartNumber(), part.getETag()));
        }

        CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, partETagList);

        complete.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", OSSTestConfig.CALLBACK_SERVER); //修改自己的服务器地址
                put("callbackBody", "test");
            }
        });
        CompleteMultipartUploadResult completeResult = oss.completeMultipartUpload(complete);
        assertEquals(200, completeResult.getStatusCode());
        assertNotNull(completeResult.getServerCallbackReturnBody());
        OSSLog.logError("-------------- serverCallback: " + completeResult.getServerCallbackReturnBody());
    }

    public void testUploadPartsWithMd5Verify() throws Exception {
        String objectKey = "multipart";

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        byte[] data = new byte[100 * 1024];
        UploadPartRequest uploadPart = new UploadPartRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, 1);
        uploadPart.setPartContent(data);
        uploadPart.setMd5Digest(BinaryUtil.calculateBase64Md5(data));
        oss.uploadPart(uploadPart);

        ListPartsRequest listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        ListPartsResult result = oss.listParts(listParts);

        for (int i = 0; i < result.getParts().size(); i++) {
            OSSLog.logDebug("listParts", "partNum: " + result.getParts().get(i).getPartNumber(), false);
            OSSLog.logDebug("listParts", "partEtag: " + result.getParts().get(i).getETag(), false);
            OSSLog.logDebug("listParts", "lastModified: " + result.getParts().get(i).getLastModified(), false);
            OSSLog.logDebug("listParts", "partSize: " + result.getParts().get(i).getSize(), false);
        }
        assertEquals(1, result.getParts().size());

        List<PartETag> partETagList = new ArrayList<PartETag>();
        for (PartSummary part : result.getParts()) {
            partETagList.add(new PartETag(part.getPartNumber(), part.getETag()));
        }

        CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, partETagList);

        CompleteMultipartUploadResult completeResult = oss.completeMultipartUpload(complete);

        assertNotNull(completeResult.getLocation());

        listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        try {
            oss.listParts(listParts);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
    }

    public void testUploadPartsWithInvalidMd5Verify() throws Exception {
        String objectKey = "multipart";

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        byte[] data = new byte[100 * 1024];
        UploadPartRequest uploadPart = new UploadPartRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, 1);
        uploadPart.setPartContent(data);
        uploadPart.setMd5Digest("wrongMd5");
        try {
            oss.uploadPart(uploadPart);
        } catch (ServiceException serviceException) {
            assertEquals(serviceException.getStatusCode(), 400);
            assertEquals(serviceException.getErrorCode(), "InvalidDigest");
            AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);
            AbortMultipartUploadResult result = oss.abortMultipartUpload(abort);
            assertEquals(result.getStatusCode(), 204);
        }

    }


    public void testMultipartUpload() throws Exception {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setHeader("x-oss-object-acl", "public-read-write");
        MultipartUploadRequest rq = new MultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, MULTIPART_OBJECTKEY_10M,
                OSSTestConfig.FILE_DIR + "/file10m", meta);
        rq.setPartSize(1024 * 1024);
        rq.setProgressCallback(new OSSProgressCallback<MultipartUploadRequest>() {
            @Override
            public void onProgress(MultipartUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testMultipartUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        CompleteMultipartUploadResult result = oss.multipartUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());

        OSSTestConfig.checkFileMd5(oss, "file10m", OSSTestConfig.FILE_DIR + "/file10m");
    }

    private void multipartUpload10mToFile(final String fileName) throws Exception {
        MultipartUploadRequest request = new MultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, fileName,
                OSSTestConfig.FILE_DIR + "file10m");

        request.setProgressCallback(new OSSProgressCallback<MultipartUploadRequest>() {

            @Override
            public void onProgress(MultipartUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testMultipartUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestMultipartUploadCallback callback = new OSSTestConfig.TestMultipartUploadCallback();

        OSSAsyncTask task = oss.asyncMultipartUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestConfig.checkFileMd5(oss, fileName, OSSTestConfig.FILE_DIR + "/file10m");
    }

    public void testConcurrentMultipartUpload() throws Exception {
        final CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        multipartUpload10mToFile("multipartUpload" + index);
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        assertTrue(false);
                        latch.countDown();
                    }
                }
            }).start();
        }
        latch.await();
    }

    public void testMultipartUploadWithServerError() throws Exception {
        MultipartUploadRequest rq = new MultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, MULTIPART_OBJECTKEY_1M,
                OSSTestConfig.FILE_DIR + "/file1m");
        rq.setProgressCallback(new OSSProgressCallback<MultipartUploadRequest>() {
            @Override
            public void onProgress(MultipartUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testMultipartUpload] - " + currentSize + " " + totalSize, false);
            }
        });
        OSSCredentialProvider provider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                return "xxx";
            }
        };
        oss = new OSSClient(getContext(), "http://oss-cn-hangzhou.aliyuncs.com", provider);
        try {
            CompleteMultipartUploadResult result = oss.multipartUpload(rq);
        } catch (Exception e) {
            assertTrue(e instanceof ServiceException);
        }
    }

    public void testMultipartUploadFailed() throws Exception {
        MultipartUploadRequest request = new MultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, MULTIPART_OBJECTKEY_10M,
                OSSTestConfig.FILE_DIR + "/file10m");
        request.setPartSize(512 * 1024);
        request.setProgressCallback(new OSSProgressCallback<MultipartUploadRequest>() {
            @Override
            public void onProgress(MultipartUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testMultipartUpload] - " + currentSize + " " + totalSize, false);
                if (currentSize > totalSize / 2) {
                    throw new RuntimeException("Make you failed!");
                }
            }
        });

        OSSTestConfig.TestMultipartUploadCallback callback = new OSSTestConfig.TestMultipartUploadCallback();

        OSSAsyncTask task = oss.asyncMultipartUpload(request, callback);

        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);
    }

    public void testMultipartUploadCancel() throws Exception {
        MultipartUploadRequest request = new MultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, MULTIPART_OBJECTKEY_10M,
                OSSTestConfig.FILE_DIR + "/file10m");
        request.setPartSize(512 * 1024);
        request.setProgressCallback(new OSSProgressCallback<MultipartUploadRequest>() {
            @Override
            public void onProgress(MultipartUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testMultipartUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestMultipartUploadCallback callback = new OSSTestConfig.TestMultipartUploadCallback();

        OSSAsyncTask task = oss.asyncMultipartUpload(request, callback);

        Thread.sleep(500);
        task.cancel();
        task.waitUntilFinished();

        assertNotNull(callback.clientException);
        callback.clientException.printStackTrace();
    }

    public void testMultipartUploadWithErrorParts() {
        MultipartUploadRequest request = new MultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, MULTIPART_OBJECTKEY_10M,
                OSSTestConfig.FILE_DIR + "/file10m");

        try {
            request.setPartSize(1024);
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    public void testMultipartUploadWithServerCallback() throws Exception {
        MultipartUploadRequest rq = new MultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, MULTIPART_OBJECTKEY_1M,
                OSSTestConfig.FILE_DIR + "/file1m");
        rq.setCallbackParam(new HashMap<String, String>() {
            {
                put("callbackUrl", OSSTestConfig.CALLBACK_SERVER);
                put("callbackBody", "test");
            }
        });
        rq.setCallbackVars(new HashMap<String, String>() {
            {
                put("x:var1", "value");
            }
        });
        rq.setProgressCallback(new OSSProgressCallback<MultipartUploadRequest>() {
            @Override
            public void onProgress(MultipartUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testMultipartUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        CompleteMultipartUploadResult result = oss.multipartUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertNotNull(result.getServerCallbackReturnBody());
    }
}
