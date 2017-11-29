package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.internal.CheckCRC64DownLoadInputStream;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.OSSResult;

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

        ClientConfiguration conf = new ClientConfiguration();
        conf.setCheckCRC64(true);
        OSS oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT,
                OSSTestConfig.authCredentialProvider,
                conf);
        GetObjectRequest request = new GetObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");

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

    private <Result extends OSSResult> void checkCRC(final Result result) {
        assertNotNull(result.getClientCRC());
        assertNotNull(result.getServerCRC());
        assertTrue(result.getClientCRC() != 0L);
        assertTrue(result.getServerCRC() != 0L);
        assertEquals(result.getClientCRC(), result.getServerCRC());
    }
}
