package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.HttpdnsMini;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

/**
 * Created by wangzheng on 2018/8/2.
 */

public class OSSHttp2Test extends AndroidTestCase {
    protected String mBucketName;
    protected OSS oss;


    void initTestData() throws Exception {
        OSSTestConfig.initLocalFile();
        OSSTestConfig.initDemoFile("guihua.zip");
        OSSTestConfig.initDemoFile("demo.pdf");
    }

    protected void initOSSClient() {
        HttpdnsMini.getInstance().isHttp2Test = true;
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(60 * 1000); // 连接超时，默认60秒
        conf.setSocketTimeout(60 * 1000); // socket超时，默认60秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider,conf);
        OSSLog.enableLog();
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mBucketName = "zq-beijing";
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            initOSSClient();
            initTestData();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testMultiOperaion() throws Exception{
        String fileName = "file1m.jpg";

        for (int i = 0; i < 30; i++){
            OSSLog.logDebug("===== " + i + " =====");
            PutObjectRequest put = new PutObjectRequest(mBucketName, fileName,
                    OSSTestConfig.FILE_DIR + "file1m");
            OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

            OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
            task.waitUntilFinished();
            assertEquals(200, putCallback.result.getStatusCode());

            HeadObjectRequest head = new HeadObjectRequest(mBucketName, fileName);
            HeadObjectResult headResult = oss.headObject(head);

            assertEquals("image/jpeg", headResult.getMetadata().getContentType());


            GetObjectRequest request = new GetObjectRequest(mBucketName, fileName);
            OSSTestConfig.TestGetCallback getCallback = new OSSTestConfig.TestGetCallback();

            OSSAsyncTask getTask = oss.asyncGetObject(request, getCallback);
            getTask.waitUntilFinished();

            assertEquals(200, getCallback.result.getStatusCode());
        }
    }

}
