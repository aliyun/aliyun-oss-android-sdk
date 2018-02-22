package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhouzhuo on 11/25/15.
 */
public class ManageObjectTest extends AndroidTestCase {
    private OSS oss;
    private String objectKey = "file1m";
    private String filePath = OSSTestConfig.FILE_DIR + "file1m";
    private String TEST_ETAG = "7E868A8A0AD0493DD9545129EFD51C45-4";
    private final static String BUCKET_NAME = "oss-android-manage-object-test";

    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
            try {
                CreateBucketRequest request = new CreateBucketRequest(BUCKET_NAME);
                oss.createBucket(request);
                OSSLog.logDebug("OSSTEST", "initLocalFile");
                OSSTestConfig.initLocalFile();
                putTestFile();
            } catch (Exception e) {
            }

        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, BUCKET_NAME);
        } catch (Exception e) {
        }
    }

    public void testManageObject() throws Exception{
        deleteObjectTest();
        deleteAsyncTest();
        asyncCopyObjectTest();
        copyObjectTest();
        copyObjectWithMatchEtagTest();
        copyObjectWithNoMatchEtagTest();
        asyncHeadObjectTest();
        headObjectTest();
        doesObjectExistTest();
    }

    private void putTestFile() throws Exception{
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME,
                objectKey, filePath);

        oss.putObject(put);
    }

    public void deleteObjectTest() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, objectKey);
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(200, headResult.getStatusCode());

        DeleteObjectRequest delete = new DeleteObjectRequest(BUCKET_NAME, objectKey);
        DeleteObjectResult deleteResult = oss.deleteObject(delete);
        assertEquals(204, deleteResult.getStatusCode());

        head = new HeadObjectRequest(BUCKET_NAME, objectKey);
        try {
            oss.headObject(head);
            assertTrue(false);
        } catch (ClientException e) {
            assertNull(e);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
        putTestFile();
    }

    public void deleteAsyncTest() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, objectKey);
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(200, headResult.getStatusCode());

        OSSTestConfig.TestDeleteCallback deleteCallback = new OSSTestConfig.TestDeleteCallback();

        DeleteObjectRequest delete = new DeleteObjectRequest(BUCKET_NAME, objectKey);

        OSSAsyncTask task = oss.asyncDeleteObject(delete, deleteCallback);
        task.waitUntilFinished();

        assertEquals(204, deleteCallback.result.getStatusCode());

        head = new HeadObjectRequest(BUCKET_NAME, objectKey);
        try {
            oss.headObject(head);
            assertTrue(false);
        } catch (ClientException e) {
            assertNull(e);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
        putTestFile();
    }

    public void asyncCopyObjectTest() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(BUCKET_NAME, "testCopy");
        oss.deleteObject(delete);

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(BUCKET_NAME, objectKey,
                BUCKET_NAME, "testCopy");

        copyObjectRequest.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate1 = dateFormat1.parse("2017-01-01");
        copyObjectRequest.setModifiedSinceConstraint(myDate1);

        copyObjectRequest.setUnmodifiedSinceConstraint(new Date());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("application/binary-copy");

        copyObjectRequest.setNewObjectMetadata(objectMetadata);

        OSSTestConfig.TestCopyObjectCallback callback = new OSSTestConfig.TestCopyObjectCallback();


        OSSAsyncTask task = oss.asyncCopyObject(copyObjectRequest, callback);

        task.waitUntilFinished();

        assertNull(callback.serviceException);
        assertNotNull(callback.result.getETag());
        assertNotNull(callback.result.getLastModified());


        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    public void copyObjectTest() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(BUCKET_NAME, "testCopy");
        oss.deleteObject(delete);

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(BUCKET_NAME, objectKey,
                BUCKET_NAME, "testCopy");

        copyObjectRequest.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);

        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate1 = dateFormat1.parse("2017-01-01");
        copyObjectRequest.setModifiedSinceConstraint(myDate1);

        copyObjectRequest.setUnmodifiedSinceConstraint(new Date());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("application/binary-copy");

        copyObjectRequest.setNewObjectMetadata(objectMetadata);

        CopyObjectResult copyResult = oss.copyObject(copyObjectRequest);
        assertNotNull(copyResult.getETag());
        assertNotNull(copyResult.getLastModified());


        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    public void copyObjectWithMatchEtagTest() throws Exception {

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(BUCKET_NAME, objectKey,
                BUCKET_NAME, "testCopy");

        copyObjectRequest.clearMatchingETagConstraints();

        List<String> etags = new ArrayList<String>();
        etags.add(TEST_ETAG);
        copyObjectRequest.setMatchingETagConstraints(etags);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("application/binary-copy");

        copyObjectRequest.setNewObjectMetadata(objectMetadata);

        try {
            oss.copyObject(copyObjectRequest);
        } catch (ServiceException e) { //412 指定的etag 与source object 不符
            assertTrue(true);
        }
    }

    public void copyObjectWithNoMatchEtagTest() throws Exception {

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(BUCKET_NAME, objectKey,
                BUCKET_NAME, "testCopy");

        copyObjectRequest.clearNonmatchingETagConstraints();

        List<String> etags = new ArrayList<String>();
        etags.add("1234567890");
        copyObjectRequest.setNonmatchingETagConstraints(etags);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("application/binary-copy");

        copyObjectRequest.setNewObjectMetadata(objectMetadata);

        CopyObjectResult copyResult = oss.copyObject(copyObjectRequest);
        assertNotNull(copyResult.getETag());
        assertNotNull(copyResult.getLastModified());


        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    public void asyncHeadObjectTest() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, objectKey);

        OSSTestConfig.TestHeadObjectCallback callback = new OSSTestConfig.TestHeadObjectCallback();

        OSSAsyncTask<HeadObjectResult> task = oss.asyncHeadObject(head, callback);

        task.waitUntilFinished();

        assertNull(callback.serviceException);
        assertNotNull(callback.result.getMetadata().getContentType());
        assertEquals(1024 * 1000, callback.result.getMetadata().getContentLength());
    }

    public void headObjectTest() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(BUCKET_NAME, "file1m");

        HeadObjectResult headResult = oss.headObject(head);

        assertNotNull(headResult.getMetadata().getContentType());
        assertEquals(1024 * 1000, headResult.getMetadata().getContentLength());
    }

    public void doesObjectExistTest() throws Exception {

        assertTrue(oss.doesObjectExist(BUCKET_NAME, "file1m"));

        assertFalse(oss.doesObjectExist(BUCKET_NAME, "doesnotexist"));
    }

    @Override
    public void testAndroidTestCaseSetupProperly() {
        //do nothing
    }
}
