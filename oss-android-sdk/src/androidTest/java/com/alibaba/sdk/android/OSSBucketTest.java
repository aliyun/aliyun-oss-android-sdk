package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CannedAccessControlList;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.GetBucketInfoRequest;
import com.alibaba.sdk.android.oss.model.GetBucketInfoResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.ListBucketsRequest;
import com.alibaba.sdk.android.oss.model.ListBucketsResult;
import com.alibaba.sdk.android.oss.model.OSSBucketSummary;
import com.alibaba.sdk.android.oss.model.Owner;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

import java.util.List;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSBucketTest extends AndroidTestCase {

    public static final String CREATE_TEMP_BUCKET = "oss-android-create-bucket-test";
    OSS oss;

    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        Thread.sleep(500);
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
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
            OSSClient ossClient = new OSSClient(getContext(), OSSTestConfig.credentialProvider, null);

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
}
