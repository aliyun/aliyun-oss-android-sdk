/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss;

import android.app.Service;
import android.content.Context;

import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.internal.ObjectURLPresigner;
import com.alibaba.sdk.android.oss.internal.InternalRequestOperation;
import com.alibaba.sdk.android.oss.internal.ExtensionRequestOperation;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 访问阿里云开放存储服务（Open Storage Service， OSS）的入口类。
 */
public class OSSClient implements OSS {

    private URI endpointURI;
    private OSSCredentialProvider credentialProvider;
    private InternalRequestOperation internalRequestOperation;
    private ExtensionRequestOperation extensionRequestOperation;

    /**
     * 构造一个OSSClient实例
     *
     * @param context android应用的applicationContext
     * @param endpoint OSS访问域名，参考http://help.aliyun.com/document_detail/oss/user_guide/endpoint_region.html
     * @param credentialProvider 鉴权设置
     */
    public OSSClient(Context context, String endpoint, OSSCredentialProvider credentialProvider) {
        this(context, endpoint, credentialProvider, null);
    }

    /**
     * 构造一个OSSClient实例
     *
     * @param context android应用的applicationContext
     * @param endpoint OSS访问域名，参考http://help.aliyun.com/document_detail/oss/user_guide/endpoint_region.html
     * @param credentialProvider 鉴权设置
     * @param conf 网络参数设置
     */
    public OSSClient(Context context, String endpoint, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
        try {
            if (!endpoint.startsWith("http")) {
                endpoint = "http://" + endpoint;
            }
            this.endpointURI = new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Endpoint must be a string like 'http://oss-cn-****.aliyuncs.com'," +
                    "or your cname like 'http://image.cnamedomain.com'!");
        }
        this.credentialProvider = credentialProvider;

        internalRequestOperation = new InternalRequestOperation(context, endpointURI, credentialProvider, conf);
        extensionRequestOperation = new ExtensionRequestOperation(internalRequestOperation);
    }

    @Override
    public OSSAsyncTask<CreateBucketResult> asyncCreateBucket(
            CreateBucketRequest request, OSSCompletedCallback<CreateBucketRequest, CreateBucketResult> completedCallback) {

        return internalRequestOperation.createBucket(request, completedCallback);
    }

    @Override
    public CreateBucketResult createBucket(CreateBucketRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.createBucket(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<DeleteBucketResult> asyncDeleteBucket(
            DeleteBucketRequest request, OSSCompletedCallback<DeleteBucketRequest, DeleteBucketResult> completedCallback) {

        return internalRequestOperation.deleteBucket(request, completedCallback);
    }

    @Override
    public DeleteBucketResult deleteBucket(DeleteBucketRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.deleteBucket(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<GetBucketACLResult> asyncGetBucketACL(
            GetBucketACLRequest request, OSSCompletedCallback<GetBucketACLRequest, GetBucketACLResult> completedCallback) {

        return internalRequestOperation.getBucketACL(request, completedCallback);
    }

    @Override
    public GetBucketACLResult getBucketACL(GetBucketACLRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.getBucketACL(request, null).getResult();
    }

    @Override
	public OSSAsyncTask<PutObjectResult> asyncPutObject(
            PutObjectRequest request, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {

        return internalRequestOperation.putObject(request, completedCallback);
	}

    @Override
    public PutObjectResult putObject(PutObjectRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.putObject(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<GetObjectResult> asyncGetObject(
            GetObjectRequest request, OSSCompletedCallback<GetObjectRequest, GetObjectResult> completedCallback) {

        return internalRequestOperation.getObject(request, completedCallback);
    }

    @Override
    public GetObjectResult getObject(GetObjectRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.getObject(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<DeleteObjectResult> asyncDeleteObject(
            DeleteObjectRequest request, OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult> completedCallback) {

        return internalRequestOperation.deleteObject(request, completedCallback);
    }

    @Override
    public DeleteObjectResult deleteObject(DeleteObjectRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.deleteObject(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<AppendObjectResult> asyncAppendObject(
            AppendObjectRequest request, OSSCompletedCallback<AppendObjectRequest, AppendObjectResult> completedCallback) {

        return internalRequestOperation.appendObject(request, completedCallback);
    }

    @Override
    public AppendObjectResult appendObject(AppendObjectRequest request)
        throws ClientException, ServiceException {

        return internalRequestOperation.appendObject(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<HeadObjectResult> asyncHeadObject(HeadObjectRequest request, OSSCompletedCallback<HeadObjectRequest, HeadObjectResult> completedCallback) {

        return internalRequestOperation.headObject(request, completedCallback);
    }

    @Override
    public HeadObjectResult headObject(HeadObjectRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.headObject(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<CopyObjectResult> asyncCopyObject(CopyObjectRequest request, OSSCompletedCallback<CopyObjectRequest, CopyObjectResult> completedCallback) {

        return internalRequestOperation.copyObject(request, completedCallback);
    }

    @Override
    public CopyObjectResult copyObject(CopyObjectRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.copyObject(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<ListObjectsResult> asyncListObjects(
            ListObjectsRequest request, OSSCompletedCallback<ListObjectsRequest, ListObjectsResult> completedCallback) {

        return internalRequestOperation.listObjects(request, completedCallback);
    }

    @Override
    public ListObjectsResult listObjects(ListObjectsRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.listObjects(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<InitiateMultipartUploadResult> asyncInitMultipartUpload(InitiateMultipartUploadRequest request, OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> completedCallback) {

        return internalRequestOperation.initMultipartUpload(request, completedCallback);
    }

    @Override
    public InitiateMultipartUploadResult initMultipartUpload(InitiateMultipartUploadRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.initMultipartUpload(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<UploadPartResult> asyncUploadPart(UploadPartRequest request, OSSCompletedCallback<UploadPartRequest, UploadPartResult> completedCallback) {

        return internalRequestOperation.uploadPart(request, completedCallback);
    }

    @Override
    public UploadPartResult uploadPart(UploadPartRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.uploadPart(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<CompleteMultipartUploadResult> asyncCompleteMultipartUpload(CompleteMultipartUploadRequest request, OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback) {

        return internalRequestOperation.completeMultipartUpload(request, completedCallback);
    }

    @Override
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.completeMultipartUpload(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<AbortMultipartUploadResult> asyncAbortMultipartUpload(AbortMultipartUploadRequest request, OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult> completedCallback) {

        return internalRequestOperation.abortMultipartUpload(request, completedCallback);
    }

    @Override
    public AbortMultipartUploadResult abortMultipartUpload(AbortMultipartUploadRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.abortMultipartUpload(request, null).getResult();
    }

    @Override
    public OSSAsyncTask<ListPartsResult> asyncListParts(ListPartsRequest request, OSSCompletedCallback<ListPartsRequest, ListPartsResult> completedCallback) {

        return internalRequestOperation.listParts(request, completedCallback);
    }

    @Override
    public ListPartsResult listParts(ListPartsRequest request)
            throws ClientException, ServiceException {

        return internalRequestOperation.listParts(request, null).getResult();
    }

    @Override
    public void updateCredentialProvider(OSSCredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
        internalRequestOperation.setCredentialProvider(credentialProvider);
    }

    @Override
    public OSSAsyncTask<ResumableUploadResult> asyncResumableUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback) {

        return extensionRequestOperation.resumableUpload(request, completedCallback);
    }

    @Override
    public ResumableUploadResult resumableUpload(ResumableUploadRequest request)
            throws ClientException, ServiceException {

        return extensionRequestOperation.resumableUpload(request, null).getResult();
    }

    @Override
    public String presignConstrainedObjectURL(String bucketName, String objectKey, long expiredTimeInSeconds)
            throws ClientException {

        return new ObjectURLPresigner(endpointURI, credentialProvider).presignConstrainedURL(bucketName, objectKey, expiredTimeInSeconds);
    }

    @Override
    public String presignPublicObjectURL(String bucketName, String objectKey) {

        return new ObjectURLPresigner(endpointURI, credentialProvider).presignPublicURL(bucketName, objectKey);
    }

    @Override
    public boolean doesObjectExist(String bucketName, String objectKey)
        throws ClientException, ServiceException {

        return extensionRequestOperation.doesObjectExist(bucketName, objectKey);
    }
}
