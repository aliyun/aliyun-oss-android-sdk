package com.alibaba.sdk.android;

import android.test.AndroidTestCase;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhouzhuo on 11/25/15.
 */
public class ManageObjectTest extends AndroidTestCase {
    private OSS oss;
    private String objectKey = "file1m";
    private String filePath = OSSTestConfig.FILE_DIR + "file1m";


    @Override
    public void setUp() throws Exception {
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credentialProvider);
            Log.d("OSSTEST","initLocalFile");
            initLocalFile();
//            uploadObjectForTest();
        }
    }

    private void initLocalFile(){
        String[] fileNames = {"file1k","file10k","file100k","file1m","file10m"};
        int[] fileSize = {1024,10240,102400,1024000,10240000};

        for (int i = 0; i < fileNames.length; i++) {
            try {
                String filePath = OSSTestConfig.FILE_DIR + fileNames[i];
                Log.d("OSSTEST","filePath : " + filePath);
                File path = new File(OSSTestConfig.FILE_DIR);
                File file = new File(filePath);
                if( !path.exists()) {
                    Log.d("OSSTEST", "Create the path:" + path.getAbsolutePath());
                    path.mkdir();
                }
                if (!file.exists()) {
                    file.createNewFile();
                    Log.d("OSSTEST","create : " + file.getAbsolutePath());
                }else{
                    return;
                }
                Log.d("OSSTEST","write file : " + filePath);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] data = new byte[fileSize[i]];
                fos.write(data);
                fos.close();
                Log.d("OSSTEST","file write" +fileNames[i]+" ok");
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public void testCopyObject() throws Exception {
        DeleteObjectRequest delete = new DeleteObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");
        oss.deleteObject(delete);

        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey,
                OSSTestConfig.ANDROID_TEST_BUCKET, "testCopy");

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
