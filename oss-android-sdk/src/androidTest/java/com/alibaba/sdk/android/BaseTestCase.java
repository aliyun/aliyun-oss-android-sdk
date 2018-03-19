package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;

import static com.alibaba.sdk.android.oss.model.CannedAccessControlList.PublicReadWrite;

/**
 * Created by jingdan on 2018/3/1.
 */

public abstract class BaseTestCase extends AndroidTestCase {
    protected String mBucketName;
    protected String mPublicBucketName;
    protected OSS oss;

    abstract void initTestData() throws Exception;

    protected void initOSSClient() {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mBucketName = OSSTestUtils.produceBucketName(getName());
        mPublicBucketName = OSSTestUtils.produceBucketName("public-" + getName());
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            initOSSClient();
            try {
                CreateBucketRequest request = new CreateBucketRequest(mBucketName);
                oss.createBucket(request);
                CreateBucketRequest request2 = new CreateBucketRequest(mPublicBucketName);
                request2.setBucketACL(PublicReadWrite);
                oss.createBucket(request2);
                initTestData();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, mBucketName);
            OSSTestUtils.cleanBucket(oss, mPublicBucketName);
        } catch (Exception e) {
        }
    }


}
