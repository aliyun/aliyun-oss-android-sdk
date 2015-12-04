package com.alibaba.sdk.android.oss.sample;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhouzhuo on 12/4/15.
 */
public class MultipartUploadSamples {
    private OSS oss;
    private String testBucket;
    private String testObject;
    private String uploadFilePath;

    public MultipartUploadSamples(OSS client, String testBucket, String testObject, String uploadFilePath) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
        this.uploadFilePath = uploadFilePath;
    }

    public void multipartUpload() throws ClientException, ServiceException, IOException {

        String uploadId;

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(testBucket, testObject);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        uploadId = initResult.getUploadId();

        long partSize = 128 * 1024;

        int currentIndex = 1;

        InputStream input = new FileInputStream(new File(uploadFilePath));
    }
}
