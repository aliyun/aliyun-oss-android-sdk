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
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.Owner;
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
    private final static String FOR_LISTOBJECT_BUCKET = "oss-android-manage-object-list-test";

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

                CreateBucketRequest listBucket = new CreateBucketRequest(FOR_LISTOBJECT_BUCKET);
                oss.createBucket(listBucket);
                initListObjectData();
            } catch (Exception e) {
            }

        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, BUCKET_NAME);
            OSSTestUtils.cleanBucket(oss, FOR_LISTOBJECT_BUCKET);
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
        asyncListObjectsTest();
        syncListObjectsTest();
        listObjectsWithDelimiterMarkerTest();
        asyncListObjectsWithInvalidBucketTest();
        listObjectSettingPrefixTest();
        listObjectSettingPrefixAndDelimitateTest();
    }

    private void putTestFile() throws Exception{
        PutObjectRequest put = new PutObjectRequest(BUCKET_NAME,
                objectKey, filePath);
        oss.putObject(put);
    }

    private void initListObjectData() throws Exception{
        PutObjectRequest createFolder1 = new PutObjectRequest(FOR_LISTOBJECT_BUCKET,
                "folder1/", new byte[0]);
        oss.putObject(createFolder1);
        PutObjectRequest createFolder2 = new PutObjectRequest(FOR_LISTOBJECT_BUCKET,
                "folder2/", new byte[0]);
        oss.putObject(createFolder2);

        PutObjectRequest file1 = new PutObjectRequest(FOR_LISTOBJECT_BUCKET,
                "file1", filePath);
        oss.putObject(file1);
        PutObjectRequest file2 = new PutObjectRequest(FOR_LISTOBJECT_BUCKET,
                "file2", filePath);
        oss.putObject(file2);
        PutObjectRequest file3 = new PutObjectRequest(FOR_LISTOBJECT_BUCKET,
                "file3", filePath);
        oss.putObject(file3);

        PutObjectRequest file1m = new PutObjectRequest(FOR_LISTOBJECT_BUCKET,
                objectKey, filePath);
        oss.putObject(file1m);
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

    public void asyncListObjectsTest() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(FOR_LISTOBJECT_BUCKET);

        OSSTestConfig.TestListObjectsCallback callback = new OSSTestConfig.TestListObjectsCallback();

        OSSAsyncTask task = oss.asyncListObjects(listObjects, callback);

        task.waitUntilFinished();

        assertEquals(6, callback.result.getObjectSummaries().size());
        for (int i = 0; i < callback.result.getObjectSummaries().size(); i++) {
            OSSLog.logDebug("object: " + callback.result.getObjectSummaries().get(i).getKey() + " "
                    + callback.result.getObjectSummaries().get(i).getETag() + " "
                    + callback.result.getObjectSummaries().get(i).getLastModified());
        }
    }

    public void syncListObjectsTest() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(FOR_LISTOBJECT_BUCKET);
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

        assertEquals(6, result.getObjectSummaries().size());
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

    public void listObjectsWithDelimiterMarkerTest() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(FOR_LISTOBJECT_BUCKET);
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

    public void asyncListObjectsWithInvalidBucketTest() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest();
        listObjects.setBucketName("#bucketName");

        OSSTestConfig.TestListObjectsCallback callback = new OSSTestConfig.TestListObjectsCallback();

        OSSAsyncTask task = oss.asyncListObjects(listObjects, callback);

        task.waitUntilFinished();
        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("The bucket name is invalid"));
    }

    public void listObjectSettingPrefixTest() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(FOR_LISTOBJECT_BUCKET);

        listObjects.setPrefix("file");

        ListObjectsResult result = oss.listObjects(listObjects);

        assertEquals(4, result.getObjectSummaries().size());

        for (int i = 0; i < result.getObjectSummaries().size(); i++) {
            OSSLog.logDebug("object: " + result.getObjectSummaries().get(i).getKey() + " "
                    + result.getObjectSummaries().get(i).getETag() + " "
                    + result.getObjectSummaries().get(i).getLastModified());
        }

        assertEquals(0, result.getCommonPrefixes().size());
    }

    public void listObjectSettingPrefixAndDelimitateTest() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(FOR_LISTOBJECT_BUCKET);

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
