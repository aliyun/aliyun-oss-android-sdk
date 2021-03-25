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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;


import java.util.UUID;

import static com.alibaba.sdk.android.oss.model.CannedAccessControlList.PublicReadWrite;
import static java.lang.Thread.sleep;

/**
 * Created by jingdan on 2018/3/1.
 */

@RunWith(AndroidJUnit4.class)
public abstract class BaseTestCase {
    protected static String mBucketName;
    protected static String mPublicBucketName;
    protected static OSS oss;

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    abstract void initTestData() throws Exception;
    void initOSSClient() {

    }

    private static void initOss() {
        ClientConfiguration conf = new ClientConfiguration();
        //ClientConfiguration 链接和socket 已经改为60s了
//        conf.setConnectionTimeout(60 * 1000); // 连接超时，默认15秒
//        conf.setSocketTimeout(60 * 1000); // socket超时，默认15秒
//        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
//        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        conf.setHttpDnsEnable(false);
        OSSLog.enableLog();
        oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, conf);
        mBucketName = OSSTestUtils.produceBucketName(getName());
        mPublicBucketName = OSSTestUtils.produceBucketName("public-" + getName());
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
        try {
            CreateBucketRequest request2 = new CreateBucketRequest(mPublicBucketName);
            request2.setBucketACL(PublicReadWrite);
            oss.createBucket(request2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUp() throws Exception {
        initOSSClient();
        initTestData();
    }

    public static String getName() {
        String own = UUID.randomUUID().toString();
        return own;
    }

    @After
    public void tearDown() throws Exception {
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

}
