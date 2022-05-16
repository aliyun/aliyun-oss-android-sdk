package com.alibaba.sdk.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.OkHttpClient;

import static com.alibaba.sdk.android.oss.model.CannedAccessControlList.PublicReadWrite;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class CustomOkhttpTest {

    private final static String UPLOAD_DEFAULT_FILE = "guihua.zip";
    private String file1mPath = OSSTestConfig.FILE_DIR + "file1m";

    protected static String mBucketName;
    protected static String mPublicBucketName;
    protected static OSS oss;

    private static void initOss() {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setHttpDnsEnable(false);
        conf.setOkHttpClient(new OkHttpClient.Builder().build());
        OSSLog.enableLog();
        oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        mBucketName = OSSTestUtils.produceBucketName(getName());
    }

    @BeforeClass
    public static void init() throws Exception {
        OSSTestConfig.instance(InstrumentationRegistry.getTargetContext());
        if (oss == null) {
            OSSLog.enableLog();
            initOss();
        }
        try {
            CreateBucketRequest request = new CreateBucketRequest(mBucketName);
            oss.createBucket(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OSSTestConfig.copyFilesFassets(InstrumentationRegistry.getContext(), UPLOAD_DEFAULT_FILE, true);
    }

    @AfterClass
    public static void dealloc() throws Exception {
        try {
            OSSTestUtils.cleanBucket(oss, mBucketName);
            OSSTestUtils.cleanBucket(oss, mPublicBucketName);
        } catch (Exception e) {
        }
        sleep(1000);
    }

    @Test
    public void testPutObjectFromFile() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, "file1m.jpg",
                OSSTestConfig.FILE_DIR + "file1m");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        ObjectMetadata metadata = new ObjectMetadata();
        Map<String, String> userMetadata = new HashMap<String, String>();
        userMetadata.put("userVar1", "value");
        metadata.addUserMetadata("X-Oss-meta-Key2", "Value2");
        // Content-Disposition
        metadata.setContentDisposition("attachment;filename="
                + OSSTestConfig.FILE_DIR + "file1m");
        metadata.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
        metadata.setCacheControl("no-cache");
        metadata.setContentEncoding("gzip");
        metadata.setUserMetadata(userMetadata);
        put.setMetadata(metadata);

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "file1m.jpg");
        HeadObjectResult headResult = oss.headObject(head);

        assertEquals("image/jpeg", headResult.getMetadata().getContentType());
    }

    @Test
    public void testAsyncGetObject() throws Exception {
        PutObjectRequest file1m = new PutObjectRequest(mBucketName,
                "file1m", file1mPath);
        oss.putObject(file1m);

        GetObjectRequest request = new GetObjectRequest(mBucketName, "file1m");
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
        assertEquals(mBucketName, rq.getBucketName());
        byte[] content = IOUtils.readStreamAsBytesArray(result.getObjectContent());
        assertEquals(1024 * 1000, content.length);
        result.getObjectContent().close();
    }

    @Test
    public void testResumableUpload() throws Exception {
        ResumableUploadRequest rq = new ResumableUploadRequest(mBucketName, UPLOAD_DEFAULT_FILE,
                OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE, OSSTestConfig.FILE_DIR);
        rq.setProgressCallback(new OSSProgressCallback<ResumableUploadRequest>() {
            @Override
            public void onProgress(ResumableUploadRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("[testResumableUpload] - " + currentSize + " " + totalSize, false);
            }
        });

        ResumableUploadResult result = oss.resumableUpload(rq);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());

        OSSTestUtils.checkFileMd5(oss, mBucketName, UPLOAD_DEFAULT_FILE, OSSTestConfig.FILE_DIR + UPLOAD_DEFAULT_FILE);

    }

    public static String getName() {
        String own = UUID.randomUUID().toString();
        return own;
    }
}
