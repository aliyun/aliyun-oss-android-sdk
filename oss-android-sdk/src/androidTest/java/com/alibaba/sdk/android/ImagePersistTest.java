package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ImagePersistRequest;
import com.alibaba.sdk.android.oss.model.ImagePersistResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.TriggerCallbackRequest;
import com.alibaba.sdk.android.oss.model.TriggerCallbackResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huaixu on 2018/1/30.
 */

public class ImagePersistTest extends BaseTestCase {
    public static final String JPG_OBJECT_KEY = "JPG_OBJECT_KEY";
    public static final String persist2Obj = "persis2Obj";


    private String imgPath = OSSTestConfig.FILE_DIR + "shilan.jpg";

    @Override
    void initTestData() throws Exception {
        PutObjectRequest putImg = new PutObjectRequest(mBucketName,
                JPG_OBJECT_KEY, imgPath);
        oss.putObject(putImg);
    }


    public void testImagePersist() throws Exception {
        ImagePersistRequest request = new ImagePersistRequest(mBucketName, JPG_OBJECT_KEY,mBucketName,persist2Obj,"resize,w_100");
        try{
            ImagePersistResult result = oss.imagePersist(request);

            HeadObjectRequest head = new HeadObjectRequest(mBucketName, persist2Obj);

            HeadObjectResult headResult = oss.headObject(head);


            assertNotNull(headResult.getMetadata().getContentType());
            assertEquals(200, headResult.getStatusCode());

        }catch (Exception e){
            e.printStackTrace();
            assertNull(e);
        }

    }
}
