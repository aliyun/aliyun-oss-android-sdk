package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.RestoreObjectRequest;
import com.alibaba.sdk.android.oss.model.StorageClass;

public class RestoreObjectTest extends BaseTestCase {

    private String bucketName = "android-restoreobjecttest";
    private String restoredObjectName = "android-test-restore-object-name";
    private String testLocalFileName = "file100k";

    @Override
    void initTestData() throws Exception {
        OSSTestConfig.initLocalFile();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
            createBucketRequest.setBucketStorageClass(StorageClass.parse("Archive"));
            oss.createBucket(createBucketRequest);
            initTestData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, bucketName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testRestoreObject() throws Exception {
        PutObjectRequest put = new PutObjectRequest(bucketName, restoredObjectName,
                OSSTestConfig.FILE_DIR + testLocalFileName);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        RestoreObjectRequest restore = new RestoreObjectRequest();
        restore.setBucketName(bucketName);
        restore.setObjectKey(restoredObjectName);

        OSSTestConfig.TestRestoreObjectCallback restoreCallback = new OSSTestConfig.TestRestoreObjectCallback();

        OSSAsyncTask restoreTask = oss.asyncRestoreObject(restore, restoreCallback);
        restoreTask.waitUntilFinished();
        assertEquals(202, restoreCallback.result.getStatusCode());

        RestoreObjectRequest restore1 = new RestoreObjectRequest();
        restore1.setBucketName(bucketName);
        restore1.setObjectKey(restoredObjectName);
        OSSTestConfig.TestRestoreObjectCallback restoreCallback1 = new OSSTestConfig.TestRestoreObjectCallback();
        OSSAsyncTask restoreTask1 = oss.asyncRestoreObject(restore1, restoreCallback1);
        restoreTask1.waitUntilFinished();
        assertEquals(409, restoreCallback1.serviceException.getStatusCode());
    }
}
