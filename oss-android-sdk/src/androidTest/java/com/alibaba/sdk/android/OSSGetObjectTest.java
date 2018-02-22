package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.Range;

import java.io.FileOutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSGetObjectTest extends AndroidTestCase {

    OSS oss;
    private final static String BUCKET_NAME = "oss-android-get-object-test";
    public static final String JPG_OBJECT_KEY = "JPG_OBJECT_KEY";
    private String file1mPath = OSSTestConfig.FILE_DIR + "file1m";
    private String file1kPath = OSSTestConfig.FILE_DIR + "file1k";
    private String file10kPath = OSSTestConfig.FILE_DIR + "file10k";
    private String file100kPath = OSSTestConfig.FILE_DIR + "file100k";
    private String imgPath = OSSTestConfig.FILE_DIR + "shilan.jpg";

    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            ClientConfiguration conf = new ClientConfiguration();
            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
            conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT,
                    OSSTestConfig.authCredentialProvider,
                    conf);
            try {
                CreateBucketRequest request = new CreateBucketRequest(BUCKET_NAME);
                oss.createBucket(request);
                OSSTestConfig.initLocalFile();
                OSSTestConfig.initDemoFile("shilan.jpg");
                PutObjectRequest file1k = new PutObjectRequest(BUCKET_NAME,
                        "file1k", file1kPath);
                oss.putObject(file1k);
                PutObjectRequest file10k = new PutObjectRequest(BUCKET_NAME,
                        "file10k", file10kPath);
                oss.putObject(file10k);
                PutObjectRequest file100k = new PutObjectRequest(BUCKET_NAME,
                        "file100k", file100kPath);
                oss.putObject(file100k);
                PutObjectRequest file1m = new PutObjectRequest(BUCKET_NAME,
                        "file1m", file1mPath);
                oss.putObject(file1m);
                PutObjectRequest putImg = new PutObjectRequest(BUCKET_NAME,
                        JPG_OBJECT_KEY, imgPath);
                oss.putObject(putImg);
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, BUCKET_NAME);
        } catch (Exception e) {
        }
    }

    public void testGetObject() throws Exception{
        asyncGetObjectTest();
        asyncGetImageWithXOssProcessTest();
        syncGetObjectTest();
        getObjectRangeTest();
        getObjectWithInvalidBucketNameTest();
        getObjectWithInvalidObjectKeyTest();
        getObjectWithNullObjectKeyTest();
        syncGetNotExistObjectTest();
        asyncGetNotExistObjectTest();
        downloadObjectToFileTest();
        concurrentGetObjectTest();
        putAndGetObjectWithSpecialFileKeyTest();
        notUseCRC64GetObjectTest();
    }

    public void asyncGetObjectTest() throws Exception {
        GetObjectRequest request = new GetObjectRequest(BUCKET_NAME, "file1m");
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();

        request.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncGetObject(request, getCallback);
        task.waitUntilFinished();

        GetObjectRequest rq = getCallback.request;
        GetObjectResult result = getCallback.result;

        assertEquals("file1m", rq.getObjectKey());
        assertEquals(BUCKET_NAME, rq.getBucketName());
        byte[] content = IOUtils.readStreamAsBytesArray(result.getObjectContent());
        assertEquals(1024 * 1000, content.length);
        result.getObjectContent().close();
    }

    public void asyncGetImageWithXOssProcessTest() throws Exception {
        String jpgObject = JPG_OBJECT_KEY;
        GetObjectRequest request = new GetObjectRequest(BUCKET_NAME, jpgObject);
        request.setxOssProcess("image/resize,m_lfit,w_100,h_100");
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();

        request.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncGetObject(request, getCallback);
        task.waitUntilFinished();

        assertEquals(jpgObject, getCallback.request.getObjectKey());
        assertEquals(BUCKET_NAME, getCallback.request.getBucketName());
        assertNull(getCallback.clientException);
        assertNull(getCallback.serviceException);
        assertNotNull(getCallback.result);
    }

    public void syncGetObjectTest() throws Exception {
        GetObjectRequest request = new GetObjectRequest(BUCKET_NAME, "file1m");

        request.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });

        GetObjectResult result = oss.getObject(request);

        byte[] content = IOUtils.readStreamAsBytesArray(result.getObjectContent());
        assertEquals(1024 * 1000, content.length);

        result.getObjectContent().close();

        assertNotNull(result.getMetadata().getContentType());
    }

    public void getObjectRangeTest() throws Exception {
        GetObjectRequest request = new GetObjectRequest(BUCKET_NAME, "file1m");
        Range range = new Range(1, 100);
        request.setRange(range);

        request.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });

        OSSLog.logDebug("Range: begin " + range.getBegin() + " end " + range.getEnd() + " isValid " + range.checkIsValid(), false);

        GetObjectResult result = oss.getObject(request);

        byte[] content = IOUtils.readStreamAsBytesArray(result.getObjectContent());
        assertEquals(100, content.length);
        result.getObjectContent().close();

        request.setRange(new Range(Range.INFINITE, 100));

        result = oss.getObject(request);

        content = IOUtils.readStreamAsBytesArray(result.getObjectContent());
        assertEquals(100, content.length);
        result.getObjectContent().close();

        request.setRange(new Range(100, Range.INFINITE));

        result = oss.getObject(request);

        content = IOUtils.readStreamAsBytesArray(result.getObjectContent());
        assertEquals(1024 * 1000 - 100, content.length);

        result.getObjectContent().close();
    }

    public void getObjectWithInvalidBucketNameTest() throws Exception {
        GetObjectRequest get = new GetObjectRequest("#bucketName", "file1m");
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();
        assertNotNull(getCallback.clientException);
        assertTrue(getCallback.clientException.getMessage().contains("The bucket name is invalid"));
    }

    public void getObjectWithInvalidObjectKeyTest() throws Exception {
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "//file1m");
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();
        assertNotNull(getCallback.clientException);
        assertTrue(getCallback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void getObjectWithNullObjectKeyTest() throws Exception {
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, null);
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();
        assertNotNull(getCallback.clientException);
        assertTrue(getCallback.clientException.getMessage().contains("The object key is invalid"));
    }

    public void syncGetNotExistObjectTest() throws Exception {
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "nofile");
        GetObjectResult result;
        try {
            result = oss.getObject(get);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
            assertEquals("NoSuchKey", e.getErrorCode());
        }
    }

    public void asyncGetNotExistObjectTest() throws Exception {
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "nofile");
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        OSSAsyncTask task = oss.asyncGetObject(get, getCallback);
        task.waitUntilFinished();
        assertNotNull(getCallback.serviceException);
        assertEquals(404, getCallback.serviceException.getStatusCode());
        assertEquals("NoSuchKey", getCallback.serviceException.getErrorCode());
    }

    public void downloadObjectToFileTest() throws Exception {
        // upload file
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, "file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        String srcFileBase64Md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(OSSTestConfig.FILE_DIR + "file1m"));
        OSSAsyncTask putTask = oss.asyncPutObject(put, putCallback);
        putTask.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        // download object to file
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, "file1m");
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        OSSAsyncTask getTask = oss.asyncGetObject(get, getCallback);
        getTask.waitUntilFinished();
        assertEquals(200, getCallback.result.getStatusCode());
        long length = getCallback.result.getContentLength();
        byte[] buffer = new byte[(int) length];
        int readCount = 0;
        while (readCount < length) {
            readCount += getCallback.result.getObjectContent().read(buffer, readCount, (int) length - readCount);
        }
        try {
            FileOutputStream fout = new FileOutputStream(OSSTestConfig.FILE_DIR + "download_file1m");
            fout.write(buffer);
            fout.close();
        } catch (Exception e) {
            OSSLog.logInfo(e.toString());
        }
        String downloadFileBase64Md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(OSSTestConfig.FILE_DIR + "download_file1m"));
        assertEquals(srcFileBase64Md5, downloadFileBase64Md5);

    }

    public void concurrentGetObjectTest() throws Exception {
        final String fileNameArr[] = {"file1k", "file10k", "file100k", "file1m"};
        final int fileSizeArr[] = {1024, 10240, 102400, 1024000};
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(4);
        for (int i = 0; i < 4; i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, fileNameArr[index]);
                        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
                        latch1.await();
                        OSSAsyncTask getTask = oss.asyncGetObject(get, getCallback);
                        getTask.waitUntilFinished();
                        assertEquals(200, getCallback.result.getStatusCode());
                        GetObjectResult result = getCallback.result;
                        byte[] content = IOUtils.readStreamAsBytesArray(result.getObjectContent());
                        assertEquals(fileSizeArr[index], content.length);
                        latch2.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        latch1.countDown();
        latch2.await();
        OSSLog.logDebug("testConcurrentGetObject success!");
    }

    public void putAndGetObjectWithSpecialFileKeyTest() throws Exception {
        final String specialFileKey = "+&~?、测试文件";
        // put object
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME, specialFileKey,
                OSSTestConfig.FILE_DIR + "file1m");
        String srcFileBase64Md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(OSSTestConfig.FILE_DIR + "file1m"));
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        OSSAsyncTask putTask = oss.asyncPutObject(put, putCallback);
        putTask.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());
        // get object
        GetObjectRequest get = new GetObjectRequest(BUCKET_NAME, specialFileKey);
        OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();
        OSSAsyncTask getTask = oss.asyncGetObject(get, getCallback);
        getTask.waitUntilFinished();
        assertEquals(200, getCallback.result.getStatusCode());
        long length = getCallback.result.getContentLength();
        byte[] buffer = new byte[(int) length];
        int readCount = 0;
        while (readCount < length) {
            readCount += getCallback.result.getObjectContent().read(buffer, readCount, (int) length - readCount);
        }
        try {
            FileOutputStream fout = new FileOutputStream(OSSTestConfig.FILE_DIR + "download_specialkey_file");
            fout.write(buffer);
            fout.close();
        } catch (Exception e) {
            OSSLog.logInfo(e.toString());
        }
        String downloadFileBase64Md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(OSSTestConfig.FILE_DIR + "download_specialkey_file"));
        assertEquals(srcFileBase64Md5, downloadFileBase64Md5);
    }

    public void notUseCRC64GetObjectTest() throws Exception {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setCheckCRC64(false);
        OSS oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT,
                OSSTestConfig.authCredentialProvider,
                conf);

        GetObjectRequest request = new GetObjectRequest(BUCKET_NAME, "file1m");

        request.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });

        GetObjectResult result = oss.getObject(request);

        byte[] content = IOUtils.readStreamAsBytesArray(result.getObjectContent());
        assertEquals(1024 * 1000, content.length);

        result.getObjectContent().close();

        assertNotNull(result.getMetadata().getContentType());
    }
}
