/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss;

import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
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

/**
 * 阿里云开放存储服务（Open Storage Service， OSS）的访问接口。
 * <p>
 * 阿里云存储服务（Open Storage Service，简称OSS），是阿里云对外提供的海量，安全，低成本，
 * 高可靠的云存储服务。用户可以通过简单的REST接口，在任何时间、任何地点上传和下载数据，
 * 也可以使用WEB页面对数据进行管理。<br />
 * 基于OSS，用户可以搭建出各种多媒体分享网站、网盘、个人企业数据备份等基于大规模数据的服务。
 * </p>
 *
 * <p>
 * OSS为SDK的接口类，封装了OSS的RESTFul Api接口，考虑到移动端不能在UI线程发起网络请求的编程规范，
 * SDK为所有接口提供了异步的调用形式，也提供了同步接口。
 * </p>
 */
public interface OSS {

    /**
     * 异步上传文件
     * Put Object用于上传文件。
     *
     * @param request 请求信息
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<PutObjectResult> asyncPutObject(
            PutObjectRequest request, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback);

    /**
     * 同步上传文件
     * Put Object用于上传文件。
     *
     * @param request 请求信息
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public PutObjectResult putObject(PutObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步下载文件
     * 用于获取某个Object，此操作要求用户对该Object有读权限。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<GetObjectResult> asyncGetObject(
            GetObjectRequest request, OSSCompletedCallback<GetObjectRequest, GetObjectResult> completedCallback);

    /**
     * 同步下载文件
     * 用于获取某个Object，此操作要求用户对该Object有读权限。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public GetObjectResult getObject(GetObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步删除文件
     * DeleteObject用于删除某个Object。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<DeleteObjectResult> asyncDeleteObject(
            DeleteObjectRequest request, OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult> completedCallback);

    /**
     * 同步删除文件
     * DeleteObject用于删除某个Object。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public DeleteObjectResult deleteObject(DeleteObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步追加文件
     * Append Object以追加写的方式上传文件。
     * 通过Append Object操作创建的Object类型为Appendable Object，而通过Put Object上传的Object是Normal Object。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<AppendObjectResult> asyncAppendObject(
            AppendObjectRequest request, OSSCompletedCallback<AppendObjectRequest, AppendObjectResult> completedCallback);

    /**
     * 同步追加文件
     * Append Object以追加写的方式上传文件。
     * 通过Append Object操作创建的Object类型为Appendable Object，而通过Put Object上传的Object是Normal Object。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public AppendObjectResult appendObject(AppendObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步获取文件元信息
     * Head Object只返回某个Object的meta信息，不返回文件内容。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<HeadObjectResult> asyncHeadObject(
            HeadObjectRequest request, OSSCompletedCallback<HeadObjectRequest, HeadObjectResult> completedCallback);

    /**
     * 同步获取文件元信息
     * Head Object只返回某个Object的meta信息，不返回文件内容。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public HeadObjectResult headObject(HeadObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步复制文件
     * 拷贝一个在OSS上已经存在的object成另外一个object，可以发送一个PUT请求给OSS，并在PUT请求头中添加元素“x-oss-copy-source”来指定拷贝源。
     * OSS会自动判断出这是一个Copy操作，并直接在服务器端执行该操作。如果拷贝成功，则返回新的object信息给用户。
     * 该操作适用于拷贝小于1GB的文件，当拷贝一个大于1GB的文件时，必须使用Multipart Upload操作，具体见Upload Part Copy。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<CopyObjectResult> asyncCopyObject(
            CopyObjectRequest request, OSSCompletedCallback<CopyObjectRequest, CopyObjectResult> completedCallback);

    /**
     * 同步复制文件
     * 拷贝一个在OSS上已经存在的object成另外一个object，可以发送一个PUT请求给OSS，并在PUT请求头中添加元素“x-oss-copy-source”来指定拷贝源。
     * OSS会自动判断出这是一个Copy操作，并直接在服务器端执行该操作。如果拷贝成功，则返回新的object信息给用户。
     * 该操作适用于拷贝小于1GB的文件，当拷贝一个大于1GB的文件时，必须使用Multipart Upload操作，具体见Upload Part Copy。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public CopyObjectResult copyObject(CopyObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步创建bucket
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<CreateBucketResult> asyncCreateBucket(
            CreateBucketRequest request, OSSCompletedCallback<CreateBucketRequest, CreateBucketResult> completedCallback);

    /**
     * 同步创建bucket
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public CreateBucketResult createBucket (CreateBucketRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步删除bucket
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<DeleteBucketResult> asyncDeleteBucket(
            DeleteBucketRequest request, OSSCompletedCallback<DeleteBucketRequest, DeleteBucketResult> completedCallback);

    /**
     * 同步删除bucket
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public DeleteBucketResult deleteBucket (DeleteBucketRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步获取bucket ACL权限
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<GetBucketACLResult> asyncGetBucketACL(
            GetBucketACLRequest request, OSSCompletedCallback<GetBucketACLRequest, GetBucketACLResult> completedCallback);

    /**
     * 同步获取bucket ACL权限
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public GetBucketACLResult getBucketACL (GetBucketACLRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步罗列文件
     * Get Bucket操作可用来list Bucket中所有Object的信息。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<ListObjectsResult> asyncListObjects(
            ListObjectsRequest request, OSSCompletedCallback<ListObjectsRequest, ListObjectsResult> completedCallback);

    /**
     * 同步罗列文件
     * Get Bucket操作可用来list Bucket中所有Object的信息。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ListObjectsResult listObjects(ListObjectsRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步初始化分块上传
     * 使用Multipart Upload模式传输数据前，必须先调用该接口来通知OSS初始化一个Multipart Upload事件。
     * 该接口会返回一个OSS服务器创建的全局唯一的Upload ID，用于标识本次Multipart Upload事件。
     * 用户可以根据这个ID来发起相关的操作，如中止Multipart Upload、查询Multipart Upload等。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<InitiateMultipartUploadResult> asyncInitMultipartUpload(
            InitiateMultipartUploadRequest request, OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> completedCallback);

    /**
     * 同步初始化分块上传
     * 使用Multipart Upload模式传输数据前，必须先调用该接口来通知OSS初始化一个Multipart Upload事件。
     * 该接口会返回一个OSS服务器创建的全局唯一的Upload ID，用于标识本次Multipart Upload事件。
     * 用户可以根据这个ID来发起相关的操作，如中止Multipart Upload、查询Multipart Upload等。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public InitiateMultipartUploadResult initMultipartUpload(InitiateMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步上传分块
     * 初始化一个Multipart Upload之后，可以根据指定的Object名和Upload ID来分块（Part）上传数据。
     * 每一个上传的Part都有一个标识它的号码（part number，范围是1~10,000）。
     * 对于同一个Upload ID，该号码不但唯一标识这一块数据，也标识了这块数据在整个文件内的相对位置。
     * 如果你用同一个part号码，上传了新的数据，那么OSS上已有的这个号码的Part数据将被覆盖。
     * 除了最后一块Part以外，其他的part最小为100KB；最后一块Part没有大小限制。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<UploadPartResult> asyncUploadPart(
            UploadPartRequest request, OSSCompletedCallback<UploadPartRequest, UploadPartResult> completedCallback);

    /**
     * 同步上传分块
     * 初始化一个Multipart Upload之后，可以根据指定的Object名和Upload ID来分块（Part）上传数据。
     * 每一个上传的Part都有一个标识它的号码（part number，范围是1~10,000）。
     * 对于同一个Upload ID，该号码不但唯一标识这一块数据，也标识了这块数据在整个文件内的相对位置。
     * 如果你用同一个part号码，上传了新的数据，那么OSS上已有的这个号码的Part数据将被覆盖。
     * 除了最后一块Part以外，其他的part最小为100KB；最后一块Part没有大小限制。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public UploadPartResult uploadPart(UploadPartRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步完成分块上传
     * 在将所有数据Part都上传完成后，必须调用Complete Multipart Upload API来完成整个文件的Multipart Upload。
     * 在执行该操作时，用户必须提供所有有效的数据Part的列表（包括part号码和ETAG）；OSS收到用户提交的Part列表后，会逐一验证每个数据Part的有效性。
     * 当所有的数据Part验证通过后，OSS将把这些数据part组合成一个完整的Object。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<CompleteMultipartUploadResult> asyncCompleteMultipartUpload(
            CompleteMultipartUploadRequest request, OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback);

    /**
     * 同步完成分块上传
     * 在将所有数据Part都上传完成后，必须调用Complete Multipart Upload API来完成整个文件的Multipart Upload。
     * 在执行该操作时，用户必须提供所有有效的数据Part的列表（包括part号码和ETAG）；OSS收到用户提交的Part列表后，会逐一验证每个数据Part的有效性。
     * 当所有的数据Part验证通过后，OSS将把这些数据part组合成一个完整的Object。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步取消分块上传
     * 该接口可以根据用户提供的Upload ID中止其对应的Multipart Upload事件。
     * 当一个Multipart Upload事件被中止后，就不能再使用这个Upload ID做任何操作，已经上传的Part数据也会被删除。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<AbortMultipartUploadResult> asyncAbortMultipartUpload(
            AbortMultipartUploadRequest request, OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult> completedCallback);

    /**
     * 同步取消分块上传
     * 该接口可以根据用户提供的Upload ID中止其对应的Multipart Upload事件。
     * 当一个Multipart Upload事件被中止后，就不能再使用这个Upload ID做任何操作，已经上传的Part数据也会被删除。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public AbortMultipartUploadResult abortMultipartUpload(AbortMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * 异步罗列分块
     * List Parts命令可以罗列出指定Upload ID所属的所有已经上传成功Part。
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<ListPartsResult> asyncListParts(
            ListPartsRequest request, OSSCompletedCallback<ListPartsRequest, ListPartsResult> completedCallback);

    /**
     * 同步罗列分块
     * List Parts命令可以罗列出指定Upload ID所属的所有已经上传成功Part。
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ListPartsResult listParts(ListPartsRequest request)
            throws ClientException, ServiceException;


    /******************** extension function **********************/

    /**
     * 更新鉴权设置，调用后，旧的设置会失效
     */
    public void updateCredentialProvider(OSSCredentialProvider credentialProvider);

    /**
     * 异步断点上传
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public OSSAsyncTask<ResumableUploadResult> asyncResumableUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback);


    /**
     * 同步断点上传
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ResumableUploadResult resumableUpload(ResumableUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * 签名Object的访问URL，以便授权第三方访问
     *
     * @param bucketName 存储Object的Bucket名
     * @param objectKey Object名
     * @param expiredTimeInSeconds URL的有效时长，秒为单位
     * @return
     * @throws ClientException
     */
    public String presignConstrainedObjectURL(String bucketName, String objectKey, long expiredTimeInSeconds)
            throws ClientException;

    /**
     * 签名公开可访问的URL
     *
     * @param bucketName 存储Object的Bucket名
     * @param objectKey  Object名
     * @return
     */
    public String presignPublicObjectURL(String bucketName, String objectKey);

    /**
     * 检查指定Object是否存在
     *
     * @param bucketName
     * @param objectKey
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public boolean doesObjectExist(String bucketName, String objectKey)
        throws ClientException, ServiceException;
}
