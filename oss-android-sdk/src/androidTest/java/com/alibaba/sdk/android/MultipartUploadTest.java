package com.alibaba.sdk.android;

import android.test.AndroidTestCase;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PartSummary;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class MultipartUploadTest extends AndroidTestCase {

    OSS oss;

    @Override
    public void setUp() throws Exception {
        if (oss == null) {
            Thread.sleep(5 * 1000); // for logcat initialization
            OSSLog.enableLog();
            oss = new OSSClient(getContext(), OSSTestConfig.ENDPOINT, OSSTestConfig.credetialProvider);
        }
    }

    public void testInitAndDeleteMultipartUpload() throws Exception {
        String objectKey = "multipart";
        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        OSSLog.logD("uploadid - " + uploadId);
        AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);
        AbortMultipartUploadResult abortResult = oss.abortMultipartUpload(abort);

        assertNotNull(abortResult);

        ListPartsRequest listpart = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId);
        try {
            oss.listParts(listpart);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
    }

    public void testUploadPartsAndListAndComplete() throws Exception {
        String objectKey = "multipart";

        InitiateMultipartUploadRequest init = new InitiateMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey);
        InitiateMultipartUploadResult initResult = oss.initMultipartUpload(init);

        assertNotNull(initResult.getUploadId());
        String uploadId = initResult.getUploadId();

        byte[] data = new byte[100 * 1024];
        UploadPartRequest uploadPart = new UploadPartRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, 1);
        uploadPart.setPartContent(data);

        oss.uploadPart(uploadPart);

        uploadPart = new UploadPartRequest(OSSTestConfig.ANDROID_TEST_BUCKET, objectKey, uploadId, 2);
        uploadPart.setPartContent(data);

        oss.uploadPart(uploadPart);

        ListPartsRequest listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        ListPartsResult result = oss.listParts(listParts);
        assertEquals(2, result.getParts().size());

        List<PartETag> partETagList = new ArrayList<PartETag>();
        for (PartSummary part : result.getParts()) {
            partETagList.add(new PartETag(part.getPartNumber(), part.getETag()));
        }

        CompleteMultipartUploadRequest complete = new CompleteMultipartUploadRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId, partETagList);

        CompleteMultipartUploadResult completeResult = oss.completeMultipartUpload(complete);

        assertNotNull(completeResult.getLocation());

        listParts = new ListPartsRequest(OSSTestConfig.ANDROID_TEST_BUCKET,
                objectKey, uploadId);

        try {
            oss.listParts(listParts);
        } catch (ServiceException e) {
            assertEquals(404, e.getStatusCode());
        }
    }
}
