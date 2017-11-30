package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.internal.CheckCRC64DownLoadInputStream;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

/**
 * Created by jingdan on 2017/11/29.
 */

public class CRC64Test extends AndroidTestCase {

    private OSS oss;

    @Override
    protected void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.authCredentialProvider);
        }
    }

    public void testCRC64GetObject() throws Exception {

        GetObjectRequest request = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "demo.pdf");
        request.setCRC64(OSSRequest.CRC64Config.YES);
        request.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("progress: " + currentSize+"  total_size: " + totalSize,false);
            }
        });

        GetObjectResult result = oss.getObject(request);

        IOUtils.readStreamAsBytesArray(result.getObjectContent());
        long clientCrc64 = ((CheckCRC64DownLoadInputStream) result.getObjectContent()).getChecksum().getValue();
        result.setClientCRC(clientCrc64);

        checkCRC(result);

        result.getObjectContent().close();

    }

    public void testCRC64PutObject() throws Exception {

        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "demo.pdf",
                OSSTestConfig.FILE_DIR + "demo.pdf");
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();
        put.setCRC64(OSSRequest.CRC64Config.YES);
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        checkCRC(putCallback.result);
    }

    public void testCRC64AppendObject() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "append_file1m");
        oss.deleteObject(delete);

        AppendObjectRequest append = new AppendObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "append_file1m",
                OSSTestConfig.FILE_DIR + "file1m");
        append.setInitCRC64(0L);
        append.setCRC64(OSSRequest.CRC64Config.YES);
        append.setProgressCallback(new OSSProgressCallback<AppendObjectRequest>() {
            @Override
            public void onProgress(AppendObjectRequest request, long currentSize, long totalSize) {
                OSSLog.logDebug("onProgress - " + currentSize + " " + totalSize, false);
            }
        });

        OSSTestConfig.TestAppendCallback appendCallback = new OSSTestConfig.TestAppendCallback();

        // first append
        OSSAsyncTask task = oss.asyncAppendObject(append, appendCallback);
        task.waitUntilFinished();

        assertEquals(200, appendCallback.result.getStatusCode());

        checkCRC(appendCallback.result);
        append.setInitCRC64(appendCallback.result.getClientCRC());
        append.setPosition(1024 * 1000);

        appendCallback = new OSSTestConfig.TestAppendCallback();

        // second append
        task = oss.asyncAppendObject(append, appendCallback);
        task.waitUntilFinished();

        assertEquals(200, appendCallback.result.getStatusCode());

        checkCRC(appendCallback.result);
    }

    private <Result extends OSSResult> void checkCRC(final Result result) {
        assertNotNull(result.getClientCRC());
        assertNotNull(result.getServerCRC());
        assertTrue(result.getClientCRC() != 0L);
        assertTrue(result.getServerCRC() != 0L);
        assertEquals(result.getClientCRC(), result.getServerCRC());
    }
}
