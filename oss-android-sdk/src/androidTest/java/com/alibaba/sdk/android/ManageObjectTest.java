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


    @Override
    public void setUp() throws Exception {
        OSSTestConfig.instance(getContext());
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
            OSSLog.logDebug("OSSTEST", "initLocalFile");
            OSSTestConfig.initLocalFile();
//            uploadObjectForTest();
        }
    }


    public void testUploadObjectForTest() throws Exception {
        PutObjectRequest put = new PutObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, filePath);

        PutObjectResult putResult = oss.putObject(put);

        assertEquals(200, putResult.getStatusCode());
        assertNotNull(putResult.getETag());
    }

    public void testDeleteObject() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(200, headResult.getStatusCode());

        DeleteObjectRequest delete = new DeleteObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        DeleteObjectResult deleteResult = oss.deleteObject(delete);
        assertEquals(204, deleteResult.getStatusCode());

        head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        try {
            oss.headObject(head);
            assertTrue(false);
        } catch (ClientException e) {
            assertNull(e);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
        testUploadObjectForTest();
    }

    public void testDeleteAsync() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        HeadObjectResult headResult = oss.headObject(head);
        assertEquals(200, headResult.getStatusCode());

        OSSTestConfig.TestDeleteCallback deleteCallback = new OSSTestConfig.TestDeleteCallback();

        DeleteObjectRequest delete = new DeleteObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);

        OSSAsyncTask task = oss.asyncDeleteObject(delete, deleteCallback);
        task.waitUntilFinished();

        assertEquals(204, deleteCallback.result.getStatusCode());

        head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        try {
            oss.headObject(head);
            assertTrue(false);
        } catch (ClientException e) {
            assertNull(e);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
        testUploadObjectForTest();
    }

    public void testAsyncCopyObject() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");
        oss.deleteObject(delete);

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");

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


        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    public void testCopyObject() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");
        oss.deleteObject(delete);

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");

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


        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    public void testCopyObjectWithMatchEtag() throws Exception {

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");

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

    public void testCopyObjectWithNoMatchEtag() throws Exception {

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");

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


        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");
        HeadObjectResult result = oss.headObject(head);

        assertEquals("application/binary-copy", result.getMetadata().getContentType());
    }

    public void testAsyncHeadObject() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");

        OSSTestConfig.TestHeadObjectCallback callback = new OSSTestConfig.TestHeadObjectCallback();

        OSSAsyncTask<HeadObjectResult> task = oss.asyncHeadObject(head, callback);

        task.waitUntilFinished();

        assertNull(callback.serviceException);
        assertNotNull(callback.result.getMetadata().getContentType());
        assertEquals(1024 * 1000, callback.result.getMetadata().getContentLength());
    }

    public void testHeadObject() throws Exception {
        HeadObjectRequest head = new HeadObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m");

        HeadObjectResult headResult = oss.headObject(head);

        assertNotNull(headResult.getMetadata().getContentType());
        assertEquals(1024 * 1000, headResult.getMetadata().getContentLength());
    }

    public void testDoesObjectExist() throws Exception {

        assertTrue(oss.doesObjectExist(OSSTestConfig.ANDROID_TEST_BUCKET, "file1m"));

        assertFalse(oss.doesObjectExist(OSSTestConfig.ANDROID_TEST_BUCKET, "doesnotexist"));
    }

}
