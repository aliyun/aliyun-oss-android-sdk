package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ListMultipartUploadsRequest;
import com.alibaba.sdk.android.oss.model.ListMultipartUploadsResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.MultipartUpload;
import com.alibaba.sdk.android.oss.model.OSSObjectSummary;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by jingdan on 2018/2/6.
 */

public class OSSTestUtils {

    public static void cleanBucket(OSS oss, String bucket) throws Exception {
        ListObjectsRequest listRequest = new ListObjectsRequest(bucket);
        listRequest.setMaxKeys(1000);
        ListObjectsResult listObjectsResult = oss.listObjects(listRequest);
        List<OSSObjectSummary> objectSummaries = listObjectsResult.getObjectSummaries();

        //delete objects
        if (objectSummaries != null && objectSummaries.size() > 0) {
            for (OSSObjectSummary object : objectSummaries) {
                DeleteObjectRequest delete = new DeleteObjectRequest(bucket, object.getKey());
                oss.deleteObject(delete);
            }
        }

        //delete multipart uploads
        ListMultipartUploadsRequest multipartUploadsRequest = new ListMultipartUploadsRequest(bucket);
        multipartUploadsRequest.setMaxUploads(1000);
        ListMultipartUploadsResult listMultipartUploadsResult = oss.listMultipartUploads(multipartUploadsRequest);
        if (listMultipartUploadsResult.getMultipartUploads().size() > 0) {
            for (MultipartUpload upload : listMultipartUploadsResult.getMultipartUploads()) {
                AbortMultipartUploadRequest abort = new AbortMultipartUploadRequest(bucket, upload.getKey(), upload.getUploadId());
                oss.abortMultipartUpload(abort);
            }
        }

        //delete bucket
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(bucket);
        oss.deleteBucket(deleteBucketRequest);
    }

    public static void checkFileMd5(OSS oss, String bucket, String objectKey, String filePath) throws IOException, NoSuchAlgorithmException, ClientException, ServiceException {
        GetObjectRequest getRq = new GetObjectRequest(bucket, objectKey);
        GetObjectResult getRs = oss.getObject(getRq);
        String localMd5 = BinaryUtil.calculateMd5Str(filePath);
        String remoteMd5 = getMd5(getRs);
        assertEquals(true, localMd5.equals(remoteMd5));
        assertNotNull(getRs);
        assertEquals(200, getRs.getStatusCode());
    }

    public static String getMd5(GetObjectResult getRs) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] buffer = new byte[8 * 1024];
        InputStream is = getRs.getObjectContent();
        int len;
        while ((len = is.read(buffer)) != -1) {
            digest.update(buffer, 0, len);
        }
        is.close();
        return BinaryUtil.getMd5StrFromBytes(digest.digest());
    }

    public static String produceBucketName(String caseName) {
        return "oss-android-" + caseName.toLowerCase();
    }
}
