/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * <p>
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss;

import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.*;

import java.io.IOException;

/**
 * The access entry point interface for OSS (Open Storage Service).
 * <p>
 * Open Storage Service is the public massive, secure, low cost and high reliable storage service.
 * Users could use RESTFul APIs to access the data at anytime and anywhere.
 * Users could also manage the data and its configuration with the web console.
 * <br />
 * Based on OSS, user could build apps that need to have massive data access, such asmedia sharing app,
 * cloud disk app, personal or enterprise data backup apps,etc
 * </p>
 * <p>
 * <p>
 * OSS interface is the SDK's entry point interface. It wraps the OSS RESTful APIs in sync and async APIs
 * the network request cannot be issued in UI thread.
 * </p>
 */
public interface OSS {

    /**
     * Asynchronously list buckets
     * RESTFul API:PutObject
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<ListBucketsResult> asyncListBuckets(
            ListBucketsRequest request, OSSCompletedCallback<ListBucketsRequest, ListBucketsResult> completedCallback);

    /**
     * Synchronously list buckets
     * RESTFul API:PutObject
     *
     * @param request
     * @return
     */
    public ListBucketsResult listBuckets(ListBucketsRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously upload file
     * RESTFul API:PutObject
     *
     * @param request           the PutObjectRequest instance
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<PutObjectResult> asyncPutObject(
            PutObjectRequest request, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback);

    /**
     * Synchronously upload file
     * RESTFul API:PutObject
     *
     * @param request the PutObjectRequest instance
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public PutObjectResult putObject(PutObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously download file
     * Gets the object. (the caller needs the read permission on the object)
     * RESTFul API:GetObject
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<GetObjectResult> asyncGetObject(
            GetObjectRequest request, OSSCompletedCallback<GetObjectRequest, GetObjectResult> completedCallback);

    /**
     * Synchronously download file
     * Gets the object. (the caller needs the read permission on the object)
     * RESTFul API:GetObject
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public GetObjectResult getObject(GetObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously delete file
     * RESTFul API:DeleteObject
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<DeleteObjectResult> asyncDeleteObject(
            DeleteObjectRequest request, OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult> completedCallback);

    /**
     * Synchronously delete file
     * RESTFul API:DeleteObject
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public DeleteObjectResult deleteObject(DeleteObjectRequest request)
            throws ClientException, ServiceException;


    /**
     * Asynchronously delete multiple objects
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<DeleteMultipleObjectResult> asyncDeleteMultipleObject(
            DeleteMultipleObjectRequest request, OSSCompletedCallback<DeleteMultipleObjectRequest, DeleteMultipleObjectResult> completedCallback);


    /**
     * delete multiple objects
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public DeleteMultipleObjectResult deleteMultipleObject(DeleteMultipleObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously append the file
     * The object created by this method is Appendable type. While the object created by PUT Object is
     * normal type (not appendable).
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<AppendObjectResult> asyncAppendObject(
            AppendObjectRequest request, OSSCompletedCallback<AppendObjectRequest, AppendObjectResult> completedCallback);

    /**
     * Synchronously append the file
     * The object created by this method is Appendable type. While the object created by PUT Object is
     * normal type (not appendable).
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public AppendObjectResult appendObject(AppendObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously get the file's metadata.
     * Head Object only returns the metadata information, not the object content.
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<HeadObjectResult> asyncHeadObject(
            HeadObjectRequest request, OSSCompletedCallback<HeadObjectRequest, HeadObjectResult> completedCallback);

    /**
     * Synchronously get the file's metadata.
     * Head Object only returns the metadata information, not the object content.
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public HeadObjectResult headObject(HeadObjectRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously copy a file
     * It copies an existing file to another one.
     * This API just sends the PUT object request to OSS with the x-oss-copy-source information.
     * And therefore the file's content is not downloaded or uploaded from to server.
     * This API only fit the files whose size is less than 1GB.
     * For bigger files, please use multipart copy API. Checks out the multipart upload APIs.
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<CopyObjectResult> asyncCopyObject(
            CopyObjectRequest request, OSSCompletedCallback<CopyObjectRequest, CopyObjectResult> completedCallback);

    /**
     * Synchronously copy a file
     * It copies an existing file to another one.
     * This API just sends the PUT object request to OSS with the x-oss-copy-source information.
     * And therefore the file's content is not downloaded or uploaded from to server.
     * This API only fit the files whose size is less than 1GB.
     * For bigger files, please use multipart copy API. Checks out the multipart upload APIs.
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public CopyObjectResult copyObject(CopyObjectRequest request)
            throws ClientException, ServiceException;


    public OSSAsyncTask<GetObjectACLResult> asyncGetObjectACL(
            GetObjectACLRequest request, OSSCompletedCallback<GetObjectACLRequest, GetObjectACLResult> completedCallback);

    public GetObjectACLResult getObjectACL(GetObjectACLRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously create bucket
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<CreateBucketResult> asyncCreateBucket(
            CreateBucketRequest request, OSSCompletedCallback<CreateBucketRequest, CreateBucketResult> completedCallback);

    /**
     * Synchronously create bucket
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public CreateBucketResult createBucket(CreateBucketRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously delete bucket
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<DeleteBucketResult> asyncDeleteBucket(
            DeleteBucketRequest request, OSSCompletedCallback<DeleteBucketRequest, DeleteBucketResult> completedCallback);

    /**
     * Synchronously delete bucket
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public DeleteBucketResult deleteBucket(DeleteBucketRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously get bucket info
     *
     * @param request
     *             A {@link GetBucketInfoRequest} instance which specifies the bucket
     *            name.
     * @param completedCallback
     *            A {@link OSSCompletedCallback<GetBucketInfoRequest, GetBucketInfoResult>} instance that specifies callback functions
     * @return
     */
    public OSSAsyncTask<GetBucketInfoResult> asyncGetBucketInfo(
            GetBucketInfoRequest request, OSSCompletedCallback<GetBucketInfoRequest, GetBucketInfoResult> completedCallback);

    /**
     * Gets the Bucket's basic information as well as its ACL.
     *
     * @param request
     *             A {@link GetBucketInfoRequest} instance which specifies the bucket
     *            name.
     * @return A {@link GetBucketInfoResult} instance.
     * @throws ClientException
     *             OSS Client side exception.
     * @throws ServiceException
     *             OSS Server side exception.
     */
    public GetBucketInfoResult getBucketInfo(GetBucketInfoRequest request) throws ClientException, ServiceException;

    /**
     * Asynchronously get bucket ACL
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<GetBucketACLResult> asyncGetBucketACL(
            GetBucketACLRequest request, OSSCompletedCallback<GetBucketACLRequest, GetBucketACLResult> completedCallback);

    /**
     * Synchronously get bucket ACL
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public GetBucketACLResult getBucketACL(GetBucketACLRequest request)
            throws ClientException, ServiceException;

    /**
     * Synchronously get bucket referer
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public GetBucketRefererResult getBucketReferer(GetBucketRefererRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously put bucket referer
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<PutBucketRefererResult> asyncPutBucketReferer(
            PutBucketRefererRequest request, OSSCompletedCallback<PutBucketRefererRequest, PutBucketRefererResult> completedCallback);

    /**
     * Synchronously put bucket referer
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public PutBucketRefererResult putBucketReferer(PutBucketRefererRequest request)
            throws ClientException, ServiceException;

    /**
     * Synchronously delete bucket logging
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public DeleteBucketLoggingResult deleteBucketLogging(DeleteBucketLoggingRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously delete bucket logging
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<DeleteBucketLoggingResult> asyncDeleteBucketLogging(
            DeleteBucketLoggingRequest request, OSSCompletedCallback<DeleteBucketLoggingRequest, DeleteBucketLoggingResult> completedCallback);

    /**
     * Synchronously put bucket logging
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public PutBucketLoggingResult putBucketLogging(PutBucketLoggingRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously delete bucket logging
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<PutBucketLoggingResult> asyncPutBucketLogging(
            PutBucketLoggingRequest request, OSSCompletedCallback<PutBucketLoggingRequest, PutBucketLoggingResult> completedCallback);

    /**
     * Synchronously get bucket logging
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public GetBucketLoggingResult getBucketLogging(GetBucketLoggingRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously get bucket logging
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<GetBucketLoggingResult> asyncGetBucketLogging(
            GetBucketLoggingRequest request, OSSCompletedCallback<GetBucketLoggingRequest, GetBucketLoggingResult> completedCallback);

    /**
     * Asynchronously get bucket referer
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<GetBucketRefererResult> asyncGetBucketReferer(
            GetBucketRefererRequest request, OSSCompletedCallback<GetBucketRefererRequest, GetBucketRefererResult> completedCallback);

    /**
     * Synchronously put bucket lifecycle
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public PutBucketLifecycleResult putBucketLifecycle(PutBucketLifecycleRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously put bucket lifecycle
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<PutBucketLifecycleResult> asyncPutBucketLifecycle(
            PutBucketLifecycleRequest request, OSSCompletedCallback<PutBucketLifecycleRequest, PutBucketLifecycleResult> completedCallback);

    /**
     * Synchronously get bucket lifecycle
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public GetBucketLifecycleResult getBucketLifecycle(GetBucketLifecycleRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously get bucket lifecycle
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<GetBucketLifecycleResult> asyncGetBucketLifecycle(
            GetBucketLifecycleRequest request, OSSCompletedCallback<GetBucketLifecycleRequest, GetBucketLifecycleResult> completedCallback);

    /**
     * Synchronously delete bucket lifecycle
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public DeleteBucketLifecycleResult deleteBucketLifecycle(DeleteBucketLifecycleRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously delete bucket lifecycle
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<DeleteBucketLifecycleResult> asyncDeleteBucketLifecycle(
            DeleteBucketLifecycleRequest request, OSSCompletedCallback<DeleteBucketLifecycleRequest, DeleteBucketLifecycleResult> completedCallback);

    /**
     * Asynchronously list files
     * Get Bucket API is for listing bucket's all object information (not data itself).
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<ListObjectsResult> asyncListObjects(
            ListObjectsRequest request, OSSCompletedCallback<ListObjectsRequest, ListObjectsResult> completedCallback);

    /**
     * Synchronously list files
     * Get Bucket API is for listing bucket's all object information (not data itself).
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ListObjectsResult listObjects(ListObjectsRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously initialize a multipart upload
     * Before use Multipart Upload for uploading data, this API is called to initiate the multipart upload,
     * which will get the upload Id from OSS.
     * Then this upload Id will be used in the subsequent calls, such as abort the multipart upload,
     * query the multipart upload, upload part, etc.
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<InitiateMultipartUploadResult> asyncInitMultipartUpload(
            InitiateMultipartUploadRequest request, OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> completedCallback);

    /**
     * Synchronously initialize a multipart upload
     * Before use Multipart Upload for uploading data, this API is called to initiate the multipart upload,
     * which will get the upload Id from OSS.
     * Then this upload Id will be used in the subsequent calls, such as abort the multipart upload,
     * query the multipart upload, upload part, etc.
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public InitiateMultipartUploadResult initMultipartUpload(InitiateMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously upload the part data
     * After the multipart upload is initialized, we can upload the part data with specified object key
     * and upload Id.
     * For each part to upload, it has a unique part number (from 1 to 10000).
     * And for the same upload Id, this part number identify the part and its position in the whole target
     * object. If the same part number and upload Id are uploaded with other data later, then this
     * part's data is overwritten.
     * Except the last part, the minimal part size is 100KB.
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<UploadPartResult> asyncUploadPart(
            UploadPartRequest request, OSSCompletedCallback<UploadPartRequest, UploadPartResult> completedCallback);

    /**
     * Synchronously upload the part data
     * After the multipart upload is initialized, we can upload the part data with specified object key
     * and upload Id.
     * For each part to upload, it has a unique part number (from 1 to 10000).
     * And for the same upload Id, this part number identify the part and its position in the whole target
     * object. If the same part number and upload Id are uploaded with other data later, then this
     * part's data is overwritten.
     * Except the last part, the minimal part size is 100KB.
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public UploadPartResult uploadPart(UploadPartRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously complete the multipart upload.
     * After uploading all parts' data, this API needs to be called to complete the whole upload.
     * To call this API, the valid list of the part numbers and ETags (wrapped as PartETag) are specified.
     * The OSS will validate very part and their rankings and then merge the parts into target file.
     * After this call, the parts data is unavailable to user.
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<CompleteMultipartUploadResult> asyncCompleteMultipartUpload(
            CompleteMultipartUploadRequest request, OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback);

    /**
     * Synchronously complete the multipart upload.
     * After uploading all parts' data, this API needs to be called to complete the whole upload.
     * To call this API, the valid list of the part numbers and ETags (wrapped as PartETag) are specified.
     * The OSS will validate very part and their rankings and then merge the parts into target file.
     * After this call, the parts data is unavailable to user.
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public CompleteMultipartUploadResult completeMultipartUpload(CompleteMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously cancel the multipart upload.
     * This API is to abort the multipart upload with specified upload Id.
     * When the multipart upload is aborted, the upload Id is invalid anymore and all parts data will
     * be deleted.
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<AbortMultipartUploadResult> asyncAbortMultipartUpload(
            AbortMultipartUploadRequest request, OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult> completedCallback);

    /**
     * Synchronously cancel the multipart upload.
     * This API is to abort the multipart upload with specified upload Id.
     * When the multipart upload is aborted, the upload Id is invalid anymore and all parts data will
     * be deleted.
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public AbortMultipartUploadResult abortMultipartUpload(AbortMultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously list parts uploaded
     * List Parts API could list all uploaded parts of the specified upload Id.
     *
     * @param request
     * @param completedCallback
     * @return
     */
    public OSSAsyncTask<ListPartsResult> asyncListParts(
            ListPartsRequest request, OSSCompletedCallback<ListPartsRequest, ListPartsResult> completedCallback);

    /**
     * Synchronously list parts uploaded
     * List Parts API could list all uploaded parts of the specified upload Id.
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ListPartsResult listParts(ListPartsRequest request)
            throws ClientException, ServiceException;


    /**
     * Asynchronously list multipart uploads
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public OSSAsyncTask<ListMultipartUploadsResult> asyncListMultipartUploads(
            ListMultipartUploadsRequest request, OSSCompletedCallback<ListMultipartUploadsRequest,
            ListMultipartUploadsResult> completedCallback);

    /**
     * Synchronously list multipart uploads
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ListMultipartUploadsResult listMultipartUploads(ListMultipartUploadsRequest request)
            throws ClientException, ServiceException;

    /******************** extension functions **********************/

    /**
     * Update the credential provider instance. The old one will not be used.
     */
    public void updateCredentialProvider(OSSCredentialProvider credentialProvider);

    /**
     * Asynchronously do a multipart upload
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public OSSAsyncTask<CompleteMultipartUploadResult> asyncMultipartUpload(
            MultipartUploadRequest request, OSSCompletedCallback<MultipartUploadRequest, CompleteMultipartUploadResult> completedCallback);


    /**
     * Synchronously do a multipart upload
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public CompleteMultipartUploadResult multipartUpload(MultipartUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * Asynchronously do a resumable upload
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public OSSAsyncTask<ResumableUploadResult> asyncResumableUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback);


    /**
     * Synchronously do a resumable upload
     *
     * @param request
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public ResumableUploadResult resumableUpload(ResumableUploadRequest request)
            throws ClientException, ServiceException;

    public OSSAsyncTask<ResumableUploadResult> asyncSequenceUpload(
            ResumableUploadRequest request, OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> completedCallback);


    public ResumableUploadResult sequenceUpload(ResumableUploadRequest request)
            throws ClientException, ServiceException;

    /**
     * Generates the signed url for 3rd parties accessing object
     *
     * @param request Generates the signed by custom config  @see {GeneratePresignedUrlRequest}
     * @return
     * @throws ClientException
     */
    public String presignConstrainedObjectURL(GeneratePresignedUrlRequest request)
            throws ClientException;

    /**
     * Generates the signed url for 3rd parties accessing object
     *
     * @param bucketName           bucket name
     * @param objectKey            Object key
     * @param expiredTimeInSeconds URL's expiration time in seconds
     * @return
     * @throws ClientException
     */
    public String presignConstrainedObjectURL(String bucketName, String objectKey, long expiredTimeInSeconds)
            throws ClientException;

    /**
     * Generates the signed  url for the public available object
     *
     * @param bucketName bucket name
     * @param objectKey  Object key
     * @return
     */
    public String presignPublicObjectURL(String bucketName, String objectKey);

    /**
     * Checks if the object exists in OSS
     *
     * @param bucketName
     * @param objectKey
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public boolean doesObjectExist(String bucketName, String objectKey)
            throws ClientException, ServiceException;

    /**
     * If the multipart upload is not aborted in a resumable upload,
     * this API needs to be called to abort the underlying multipart upload.
     *
     * @param request
     * @throws IOException
     */
    public void abortResumableUpload(ResumableUploadRequest request) throws IOException;

    public OSSAsyncTask<TriggerCallbackResult> asyncTriggerCallback(TriggerCallbackRequest request, OSSCompletedCallback<TriggerCallbackRequest, TriggerCallbackResult> completedCallback);

    public TriggerCallbackResult triggerCallback(TriggerCallbackRequest request) throws ClientException, ServiceException;

    public OSSAsyncTask<ImagePersistResult> asyncImagePersist(ImagePersistRequest request, OSSCompletedCallback<ImagePersistRequest, ImagePersistResult> completedCallback);

    public ImagePersistResult imagePersist(ImagePersistRequest request) throws ClientException, ServiceException;

    /**
     * Synchronously creates a symbol link to a target file under the bucket---this is not
     * supported for archive class bucket.
     *
     * @param request
     *            A {@link PutSymlinkRequest} instance that specifies the
     *            bucket name, symlink name.
     * @throws ClientException
     *             OSS Client side exception.
     * @throws ServiceException
     *             OSS Server side exception.
     * @return An instance of PutSymlinkResult
     */
    public PutSymlinkResult putSymlink(PutSymlinkRequest request) throws ClientException, ServiceException;

    /**
     * Asynchronously creates a symbol link to a target file under the bucket---this is not
     * supported for archive class bucket.
     *
     * @param request
     *            A {@link PutSymlinkRequest} instance that specifies the
     *            bucket name, symlink name.
     * @param completedCallback
     *            A {@link OSSCompletedCallback<PutSymlinkRequest, PutSymlinkResult>} instance that specifies callback functions
     * @return A {@link OSSAsyncTask<PutSymlinkResult>} instance.
     */
    public OSSAsyncTask<PutSymlinkResult> asyncPutSymlink(PutSymlinkRequest request, OSSCompletedCallback<PutSymlinkRequest, PutSymlinkResult> completedCallback);

    /**
     * Synchronously gets the symlink information for the given symlink name.
     *
     * @param request
     *            A {@link GetSymlinkRequest} instance which specifies the bucket
     *            name and symlink name.
     * @return The symlink information, including the target file name and its
     *         metadata.
     * @throws ClientException
     *             OSS Client side exception.
     * @throws ServiceException
     *             OSS Server side exception.
     * @return A {@link GetSymlinkResult} instance.
     */
    public GetSymlinkResult getSymlink(GetSymlinkRequest request) throws ClientException, ServiceException;

    /**
     * Asynchronously gets the symlink information for the given symlink name.
     *
     * @param request
     *            A {@link GetSymlinkRequest} instance which specifies the bucket
     *            name and symlink name.
     * @param completedCallback
     *            A {@link OSSCompletedCallback<GetSymlinkRequest, GetSymlinkResult>} instance that specifies callback functions
     * @return A {@link OSSAsyncTask<GetSymlinkResult>} instance.
     */
    public OSSAsyncTask<GetSymlinkResult> asyncGetSymlink(GetSymlinkRequest request, OSSCompletedCallback<GetSymlinkRequest, GetSymlinkResult> completedCallback);

    /**
     * Synchronously restores the object of archive storage. The function is not applicable to
     * Normal or IA storage. The restoreObject() needs to be called prior to
     * calling getObject() on an archive object.
     *
     * @param request
     *            A {@link RestoreObjectRequest} instance that specifies the bucket
     *            name and object key.
     * @return A {@link RestoreObjectResult} instance.
     */
    public RestoreObjectResult restoreObject(RestoreObjectRequest request) throws ClientException, ServiceException;

    /**
     * Asynchronously restores the object of archive storage. The function is not applicable to
     * Normal or IA storage. The restoreObject() needs to be called prior to
     * calling getObject() on an archive object.
     *
     * @param request
     *            A {@link RestoreObjectRequest} instance that specifies the bucket
     *            name and object key.
     * @param completedCallback
     *            A {@link OSSCompletedCallback<RestoreObjectRequest, RestoreObjectResult>} instance that specifies callback functions
     * @return A {@link OSSAsyncTask<RestoreObjectResult>} instance.
     */
    public OSSAsyncTask<RestoreObjectResult> asyncRestoreObject(RestoreObjectRequest request, OSSCompletedCallback<RestoreObjectRequest, RestoreObjectResult> completedCallback);

    /**
     * Asynchronously do a resumable download
     *
     * @param request
     *            A {@link ResumableDownloadRequest} instance that specifies the bucket
     *            name and object key.
     * @param completedCallback
     *            A {@link OSSCompletedCallback<ResumableDownloadRequest, ResumableDownloadResult>} instance that specifies callback functions
     * @return A {@link OSSAsyncTask<ResumableDownloadResult>} instance.
     */
    public OSSAsyncTask<ResumableDownloadResult> asyncResumableDownload(ResumableDownloadRequest request, OSSCompletedCallback<ResumableDownloadRequest, ResumableDownloadResult> completedCallback);

    /**
     * Synchronously do a resumable download
     *
     * @param request
     *            A {@link ResumableDownloadRequest} instance that specifies the bucket
     *            name and object key.
     */
    public ResumableDownloadResult syncResumableDownload(ResumableDownloadRequest request) throws ClientException, ServiceException;
}
