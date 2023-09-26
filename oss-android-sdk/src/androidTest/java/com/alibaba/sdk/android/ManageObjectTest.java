package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetObjectMetaRequest;
import com.alibaba.sdk.android.oss.model.GetObjectMetaResult;
import com.alibaba.sdk.android.oss.model.GetObjectTaggingRequest;
import com.alibaba.sdk.android.oss.model.GetObjectTaggingResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.Owner;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by zhouzhuo on 11/25/15.
 */
public class ManageObjectTest extends BaseTestCase {
    private String objectKey = "file1m";
    private String filePath = OSSTestConfig.FILE_DIR + "file1m";
    private String TEST_ETAG = "7E868A8A0AD0493DD9545129EFD51C45-4";
    private String mListBucket;

    @Override
    void initTestData() throws Exception {
        OSSTestConfig.initLocalFile();
        putTestFile();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        try {
            mListBucket = OSSTestUtils.produceBucketName("list-" + getName());
            CreateBucketRequest listBucket = new CreateBucketRequest(mListBucket);
            oss.createBucket(listBucket);
            initListObjectData();
        } catch (Exception e) {
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            OSSTestUtils.cleanBucket(oss, mListBucket);
        } catch (Exception e) {
        }
    }

    private void putTestFile() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName,
                objectKey, filePath);
        oss.putObject(put);
    }

    private void initListObjectData() throws Exception {
        PutObjectRequest createFolder1 = new PutObjectRequest(mListBucket,
                "folder1/", new byte[0]);
        oss.putObject(createFolder1);
        PutObjectRequest createFolder2 = new PutObjectRequest(mListBucket,
                "folder2/", new byte[0]);
        oss.putObject(createFolder2);

        PutObjectRequest file1 = new PutObjectRequest(mListBucket,
                "file1", filePath);
        oss.putObject(file1);
        PutObjectRequest file2 = new PutObjectRequest(mListBucket,
                "file2", filePath);
        oss.putObject(file2);
        PutObjectRequest file3 = new PutObjectRequest(mListBucket,
                "file3", filePath);
        oss.putObject(file3);

        PutObjectRequest file1m = new PutObjectRequest(mListBucket,
                objectKey, filePath);
        oss.putObject(file1m);
    }

    @Test
    public void testDeleteObject() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(mBucketName, objectKey);
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(200, headResult.getStatusCode());

        DeleteObjectRequest delete = new DeleteObjectRequest(mBucketName, objectKey);
        DeleteObjectResult deleteResult = oss.deleteObject(delete);
        assertEquals(204, deleteResult.getStatusCode());

        head = new HeadObjectRequest(mBucketName, objectKey);
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

    @Test
    public void testDeleteAsync() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(mBucketName, objectKey);
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(200, headResult.getStatusCode());

        OSSTestConfig.TestDeleteCallback deleteCallback = new OSSTestConfig.TestDeleteCallback();

        DeleteObjectRequest delete = new DeleteObjectRequest(mBucketName, objectKey);

        OSSAsyncTask task = oss.asyncDeleteObject(delete, deleteCallback);
        task.waitUntilFinished();

        assertEquals(204, deleteCallback.result.getStatusCode());

        head = new HeadObjectRequest(mBucketName, objectKey);
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

    @Test
    public void testAsyncCopyObject() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(mBucketName, "testCopy");
        oss.deleteObject(delete);

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(mBucketName, objectKey,
                mBucketName, "testCopy");

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


        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    @Test
    public void testCopyObject() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(mBucketName, "testCopy");
        oss.deleteObject(delete);

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(mBucketName, objectKey,
                mBucketName, "testCopy");

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


        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    @Test
    public void testCopyObjectWithMatchEtag() throws Exception {

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(mBucketName, objectKey,
                mBucketName, "testCopy");

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

    @Test
    public void testCopyObjectWithNoMatchEtag() throws Exception {

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(mBucketName, objectKey,
                mBucketName, "testCopy");

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


        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    @Test
    public void testAsyncHeadObject() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(mBucketName, objectKey);

        OSSTestConfig.TestHeadObjectCallback callback = new OSSTestConfig.TestHeadObjectCallback();

        OSSAsyncTask<HeadObjectResult> task = oss.asyncHeadObject(head, callback);

        task.waitUntilFinished();

        assertNull(callback.serviceException);
        assertNotNull(callback.result.getMetadata().getContentType());
        assertEquals(1024 * 1000, callback.result.getMetadata().getContentLength());
    }

    @Test
    public void testGetObjectMeta() throws Exception {
        GetObjectMetaRequest getObjectMeta = new GetObjectMetaRequest(mBucketName, "file1m");
        GetObjectMetaResult getObjectMetaResult = oss.getObjectMeta(getObjectMeta);

        assertNotNull(getObjectMetaResult.getMetadata().getETag());
        assertNotNull(getObjectMetaResult.getMetadata().getLastModified());
        assertEquals(1024 * 1000, getObjectMetaResult.getMetadata().getContentLength());

        PutObjectRequest file1m = new PutObjectRequest(mBucketName,
                "file1m.txt", filePath);
        oss.putObject(file1m);

        getObjectMeta = new GetObjectMetaRequest(mBucketName, "file1m.txt");
        getObjectMetaResult = oss.getObjectMeta(getObjectMeta);

        assertEquals(1024 * 1000, getObjectMetaResult.getMetadata().getContentLength());
    }


    @Test
    public void testHeadObject() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "file1m");

        HeadObjectResult headResult = oss.headObject(head);

        assertNotNull(headResult.getMetadata().getContentType());
        assertEquals(1024 * 1000, headResult.getMetadata().getContentLength());
    }

    @Test
    public void testDoesObjectExist() throws Exception {

        assertTrue(oss.doesObjectExist(mBucketName, "file1m"));

        assertFalse(oss.doesObjectExist(mBucketName, "doesnotexist"));
    }

    @Test
    public void testAsyncListObjects() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(mListBucket);

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

    @Test
    public void testSyncListObjects() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(mListBucket);
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

    @Test
    public void testListObjectsWithDelimiterMarker() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(mListBucket);
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

    @Test
    public void testAsyncListObjectsWithInvalidBucket() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest();
        listObjects.setBucketName("#bucketName");

        OSSTestConfig.TestListObjectsCallback callback = new OSSTestConfig.TestListObjectsCallback();

        OSSAsyncTask task = oss.asyncListObjects(listObjects, callback);

        task.waitUntilFinished();
        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("The bucket name is invalid"));
    }

    @Test
    public void testListObjectSettingPrefix() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(mListBucket);

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

    @Test
    public void testListObjectSettingPrefixAndDelimitate() throws Exception {
        ListObjectsRequest listObjects = new ListObjectsRequest(mListBucket);

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

    @Test
    public void testCopyObjectWithTagging() throws Exception {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(mBucketName, objectKey,
                mBucketName, "testCopy");
        ObjectMetadata objectMetadata = new ObjectMetadata();
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("Key", "Value");
        tags.put("Key1", "Value1");
        String tagHeader = OSSUtils.paramToQueryString(tags, "UTF-8");
        objectMetadata.setHeader("x-oss-tagging", tagHeader);
        objectMetadata.setHeader("x-oss-tagging-directive", "REPLACE");
        copyObjectRequest.setNewObjectMetadata(objectMetadata);

        OSSTestConfig.TestCopyObjectCallback callback = new OSSTestConfig.TestCopyObjectCallback();
        OSSAsyncTask task = oss.asyncCopyObject(copyObjectRequest, callback);

        task.waitUntilFinished();

        assertNull(callback.serviceException);
        assertNotNull(callback.result.getETag());
        assertNotNull(callback.result.getLastModified());

        GetObjectTaggingRequest getTagging = new GetObjectTaggingRequest(mBucketName, "testCopy");
        GetObjectTaggingResult getTaggingResult = oss.getObjectTagging(getTagging);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            assertEquals(entry.getValue(), getTaggingResult.getTags().get(entry.getKey()));
        }
    }

    @Test
    public void testHeadObjectFail() {
        OSSTestConfig.TestHeadObjectCallback callback = new OSSTestConfig.TestHeadObjectCallback();

        HeadObjectRequest head = new HeadObjectRequest(mBucketName, "null");
        oss.asyncHeadObject(head, callback).waitUntilFinished();

        assertNotNull(callback.serviceException.getEc());
        assertNotNull(callback.serviceException.getErr());
    }

    @Test
    public void testGetObjectFail() {
        OSSTestConfig.TestGetObjectMetaCallback callback = new OSSTestConfig.TestGetObjectMetaCallback();
        GetObjectMetaRequest getObjectMeta = new GetObjectMetaRequest(mBucketName, "null");
        oss.asyncGetObjectMeta(getObjectMeta, callback).waitUntilFinished();

        assertNotNull(callback.serviceException.getEc());
    }

}
