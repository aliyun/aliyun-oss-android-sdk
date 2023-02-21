package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.DeleteObjectTaggingRequest;
import com.alibaba.sdk.android.oss.model.GetObjectTaggingRequest;
import com.alibaba.sdk.android.oss.model.GetObjectTaggingResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectTaggingRequest;
import com.alibaba.sdk.android.oss.model.PutObjectTaggingResult;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ObjectTaggingTest extends BaseTestCase {

    private static String objectKey = "tagging";
    @Override
    void initTestData() throws Exception {
        OSSTestConfig.initLocalFile();
        PutObjectRequest request = new PutObjectRequest(mBucketName, objectKey, OSSTestConfig.EXTERNAL_FILE_DIR + "file1m");
        oss.putObject(request);
    }

    @Test
    public void testPutObjectTagging() throws ClientException, ServiceException {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("Key", "Value");
        tags.put("Key1", "Value1");
        PutObjectTaggingRequest request = new PutObjectTaggingRequest(mBucketName, objectKey, tags);
        oss.putObjectTagging(request);

        GetObjectTaggingRequest getObjectTaggingRequest = new GetObjectTaggingRequest(mBucketName, objectKey);
        GetObjectTaggingResult getObjectTaggingResult = oss.getObjectTagging(getObjectTaggingRequest);
        assertEquals("Value", getObjectTaggingResult.getTags().get("Key"));
        assertEquals("Value1", getObjectTaggingResult.getTags().get("Key1"));

        DeleteObjectTaggingRequest deleteObjectTaggingRequest = new DeleteObjectTaggingRequest(mBucketName, objectKey);
        oss.deleteObjectTagging(deleteObjectTaggingRequest);

        getObjectTaggingRequest = new GetObjectTaggingRequest(mBucketName, objectKey);
        getObjectTaggingResult = oss.getObjectTagging(getObjectTaggingRequest);
        assertEquals(0, getObjectTaggingResult.getTags().keySet().size());
    }

    @Test
    public void testPutNullTagging() throws ClientException, ServiceException {
        OSSTestConfig.TestPutTaggingCallback callback = new OSSTestConfig.TestPutTaggingCallback();
        Map<String, String> tags = new HashMap<String, String>();
        PutObjectTaggingRequest request = new PutObjectTaggingRequest(mBucketName, objectKey, tags);
        OSSAsyncTask task = oss.asyncPutObjectTagging(request, callback);
        task.waitUntilFinished();

        assertEquals(callback.result.getStatusCode(), 200);

        GetObjectTaggingRequest getObjectTaggingRequest = new GetObjectTaggingRequest(mBucketName, objectKey);
        GetObjectTaggingResult getObjectTaggingResult = oss.getObjectTagging(getObjectTaggingRequest);
        assertEquals(0, getObjectTaggingResult.getTags().keySet().size());

        DeleteObjectTaggingRequest deleteObjectTaggingRequest = new DeleteObjectTaggingRequest(mBucketName, objectKey);
        oss.deleteObjectTagging(deleteObjectTaggingRequest);
    }

    @Test
    public void testPutObjectTaggingError() {
        DateUtil.setCurrentServerTime(-24 * 60 * 60 * 1000);

        OSSTestConfig.TestPutTaggingCallback callback = new OSSTestConfig.TestPutTaggingCallback();
        PutObjectTaggingRequest request = new PutObjectTaggingRequest(mBucketName, objectKey, null);
        oss.asyncPutObjectTagging(request, callback);

        assertNotNull(callback.clientException);
        assertTrue(callback.clientException.getMessage().contains("Tag cannot be null!"));
    }

    @Test
    public void testDeletetObjectTaggingError() {
        DeleteObjectTaggingRequest deleteObjectTaggingRequest = new DeleteObjectTaggingRequest(mBucketName, "sss");
        try {
            oss.deleteObjectTagging(deleteObjectTaggingRequest);
        } catch (ClientException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

    }
}
