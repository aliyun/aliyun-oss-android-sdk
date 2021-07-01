package com.alibaba.sdk.android;


import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

/**
 * Created by jingdan on 2017/11/29.
 */

@RunWith(AndroidJUnit4.class)
public class SHA1Test {

    public static final String ANDROID_TEST_BUCKET = "zq-hangzhou1";

    private final static String UPLOAD_BIGFILE = "bigfile.zip";
    private static String objectname = "sequence-object";
    private static String testFile = "guihua.zip";
    private static OSS oss;


    @BeforeClass
    public static void setUp() throws Exception {
        OSSTestConfig.instance(InstrumentationRegistry.getTargetContext());
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
        try {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(ANDROID_TEST_BUCKET);
            oss.createBucket(createBucketRequest);
        } catch (Exception e) {}
        OSSTestConfig.initLocalFile();
        OSSTestConfig.initDemoFile(testFile);
        OSSTestConfig.copyFilesFassets(InstrumentationRegistry.getContext(), "guihua.zip", true);
    }

    @AfterClass
    public static void dealloc() throws Exception {
        OSSTestUtils.cleanBucket(oss, ANDROID_TEST_BUCKET);
    }

    @Test
    @SdkSuppress(minSdkVersion = 29)
    public void testPutObjectFromUriCheckSHA1() throws Exception {
        Uri uri = OSSTestConfig.queryUri(testFile);
        PutObjectRequest put = new PutObjectRequest(ANDROID_TEST_BUCKET, objectname, uri);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        FileDescriptor fileDescriptor = InstrumentationRegistry.getTargetContext().getContentResolver().openFileDescriptor(uri, "r").getFileDescriptor();
        ObjectMetadata metadata = new ObjectMetadata();
        String sha1Value = BinaryUtil.fileToSHA1(fileDescriptor);
        metadata.setSHA1(sha1Value);
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
    }

    @Test
    @Ignore
    public void testPutObjectCheckSHA1() throws Exception {
        String fileName = testFile;
        PutObjectRequest put = new PutObjectRequest(ANDROID_TEST_BUCKET, objectname,
                OSSTestConfig.FILE_DIR + fileName);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        ObjectMetadata metadata = new ObjectMetadata();
        String sha1Value = BinaryUtil.fileToSHA1(OSSTestConfig.FILE_DIR + fileName);
        metadata.setSHA1(sha1Value);
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
    }

    @Test
    public void testPutObjectWithErrorSHA1() throws Exception {
        String fileName = testFile;
        PutObjectRequest put = new PutObjectRequest(ANDROID_TEST_BUCKET, objectname,
                OSSTestConfig.FILE_DIR + fileName);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setSHA1("error sha1value");
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertNotNull(putCallback.serviceException);
        OSSLog.logError("serviceException: " + putCallback.serviceException.toString());
    }

    @Test
    public void testSequenceUpload() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(ANDROID_TEST_BUCKET, objectname,
                OSSTestConfig.FILE_DIR + testFile);
        rq.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        ResumableUploadResult result = oss.sequenceUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());

        OSSTestUtils.checkFileMd5(oss, ANDROID_TEST_BUCKET, objectname, OSSTestConfig.FILE_DIR + testFile);
    }

    @Test
    public void testSequenceUploadCancelledAndResume() throws Exception {
        final String objectKey = objectname;
        ResumableUploadRequest request = new ResumableUploadRequest(ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.FILE_DIR + testFile, OSSTestConfig.FILE_DIR);

        request.setDeleteUploadOnCancelling(false);
        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(objectKey, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
                if (currentSize > totalSize / 3) {
                    needCancelled.set(true);
                }
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncSequenceUpload(request, callback);

        while (!needCancelled.get()) {
            Thread.sleep(100);
        }
        task.cancel();
        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);
        OSSLog.logError("clientException: " + callback.clientException.toString());

        request = new ResumableUploadRequest(ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.FILE_DIR + testFile, OSSTestConfig.FILE_DIR);

//        ObjectMetadata metadata = new ObjectMetadata();
//        String sha1Value = BinaryUtil.fileToSHA1(OSSTestConfig.FILE_DIR + testFile);
//        metadata.setSHA1(sha1Value);
//        request.setMetadata(metadata);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {


            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(objectKey, request.getObjectKey());
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
                assertTrue(currentSize > totalSize / 3);

            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncSequenceUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestUtils.checkFileMd5(oss, ANDROID_TEST_BUCKET, objectKey, OSSTestConfig.FILE_DIR + testFile);
    }

    @Test
    public void testSequenceUploadWithException() throws Exception {
        final String objectKey = objectname;
        ResumableUploadRequest request = new ResumableUploadRequest(ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.FILE_DIR + testFile);

        request.setPartSize(256*1024);
        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[SequenceUpload Progress] - " + currentSize + " " + totalSize, false);
                if (currentSize > totalSize / 4 && currentSize < totalSize / 2) {
                    throw new RuntimeException("error currentSize small than 1/2");
                }
                if (currentSize > totalSize / 2) {
                    throw new RuntimeException("error currentSize bigger than 1/2");
                }

            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncSequenceUpload(request, callback);

        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("small"));
    }

    @Test
    public void testSequenceUploadMore1000AndResume() throws Exception {
        final String objectKey = UPLOAD_BIGFILE;
        ResumableUploadRequest request = new ResumableUploadRequest(ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.FILE_DIR + UPLOAD_BIGFILE, OSSTestConfig.FILE_DIR);
        final long partSize = 256 * 1024;

        request.setDeleteUploadOnCancelling(false);
        request.setPartSize(partSize);
        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {

            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                assertEquals(objectKey, request.getObjectKey());
                OSSLog.logDebug("big file progress 001 - " + currentSize + " " + totalSize + " index : " + (currentSize / partSize), false);
                if (currentSize / partSize > 1002) {
                    needCancelled.set(true);
                }
            }
        });

        OSSTestConfig.TestResumableUploadCallback callback = new OSSTestConfig.TestResumableUploadCallback();

        OSSAsyncTask task = oss.asyncSequenceUpload(request, callback);

        while (!needCancelled.get()) {
            Thread.sleep(100);
        }
        task.cancel();
        task.waitUntilFinished();

        assertNull(callback.result);
        assertNotNull(callback.clientException);
        OSSLog.logError("clientException: " + callback.clientException.toString());

        request = new ResumableUploadRequest(ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.FILE_DIR + UPLOAD_BIGFILE, OSSTestConfig.FILE_DIR);
        request.setPartSize(partSize);

//        ObjectMetadata metadata = new ObjectMetadata();
//        String sha1Value = BinaryUtil.fileToSHA1(OSSTestConfig.FILE_DIR + testFile);
//        metadata.setSHA1(sha1Value);
//        request.setMetadata(metadata);

        request.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {


            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("bigfile progress 002 - " + currentSize + " " + totalSize + " index : " + (currentSize / partSize), false);
                assertTrue(currentSize / partSize > 1002);
            }
        });

        callback = new OSSTestConfig.TestResumableUploadCallback();

        task = oss.asyncSequenceUpload(request, callback);

        task.waitUntilFinished();

        assertNotNull(callback.result);
        assertNull(callback.clientException);

        OSSTestUtils.checkFileMd5(oss, ANDROID_TEST_BUCKET, objectKey, OSSTestConfig.FILE_DIR + UPLOAD_BIGFILE);
    }

}
