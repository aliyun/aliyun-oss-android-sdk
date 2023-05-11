package com.alibaba.sdk.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.internal.ResponseParsers;
import com.alibaba.sdk.android.oss.model.BucketLifecycleRule;
import com.alibaba.sdk.android.oss.model.CannedAccessControlList;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketLifecycleRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketLifecycleResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketLoggingRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketLoggingResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.GetBucketInfoRequest;
import com.alibaba.sdk.android.oss.model.GetBucketInfoResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.GetBucketLifecycleRequest;
import com.alibaba.sdk.android.oss.model.GetBucketLifecycleResult;
import com.alibaba.sdk.android.oss.model.GetBucketLoggingRequest;
import com.alibaba.sdk.android.oss.model.GetBucketLoggingResult;
import com.alibaba.sdk.android.oss.model.GetBucketRefererRequest;
import com.alibaba.sdk.android.oss.model.GetBucketRefererResult;
import com.alibaba.sdk.android.oss.model.ListBucketsRequest;
import com.alibaba.sdk.android.oss.model.ListBucketsResult;
import com.alibaba.sdk.android.oss.model.OSSBucketSummary;
import com.alibaba.sdk.android.oss.model.Owner;
import com.alibaba.sdk.android.oss.model.PutBucketLifecycleRequest;
import com.alibaba.sdk.android.oss.model.PutBucketLifecycleResult;
import com.alibaba.sdk.android.oss.model.PutBucketLoggingRequest;
import com.alibaba.sdk.android.oss.model.PutBucketLoggingResult;
import com.alibaba.sdk.android.oss.model.PutBucketRefererRequest;
import com.alibaba.sdk.android.oss.model.PutBucketRefererResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by zhouzhuo on 11/24/15.
 */
@RunWith(AndroidJUnit4.class)
public class OSSBucketTest {

    public static final String CREATE_TEMP_BUCKET = "oss-android-create-bucket-test";
    OSS oss;

    @Before
    public void setUp() throws Exception {
        OSSTestConfig.instance(InstrumentationRegistry.getTargetContext());
        Thread.sleep(500);
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    public void testSyncCreateBucket() throws Exception {
        CreateBucketRequest request = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        CreateBucketResult bucket = oss.createBucket(request);

        assertNotNull(bucket);
        assertEquals(200, bucket.getStatusCode());

        DeleteBucketRequest delete = new DeleteBucketRequest(CREATE_TEMP_BUCKET);
        DeleteBucketResult result = oss.deleteBucket(delete);
        assertEquals(204, result.getStatusCode());
    }

    public void testAsyncCreateBucket() throws Exception {
        CreateBucketRequest request = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        OSSTestConfig.TestCreateBucketCallback callback = new OSSTestConfig.TestCreateBucketCallback();
        OSSAsyncTask task = oss.asyncCreateBucket(request, callback);

        task.waitUntilFinished();
        assertNull(callback.serviceException);
        assertEquals(200, callback.result.getStatusCode());

        DeleteBucketRequest delete = new DeleteBucketRequest(CREATE_TEMP_BUCKET);
        DeleteBucketResult result = oss.deleteBucket(delete);
        assertEquals(204, result.getStatusCode());
    }

    public void testCreateBucketWithAcl() throws Exception {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        createBucketRequest.setBucketACL(CannedAccessControlList.PublicRead);
        OSSTestConfig.TestCreateBucketCallback createCallback = new OSSTestConfig.TestCreateBucketCallback();
        OSSAsyncTask createTask = oss.asyncCreateBucket(createBucketRequest, createCallback);
        createTask.waitUntilFinished();
        assertNull(createCallback.serviceException);
        assertEquals(200, createCallback.result.getStatusCode());
        GetBucketACLRequest getBucketACLRequest = new GetBucketACLRequest(CREATE_TEMP_BUCKET);
        OSSTestConfig.TestGetBucketACLCallback getBucketACLCallback = new OSSTestConfig.TestGetBucketACLCallback();
        OSSAsyncTask getAclTask = oss.asyncGetBucketACL(getBucketACLRequest, getBucketACLCallback);
        getAclTask.waitUntilFinished();

        Owner owner = getBucketACLCallback.result.getOwner();
        OSSLog.logDebug("BucketAcl", getBucketACLCallback.result.getBucketACL());
        OSSLog.logDebug("Owner", getBucketACLCallback.result.getBucketOwner());
        OSSLog.logDebug("ID", getBucketACLCallback.result.getBucketOwnerID());
        OSSLog.logDebug("result", getBucketACLCallback.result.getOwner().toString());
        OSSLog.logDebug("isSameOwner", String.valueOf(owner.equals(owner)));
        OSSLog.logDebug("hashCode", String.valueOf(getBucketACLCallback.result.getOwner().hashCode()));

        assertEquals(false, getBucketACLCallback.result.getOwner().equals("xxx-test"));

        assertEquals(200, getBucketACLCallback.result.getStatusCode());
        assertEquals(CannedAccessControlList.PublicRead.toString(), getBucketACLCallback.result.getBucketACL());

        DeleteBucketRequest delete = new DeleteBucketRequest(CREATE_TEMP_BUCKET);
        DeleteBucketResult result = oss.deleteBucket(delete);
        assertEquals(204, result.getStatusCode());
    }

    public void testEmptyOwnerEqualsFunction() {
        Owner empty = new Owner();
        Owner empty2 = new Owner();
        boolean equals = empty.equals(empty2);
        assertTrue(equals);
    }

    public void testDeleteBucket() throws Exception {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        OSSTestConfig.TestCreateBucketCallback createCallback = new OSSTestConfig.TestCreateBucketCallback();
        OSSAsyncTask createTask = oss.asyncCreateBucket(createBucketRequest, createCallback);
        createTask.waitUntilFinished();
        assertNull(createCallback.serviceException);
        assertEquals(200, createCallback.result.getStatusCode());
        Thread.sleep(5000);
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(CREATE_TEMP_BUCKET);
        OSSTestConfig.TestDeleteBucketCallback callback = new OSSTestConfig.TestDeleteBucketCallback();
        OSSAsyncTask task = oss.asyncDeleteBucket(deleteBucketRequest, callback);
        task.waitUntilFinished();
        assertNull(callback.serviceException);
        assertEquals(204, callback.result.getStatusCode());
    }

    public void testDeleteNotExistBucket() throws Exception {
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest("not-exist-bucket");
        OSSTestConfig.TestDeleteBucketCallback callback = new OSSTestConfig.TestDeleteBucketCallback();
        OSSAsyncTask task = oss.asyncDeleteBucket(deleteBucketRequest, callback);
        task.waitUntilFinished();
        assertNotNull(callback.serviceException);
        assertEquals(404, callback.serviceException.getStatusCode());
    }

    public void testDeleteNotEmptyBucket() throws Exception {
        CreateBucketRequest request = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        oss.createBucket(request);
        PutObjectRequest put = new PutObjectRequest(CREATE_TEMP_BUCKET,
                "file1m", OSSTestConfig.FILE_DIR + "file1m");
        oss.putObject(put);

        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(CREATE_TEMP_BUCKET);
        OSSTestConfig.TestDeleteBucketCallback callback = new OSSTestConfig.TestDeleteBucketCallback();
        OSSAsyncTask task = oss.asyncDeleteBucket(deleteBucketRequest, callback);
        task.waitUntilFinished();
        assertNotNull(callback.serviceException);
        assertEquals(409, callback.serviceException.getStatusCode());
        OSSTestUtils.cleanBucket(oss, CREATE_TEMP_BUCKET);
    }

    public void testSyncGetBucketInfo() throws Exception {
        CreateBucketRequest create = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        create.setBucketACL(CannedAccessControlList.Private);
        oss.createBucket(create);

        GetBucketInfoRequest request = new GetBucketInfoRequest(CREATE_TEMP_BUCKET);
        GetBucketInfoResult result = oss.getBucketInfo(request);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertEquals(CannedAccessControlList.Private.toString(), result.getBucket().getAcl());

        OSSTestUtils.cleanBucket(oss, CREATE_TEMP_BUCKET);
    }

    public void testAsyncGetBucketInfo() throws Exception {
        CreateBucketRequest create = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        create.setBucketACL(CannedAccessControlList.Private);
        oss.createBucket(create);

        GetBucketInfoRequest request = new GetBucketInfoRequest(CREATE_TEMP_BUCKET);
        OSSTestConfig.TestGetBucketInfoCallback callback = new OSSTestConfig.TestGetBucketInfoCallback();
        OSSAsyncTask task = oss.asyncGetBucketInfo(request, callback);
        task.waitUntilFinished();
        assertNull(callback.serviceException);
        assertEquals(200, callback.result.getStatusCode());
        assertEquals(CannedAccessControlList.Private.toString(), callback.result.getBucket().getAcl());

        OSSTestUtils.cleanBucket(oss, CREATE_TEMP_BUCKET);
    }

    public void testSyncGetBucketACL() throws Exception {
        CreateBucketRequest create = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        create.setBucketACL(CannedAccessControlList.PublicReadWrite);
        oss.createBucket(create);

        GetBucketACLRequest request = new GetBucketACLRequest(CREATE_TEMP_BUCKET);
        GetBucketACLResult result = oss.getBucketACL(request);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertEquals(CannedAccessControlList.PublicReadWrite.toString(), result.getBucketACL());

        OSSTestUtils.cleanBucket(oss, CREATE_TEMP_BUCKET);
    }

    public void testAsyncGetBucketACL() throws Exception {
        CreateBucketRequest create = new CreateBucketRequest(CREATE_TEMP_BUCKET);
        create.setBucketACL(CannedAccessControlList.PublicReadWrite);
        oss.createBucket(create);

        GetBucketACLRequest request = new GetBucketACLRequest(CREATE_TEMP_BUCKET);
        OSSTestConfig.TestGetBucketACLCallback callback = new OSSTestConfig.TestGetBucketACLCallback();
        OSSAsyncTask task = oss.asyncGetBucketACL(request, callback);
        task.waitUntilFinished();
        assertNull(callback.serviceException);
        assertEquals(200, callback.result.getStatusCode());
        assertEquals(CannedAccessControlList.PublicReadWrite.toString(), callback.result.getBucketACL());

        OSSTestUtils.cleanBucket(oss, CREATE_TEMP_BUCKET);
    }

    public void testListBucket() {
        try {
            OSSClient ossClient = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.credentialProvider, null);

            ListBucketsRequest request = new ListBucketsRequest();
            ListBucketsResult result = ossClient.listBuckets(request);

            assertEquals(200, result.getStatusCode());
            List<OSSBucketSummary> buckets = result.getBuckets();
            assertTrue(buckets.size() > 0);
            for (int i = 0; i < buckets.size(); i++) {
                OSSLog.logDebug("name: " + buckets.get(i).name + " "
                        + "location: " + buckets.get(i).location);
            }
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }
    }

    @Test
    public void testListBucketsWithEndpoint() {
        try {
            OSSClient ossClient = new OSSClient(InstrumentationRegistry.getTargetContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider, null);

            List<String> bucketNames = new ArrayList();
            for (int i = 0; i < 10; i++) {
                String bucketName = "test-list-bucket" + new Date().getTime() + i;
                bucketNames.add(bucketName);

                CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
                CreateBucketResult result = oss.createBucket(createBucketRequest);
            }

            ListBucketsRequest request = new ListBucketsRequest();
            ListBucketsResult result = oss.listBuckets(request);

            assertEquals(200, result.getStatusCode());
            List<OSSBucketSummary> buckets = result.getBuckets();
            assertTrue(buckets.size() > bucketNames.size());
            List<String> ListBucketNames = new ArrayList();
            for (int i = 0; i < buckets.size(); i++) {
                String listBucketName = buckets.get(i).name;
                ListBucketNames.add(listBucketName);
            }
            for (int i = 0; i < bucketNames.size(); i++) {
                String bucketName = bucketNames.get(i);
                assertTrue(ListBucketNames.contains(bucketName));
                DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(bucketName);
                oss.deleteBucket(deleteBucketRequest);
            }
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }
    }

    @Test
    public void testBucketReferer() throws Exception {
        final String testBucketName = "android-sdk-test-bucket-referer";
        CreateBucketRequest create = new CreateBucketRequest(testBucketName);
        create.setBucketACL(CannedAccessControlList.Private);
        oss.createBucket(create);

        // put bucket referer
        try {
            PutBucketRefererRequest request = new PutBucketRefererRequest();
            request.setBucketName(testBucketName);
            ArrayList<String> referers = new ArrayList<String>();
            referers.add("http://www.taobao.com");
            referers.add("http://www.aliyun.com");
            request.setReferers(referers);

            PutBucketRefererResult result = oss.putBucketReferer(request);
            assertEquals(200, result.getStatusCode());
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }

        // get bucket referer
        try {
            GetBucketRefererRequest request = new GetBucketRefererRequest();
            request.setBucketName(testBucketName);
            GetBucketRefererResult result = oss.getBucketReferer(request);

            assertEquals(200, result.getStatusCode());
            List<String> referers = result.getReferers();
            assertTrue(referers.size() > 0);
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }

        OSSTestUtils.cleanBucket(oss, testBucketName);
    }

    @Test
    public void testBucketLogging() throws Exception {
        final String sourceBucketName = "android-sdk-test-bucket-logging-source";
        final String targetBucketName = "android-sdk-test-bucket-logging-target";
        CreateBucketRequest create1 = new CreateBucketRequest(sourceBucketName);
        create1.setBucketACL(CannedAccessControlList.Private);
        oss.createBucket(create1);

        CreateBucketRequest create2 = new CreateBucketRequest(targetBucketName);
        create2.setBucketACL(CannedAccessControlList.Private);
        oss.createBucket(create2);

        // put bucket logging
        try {
            PutBucketLoggingRequest request = new PutBucketLoggingRequest();
            request.setBucketName(sourceBucketName);
            request.setTargetBucketName(targetBucketName);
            request.setTargetPrefix("target-prefix");

            PutBucketLoggingResult result = oss.putBucketLogging(request);
            assertEquals(200, result.getStatusCode());
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }

        // get bucket logging
        try {
            GetBucketLoggingRequest request = new GetBucketLoggingRequest();
            request.setBucketName(sourceBucketName);
            GetBucketLoggingResult result = oss.getBucketLogging(request);

            assertEquals(200, result.getStatusCode());
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }

        // delete bucket logging
        try {
            DeleteBucketLoggingRequest request = new DeleteBucketLoggingRequest();
            request.setBucketName(sourceBucketName);
            DeleteBucketLoggingResult result = oss.deleteBucketLogging(request);

            assertEquals(204, result.getStatusCode());
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }

        OSSTestUtils.cleanBucket(oss, sourceBucketName);
        OSSTestUtils.cleanBucket(oss, targetBucketName);
    }

    @Test
    public void testBucketLifecycle() throws Exception {
        final String bucketName = "android-sdk-test-bucket-lifecycle";
        CreateBucketRequest create = new CreateBucketRequest(bucketName);
        create.setBucketACL(CannedAccessControlList.Private);
        oss.createBucket(create);

        // put bucket logging
        try {
            PutBucketLifecycleRequest request = new PutBucketLifecycleRequest();
            request.setBucketName(bucketName);

            BucketLifecycleRule rule1 = new BucketLifecycleRule();
            rule1.setIdentifier("1");
            rule1.setPrefix("A");
            rule1.setStatus(true);
            rule1.setDays("2");
            rule1.setArchiveDays("30");
            rule1.setMultipartDays("3");
            rule1.setIADays("15");

            BucketLifecycleRule rule2 = new BucketLifecycleRule();
            rule2.setIdentifier("2");
            rule2.setPrefix("B");
            rule2.setStatus(true);
            rule2.setDays("3");
            rule2.setArchiveDays("30");
            rule2.setMultipartDays("3");
            rule2.setIADays("15");

            ArrayList<BucketLifecycleRule> lifecycleRules = new ArrayList<BucketLifecycleRule>();
            lifecycleRules.add(rule1);
            lifecycleRules.add(rule2);

            request.setLifecycleRules(lifecycleRules);


            PutBucketLifecycleResult result = oss.putBucketLifecycle(request);
            assertEquals(200, result.getStatusCode());
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }

        // get bucket logging
        try {
            GetBucketLifecycleRequest request = new GetBucketLifecycleRequest();
            request.setBucketName(bucketName);
            GetBucketLifecycleResult result = oss.getBucketLifecycle(request);

            assertEquals(200, result.getStatusCode());
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }

        // delete bucket logging
        try {
            DeleteBucketLifecycleRequest request = new DeleteBucketLifecycleRequest();
            request.setBucketName(bucketName);
            DeleteBucketLifecycleResult result = oss.deleteBucketLifecycle(request);

            assertEquals(204, result.getStatusCode());
        } catch (Exception e) {
            e.getMessage();
            assertNull(e);
        }

        OSSTestUtils.cleanBucket(oss, bucketName);
    }
}
