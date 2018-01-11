package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CannedAccessControlList;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.Owner;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSBucketTest extends AndroidTestCase {

    OSS oss;

    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
//            oss = new OSSClient(getContext(), "", OSSTestConfig.credentialProvider);
        }
    }

    public void testSyncCreateBucket() throws Exception {
        CreateBucketRequest request = new CreateBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        CreateBucketResult bucket = oss.createBucket(request);

        assertNotNull(bucket);
        assertEquals(200, bucket.getStatusCode());

        DeleteBucketRequest delete = new DeleteBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        DeleteBucketResult result = oss.deleteBucket(delete);
        assertEquals(204, result.getStatusCode());
    }

    public void testAsyncCreateBucket() throws Exception {
        CreateBucketRequest request = new CreateBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        OSSTestConfig.TestCreateBucketCallback callback = new OSSTestConfig.TestCreateBucketCallback();
        OSSAsyncTask task = oss.asyncCreateBucket(request, callback);

        task.waitUntilFinished();
        assertNull(callback.serviceException);
        assertEquals(200, callback.result.getStatusCode());

        DeleteBucketRequest delete = new DeleteBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        DeleteBucketResult result = oss.deleteBucket(delete);
        assertEquals(204, result.getStatusCode());
    }

    public void testCreateBucketWithAcl() throws Exception {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        createBucketRequest.setBucketACL(CannedAccessControlList.PublicRead);
        OSSTestConfig.TestCreateBucketCallback createCallback = new OSSTestConfig.TestCreateBucketCallback();
        OSSAsyncTask createTask = oss.asyncCreateBucket(createBucketRequest, createCallback);
        createTask.waitUntilFinished();
        assertNull(createCallback.serviceException);
        assertEquals(200, createCallback.result.getStatusCode());
        GetBucketACLRequest getBucketACLRequest = new GetBucketACLRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
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
        assertEquals(CannedAccessControlList.Private.toString(), getBucketACLCallback.result.getBucketACL());

        DeleteBucketRequest delete = new DeleteBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
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
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        createBucketRequest.setLocationConstraint(OSSTestConfig.ANDROID_TEST_LOCATION);
        OSSTestConfig.TestCreateBucketCallback createCallback = new OSSTestConfig.TestCreateBucketCallback();
        OSSAsyncTask createTask = oss.asyncCreateBucket(createBucketRequest, createCallback);
        createTask.waitUntilFinished();
        assertNull(createCallback.serviceException);
        assertEquals(200, createCallback.result.getStatusCode());
        Thread.sleep(5000);
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
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
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(OSSTestConfig.ANDROID_TEST_BUCKET);
        OSSTestConfig.TestDeleteBucketCallback callback = new OSSTestConfig.TestDeleteBucketCallback();
        OSSAsyncTask task = oss.asyncDeleteBucket(deleteBucketRequest, callback);
        task.waitUntilFinished();
        assertNotNull(callback.serviceException);
        assertEquals(409, callback.serviceException.getStatusCode());
    }

    public void testSyncGetBucketACL() throws Exception {
        GetBucketACLRequest request = new GetBucketACLRequest(OSSTestConfig.ANDROID_TEST_BUCKET);
        GetBucketACLResult result = oss.getBucketACL(request);
        assertNotNull(result);
        assertEquals(200, result.getStatusCode());
        assertEquals(CannedAccessControlList.PublicReadWrite.toString(), result.getBucketACL());
    }

    public void testAsyncGetBucketACL() throws Exception {
        GetBucketACLRequest request = new GetBucketACLRequest(OSSTestConfig.ANDROID_TEST_BUCKET);
        OSSTestConfig.TestGetBucketACLCallback callback = new OSSTestConfig.TestGetBucketACLCallback();
        OSSAsyncTask task = oss.asyncGetBucketACL(request, callback);
        task.waitUntilFinished();
        assertNull(callback.serviceException);
        assertEquals(200, callback.result.getStatusCode());
        assertEquals(CannedAccessControlList.PublicReadWrite.toString(), callback.result.getBucketACL());
    }

    public void testAsyncListObjects() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);

        OSSTestConfig.TestListObjectsCallback callback = new OSSTestConfig.TestListObjectsCallback();

        OSSAsyncTask task = oss.asyncListObjects(listObjects, callback);

        task.waitUntilFinished();

        assertEquals(8, callback.result.getObjectSummaries().size());
        for (int i = 0; i < callback.result.getObjectSummaries().size(); i++) {
            OSSLog.logDebug("object: " + callback.result.getObjectSummaries().get(i).getKey() + " "
                    + callback.result.getObjectSummaries().get(i).getETag() + " "
                    + callback.result.getObjectSummaries().get(i).getLastModified());
        }
    }

    public void testSyncListObjects() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);
        listObjects.setEncodingType("url");
        ListObjectsResult result = oss.listObjects(listObjects);

        OSSLog.logDebug("object: " + result.getNextMarker() + " "
                + result.getBucketName() + " "
                + result.getPrefix() + " "
                + result.getMarker() + " "
                + result.getMaxKeys() + " "
                + result.getDelimiter() + " "
                + result.getEncodingType() + " "
                + result.isTruncated());

        assertEquals(8, result.getObjectSummaries().size());
        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            Owner owner = result.getObjectSummaries().get(i).getOwner();
            OSSLog.logDebug("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getBucketName() + " "
                    + result.getObjectSummaries().get(i).getSize() + " "
                    + result.getObjectSummaries().get(i).getStorageClass() + " "
                    + result.getObjectSummaries().get(i).getType() + " "
                    + (owner != null ? owner.toString() : " ") + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }
    }

    public void testListObjectsWithDelimiterMarker() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);
        listObjects.setMarker("file1m");
        listObjects.setDelimiter("/");
        listObjects.setMaxKeys(2);
        ListObjectsResult result = oss.listObjects(listObjects);

        OSSLog.logDebug("OSS-Android-SDK", "object: " + result.getNextMarker() + " "
                + result.getBucketName() + " "
                + result.getPrefix() + " "
                + result.getMarker() + " "
                + result.getMaxKeys() + " "
                + result.getDelimiter() + " "
                + result.getEncodingType() + " "
                + result.isTruncated());

        assertEquals(2, result.getObjectSummaries().size());
    }

    public void testAsyncListObjectsWithInvalidBucket() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest();
        listObjects.setBucketName("#bucketName");

        OSSTestConfig.TestListObjectsCallback callback = new OSSTestConfig.TestListObjectsCallback();

        OSSAsyncTask task = oss.asyncListObjects(listObjects, callback);

        task.waitUntilFinished();
        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("The bucket name is invalid"));
    }

    public void testListObjectSettingPrefix() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);

        listObjects.setPrefix("file");

        ListObjectsResult result = oss.listObjects(listObjects);

        assertEquals(3, result.getObjectSummaries().size());

        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            OSSLog.logDebug("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }

        assertEquals(0, result.getCommonPrefixes().size());
    }

    public void testListObjectSettingPrefixAndDelimitate() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);

        listObjects.setPrefix("folder");
        listObjects.setDelimiter("/");

        ListObjectsResult result = oss.listObjects(listObjects);

        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            OSSLog.logDebug("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }

        for (int i = 0; i < result.getCommonPrefixes().size(); i++) {
            OSSLog.logDebug("prefixe: " + result.getCommonPrefixes().get(i));
        }

        assertEquals(0, result.getObjectSummaries().size());
        assertEquals(2, result.getCommonPrefixes().size());
    }
}
