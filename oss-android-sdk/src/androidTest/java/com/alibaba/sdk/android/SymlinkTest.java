package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetSymlinkRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.PutSymlinkRequest;
import com.alibaba.sdk.android.oss.model.PutSymlinkResult;

public class SymlinkTest extends BaseTestCase {

    private String testFile = "file10k";
    private String testObjectName_CN = "符号文件测试";
    private String testObjectName_EN = "test-symlink-object";

    @Override
    void initTestData() throws Exception {
        OSSTestConfig.initLocalFile();
    }

    public void testSymlinkWithObjectNameCN() throws Exception {
        PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, testObjectName_CN,
                OSSTestConfig.FILE_DIR + testFile);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        OSSAsyncTask task = oss.asyncPutObject(putObjectRequest, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        OSSTestConfig.TestPutSymlinkCallback putSymlinkCallback = new OSSTestConfig.TestPutSymlinkCallback();
        PutSymlinkRequest putSymlink = new PutSymlinkRequest();
        putSymlink.setBucketName(mBucketName);
        putSymlink.setObjectKey("test-symlink-object-symlink");
        putSymlink.setTargetObjectName(testObjectName_CN);

        OSSAsyncTask putSymlinkTask = oss.asyncPutSymlink(putSymlink, putSymlinkCallback);
        putSymlinkTask.waitUntilFinished();
        assertEquals(200, putSymlinkCallback.result.getStatusCode());

        OSSTestConfig.TestGetSymlinkCallback getSymlinkCallback = new OSSTestConfig.TestGetSymlinkCallback();
        GetSymlinkRequest getSymlink = new GetSymlinkRequest();
        getSymlink.setBucketName(mBucketName);
        getSymlink.setObjectKey("test-symlink-object-symlink");

        OSSAsyncTask getSymlinkTask = oss.asyncGetSymlink(getSymlink, getSymlinkCallback);
        getSymlinkTask.waitUntilFinished();
        assertEquals(200, getSymlinkCallback.result.getStatusCode());
    }

    public void testSymlinkWithObjectNameEN() throws Exception {
        PutObjectRequest put = new PutObjectRequest(mBucketName, testObjectName_EN,
                OSSTestConfig.FILE_DIR + testFile);
        OSSTestConfig.TestPutCallback putCallback = new OSSTestConfig.TestPutCallback();

        OSSAsyncTask task = oss.asyncPutObject(put, putCallback);
        task.waitUntilFinished();
        assertEquals(200, putCallback.result.getStatusCode());

        OSSTestConfig.TestPutSymlinkCallback putSymlinkCallback = new OSSTestConfig.TestPutSymlinkCallback();
        PutSymlinkRequest putSymlink = new PutSymlinkRequest();
        putSymlink.setBucketName(mBucketName);
        putSymlink.setObjectKey("test-symlink-object-symlink");
        putSymlink.setTargetObjectName(testObjectName_EN);

        OSSAsyncTask putSymlinkTask = oss.asyncPutSymlink(putSymlink, putSymlinkCallback);
        putSymlinkTask.waitUntilFinished();
        assertEquals(200, putSymlinkCallback.result.getStatusCode());

        OSSTestConfig.TestGetSymlinkCallback getSymlinkCallback = new OSSTestConfig.TestGetSymlinkCallback();
        GetSymlinkRequest getSymlink = new GetSymlinkRequest();
        getSymlink.setBucketName(mBucketName);
        getSymlink.setObjectKey("test-symlink-object-symlink");

        OSSAsyncTask getSymlinkTask = oss.asyncGetSymlink(getSymlink, getSymlinkCallback);
        getSymlinkTask.waitUntilFinished();
        assertEquals(200, getSymlinkCallback.result.getStatusCode());
    }

    public void testPutSymlinkDuplicate() throws Exception {
        PutObjectRequest putObjectRequest = new PutObjectRequest(mBucketName, testObjectName_EN,
                OSSTestConfig.FILE_DIR + testFile);

        PutObjectResult result = oss.putObject(putObjectRequest);
        assertEquals(200, result.getStatusCode());

        PutSymlinkRequest putSymlinkRequest = new PutSymlinkRequest();
        putSymlinkRequest.setBucketName(mBucketName);
        putSymlinkRequest.setObjectKey("test-symlink-object-symlink");
        putSymlinkRequest.setTargetObjectName(testObjectName_EN);

        PutSymlinkResult putSymlinkResult = oss.putSymlink(putSymlinkRequest);
        assertEquals(200, putSymlinkResult.getStatusCode());

        PutSymlinkRequest putSymlinkRequest1 = new PutSymlinkRequest();
        putSymlinkRequest1.setBucketName(mBucketName);
        putSymlinkRequest1.setObjectKey("test-symlink-object-symlink");
        putSymlinkRequest1.setTargetObjectName(testObjectName_EN);

        PutSymlinkResult putSymlinkResult1 = oss.putSymlink(putSymlinkRequest1);
        assertEquals(200, putSymlinkResult1.getStatusCode());
    }
}
