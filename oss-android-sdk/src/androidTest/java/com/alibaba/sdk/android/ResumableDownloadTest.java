package com.alibaba.sdk.android;

import android.support.test.InstrumentationRegistry;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.ResumableDownloadResult;
import com.alibaba.sdk.android.oss.model.ResumableDownloadRequest;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.Range;

import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ResumableDownloadTest extends BaseTestCase {

    private static final String RESUMABLE_DOWNLOAD_OBJECT_KEY = "multipartDownloadFile";
    private static final String DOWNLOAD_PATH = InstrumentationRegistry.getContext().getFilesDir().getAbsolutePath() + "/file10m";
    private static final String CHECKPOINT_PATH = InstrumentationRegistry.getContext().getFilesDir().getAbsolutePath();
    private String file10mPath = OSSTestConfig.FILE_DIR + "file10m";

    @Override
    void initTestData() throws Exception {
        OSSTestConfig.initLocalFile();
        PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, file10mPath);
        oss.putObject(putObjectRequest);
    }

    @Test
    public void testResumableDownload() throws ClientException, ServiceException, IOException, NoSuchAlgorithmException {

        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setProgressListener(new OSSProgressCallback() {
            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                OSSLog.logDebug("mul_download_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }

    @Test
    public void testResumableDownloadWithInvalidBucketName() {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest("mBucketName", RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        ClientException exception = callback.clientException;
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("The bucket name is invalid"));
    }

    @Test
    public void testResumableDownloadWithInvalidObjectKey() {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, "//invalidObjectKey", DOWNLOAD_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        ClientException exception = callback.clientException;
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("The object key is invalid"));
    }

    @Test
    public void testResumableDownloadWithNullObjectKey() {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, null, DOWNLOAD_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        ClientException exception = callback.clientException;
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("The object key is invalid"));
    }

    @Test
    public void testGetNotExistObject() throws Exception {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, "objectKey", DOWNLOAD_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        ServiceException exception = callback.serviceException;
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    public void testConcurrentResumableDownload() throws Exception {
        for (int i = 0; i < 5; i++) {
            PutObjectRequest request = new PutObjectRequest(mBucketName, "ResumableDownload" + i, file10mPath);
            oss.putObject(request);
        }
        final CountDownLatch latch = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, "ResumableDownload" + index, DOWNLOAD_PATH + index);
                        oss.asyncResumableDownload(request, null).getResult();
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

    @Test
    public void testResumableDownloadWithCancel() throws InterruptedException {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);

        task.cancel();
        task.waitUntilFinished();
        ClientException exception = callback.clientException;
        assertTrue(exception.getMessage().contains("Resumable download cancel"));
    }

    @Test
    public void testResumableDownloadWithCheckpoint() throws InterruptedException {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        final int[] progress = {0};
        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        request.setCRC64(OSSRequest.CRC64Config.YES);
        final AtomicBoolean needCancelled = new AtomicBoolean(false);
        request.setProgressListener(new OSSProgressCallback() {
            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                progress[0] = (int) ((float)currentSize / totalSize);
                if (currentSize > totalSize / 2) {
                    needCancelled.set(true);
                }
            }
        });
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);

        while (!needCancelled.get()) {
            Thread.sleep(100);
        }
        task.cancel();
        task.waitUntilFinished();
        assertNull(callback.result);
        assertNotNull(callback.clientException);

        Thread.sleep(1000l);

        request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        request.setCRC64(OSSRequest.CRC64Config.YES);
        request.setProgressListener(new OSSProgressCallback() {
            @Override
            public void onProgress(Object request, long currentSize, long totalSize) {
                int p = (int) ((float)currentSize / totalSize);
                assertTrue(p >= progress[0]);
            }
        });
        task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();
    }

    @Test
    public void testResumableDownloadFile() throws InterruptedException, ClientException, ServiceException, IOException, NoSuchAlgorithmException {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);

        Thread.sleep(100);
        task.cancel();

        PutObjectRequest putRequest = new PutObjectRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, OSSTestConfig.FILE_DIR + "file1m");
        oss.putObject(putRequest);

        callback = new OSSTestConfig.TestResumableDownloadCallback();
        task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }

    @Test
    public void testResumableDownloadSmallFile() throws ClientException, ServiceException, NoSuchAlgorithmException, IOException {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }

    @Test
    public void testResumableDownloadBigFile() throws ClientException, ServiceException, NoSuchAlgorithmException, IOException {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }

    @Test
    public void testSyncResumableDownload() throws ClientException, ServiceException, NoSuchAlgorithmException, IOException {
        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        ResumableDownloadResult result = oss.syncResumableDownload(request);

        assertEquals(result.getStatusCode(), 200);
        OSSTestUtils.checkFileMd5(oss, mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
    }

    @Test
    public void testResumableErrorRange() throws ClientException, ServiceException, NoSuchAlgorithmException, IOException {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setRange(new Range(-100, 0));
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        assertTrue(callback.clientException.getMessage().contains("Range is invalid"));

        request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setRange(new Range(100, 0));
        request.setEnableCheckPoint(true);
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        assertTrue(callback.clientException.getMessage().contains("Range is invalid"));
    }

    @Test
    public void testResumableDownloadFileOfRange() throws ClientException, ServiceException, NoSuchAlgorithmException, IOException {
        OSSTestConfig.TestResumableDownloadCallback callback = new OSSTestConfig.TestResumableDownloadCallback();

        ResumableDownloadRequest request = new ResumableDownloadRequest(mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, DOWNLOAD_PATH);
        request.setEnableCheckPoint(true);
        request.setRange(new Range(100, -1));
        request.setCheckPointFilePath(CHECKPOINT_PATH);
        OSSAsyncTask<ResumableDownloadResult> task = oss.asyncResumableDownload(request, callback);
        task.waitUntilFinished();

        OSSTestUtils.checkFileMd5(oss, mBucketName, RESUMABLE_DOWNLOAD_OBJECT_KEY, new Range(100, -1), DOWNLOAD_PATH);
    }
}
