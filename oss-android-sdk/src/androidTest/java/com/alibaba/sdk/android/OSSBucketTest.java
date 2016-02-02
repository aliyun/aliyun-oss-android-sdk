package com.alibaba.sdk.android;

import android.test.AndroidTestCase;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CannedAccessControlList;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSBucketTest extends AndroidTestCase {

    OSS oss;

    @Override
    public void setUp() throws Exception {
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
        }
    }

    public void testCreateBucket() throws Exception {
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
        assertEquals(200, getBucketACLCallback.result.getStatusCode());
        assertEquals(CannedAccessControlList.PublicRead.toString(), getBucketACLCallback.result.getBucketACL());

        DeleteBucketRequest delete = new DeleteBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        DeleteBucketResult result = oss.deleteBucket(delete);
        assertEquals(204, result.getStatusCode());
    }

    public void testCreateBucketWithLocationConstraint() throws Exception {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        createBucketRequest.setLocationConstraint("oss-cn-hangzhou");
        OSSTestConfig.TestCreateBucketCallback createCallback = new OSSTestConfig.TestCreateBucketCallback();
        OSSAsyncTask createTask = oss.asyncCreateBucket(createBucketRequest, createCallback);
        createTask.waitUntilFinished();
        assertNull(createCallback.serviceException);
        assertEquals(200, createCallback.result.getStatusCode());
        assertEquals("oss-cn-hangzhou", createCallback.request.getLocationConstraint());

        DeleteBucketRequest delete = new DeleteBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        DeleteBucketResult result = oss.deleteBucket(delete);
        assertEquals(204, result.getStatusCode());
    }

    public void testDeleteBucket() throws Exception {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(OSSTestConfig.CREATE_TEMP_BUCKET);
        createBucketRequest.setLocationConstraint("oss-cn-hangzhou");
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


    public void testGetBucketACL() throws Exception {
        GetBucketACLRequest request = new GetBucketACLRequest(OSSTestConfig.PUBLIC_READ_WRITE_BUCKET);
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

        assertEquals(20, callback.result.getObjectSummaries().size());
        for (int i = 0; i < callback.result.getObjectSummaries().size(); i++) {
            OSSLog.logD("object: " + callback.result.getObjectSummaries().get(i).getKey() + " "
                    + callback.result.getObjectSummaries().get(i).getETag() + " "
                    + callback.result.getObjectSummaries().get(i).getLastModified());
        }
    }

    public void testSyncListObjects() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(OSSTestConfig.FOR_LISTOBJECT_BUCKET);

        ListObjectsResult result = oss.listObjects(listObjects);

        assertEquals(20, result.getObjectSummaries().size());
        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            OSSLog.logD("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }
    }

    public void testAsyncListObjectsWithInvalidBucket() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest("#bucketName");

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

        assertEquals(10, result.getObjectSummaries().size());

        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            OSSLog.logD("object: " + result.getObjectSummaries().get(i).getKey() + " "
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
            OSSLog.logD("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }

        for (int i = 0; i < result.getCommonPrefixes().size(); i++) {
            OSSLog.logD("prefixe: " + result.getCommonPrefixes().get(i));
        }

        assertEquals(0, result.getObjectSummaries().size());
        assertEquals(10, result.getCommonPrefixes().size());
    }
}
