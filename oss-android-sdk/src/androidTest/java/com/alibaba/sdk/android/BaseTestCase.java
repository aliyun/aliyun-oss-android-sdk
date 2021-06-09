package com.alibaba.sdk.android;

import android.Manifest;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.HttpdnsMini;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;


import static com.alibaba.sdk.android.oss.model.CannedAccessControlList.PublicReadWrite;

/**
 * Created by jingdan on 2018/3/1.
 */

@RunWith(AndroidJUnit4.class)
public abstract class BaseTestCase {
    protected String mBucketName;
    protected String mPublicBucketName;
    protected OSS oss;

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE);

    abstract void initTestData() throws Exception;

    protected void initOSSClient() {
        ClientConfiguration conf = new ClientConfiguration();
        //ClientConfiguration 链接和socket 已经改为60s了
//        conf.setConnectionTimeout(60 * 1000); // 连接超时，默认15秒
//        conf.setSocketTimeout(60 * 1000); // socket超时，默认15秒
//        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
//        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        conf.setHttpDnsEnable(false);
        OSSLog.enableLog();

        oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider,conf);
    }


    @Before
    public void setUp() throws Exception {
        mBucketName = OSSTestUtils.produceBucketName(getName());
        mPublicBucketName = OSSTestUtils.produceBucketName("public-" + getName());
        OSSTestConfig.instance(InstrumentationRegistry.getTargetContext());
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

    public String getName() {
        return "photo-line";
    }

    @After
    public void tearDown() throws Exception {
        try {
            OSSTestUtils.cleanBucket(oss, mBucketName);
            OSSTestUtils.cleanBucket(oss, mPublicBucketName);
        } catch (Exception e) {
        }
    }


}
