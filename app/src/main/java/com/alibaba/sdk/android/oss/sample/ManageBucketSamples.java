package com.alibaba.sdk.android.oss.sample;

import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.CannedAccessControlList;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;

/**
 * Created by LK on 15/12/19.
 */
public class ManageBucketSamples {
    private OSS oss;
    private String bucketName;
    private String uploadFilePath;

    public ManageBucketSamples(OSS oss, String bucketName, String uploadFilePath) {
        this.oss = oss;
        this.bucketName = bucketName;
    }

    /**
     * Creates a bucket and specifies the ACL permission and datacenter location
     */
    public void createBucketWithAclAndLocationContraint() {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
        createBucketRequest.setBucketACL(CannedAccessControlList.PublicRead);
        createBucketRequest.setLocationConstraint("oss-cn-hangzhou");
        OSSAsyncTask createTask = oss.asyncCreateBucket(createBucketRequest, new OSSCompletedCallback<CreateBucketRequest, CreateBucketResult>() {
            @Override
            public void onSuccess(CreateBucketRequest request, CreateBucketResult result) {
                Log.d("locationConstraint", request.getLocationConstraint());
                GetBucketACLRequest getBucketACLRequest = new GetBucketACLRequest(bucketName);
                OSSAsyncTask getBucketAclTask = oss.asyncGetBucketACL(getBucketACLRequest, new OSSCompletedCallback<GetBucketACLRequest, GetBucketACLResult>() {
                    @Override
                    public void onSuccess(GetBucketACLRequest request, GetBucketACLResult result) {
                        Log.d("BucketAcl", result.getBucketACL());
                    }

                    @Override
                    public void onFailure(GetBucketACLRequest request, ClientException clientException, ServiceException serviceException) {
                        // request exception
                        if (clientException != null) {
                            // client side exception
                            clientException.printStackTrace();
                        }
                        if (serviceException != null) {
                            // service side exception
                            Log.e("ErrorCode", serviceException.getErrorCode());
                            Log.e("RequestId", serviceException.getRequestId());
                            Log.e("HostId", serviceException.getHostId());
                            Log.e("RawMessage", serviceException.getRawMessage());
                        }
                    }
                });
            }

            @Override
            public void onFailure(CreateBucketRequest request, ClientException clientException, ServiceException serviceException) {
                // request exception
                if (clientException != null) {
                    // client side exception,  such as network exception
                    clientException.printStackTrace();
                }
                if (serviceException != null) {
                    // service side exception
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });
    }

    /**
     * 获取bucket ACL权限
     */
    public void getBucketAcl() {
        GetBucketACLRequest getBucketACLRequest = new GetBucketACLRequest(bucketName);
        OSSAsyncTask getBucketAclTask = oss.asyncGetBucketACL(getBucketACLRequest, new OSSCompletedCallback<GetBucketACLRequest, GetBucketACLResult>() {
            @Override
            public void onSuccess(GetBucketACLRequest request, GetBucketACLResult result) {
                Log.d("BucketAcl", result.getBucketACL());
                Log.d("Owner", result.getBucketOwner());
                Log.d("ID", result.getBucketOwnerID());
            }

            @Override
            public void onFailure(GetBucketACLRequest request, ClientException clientException, ServiceException serviceException) {
                // request exception
                if (clientException != null) {
                    // client side exception,  such as network exception
                    clientException.printStackTrace();
                }
                if (serviceException != null) {
                    // service side exception
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });
    }

    /**
     * Delete a non-empty bucket.
     * Create a bucket, and add files into it.
     * Try to delete the bucket and failure is expected.
     * Then delete file and then delete bucket
     */
    public void deleteNotEmptyBucket() {
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
        // 创建bucket
        try {
            oss.createBucket(createBucketRequest);
        } catch (ClientException clientException) {
            clientException.printStackTrace();
        } catch (ServiceException serviceException) {
            serviceException.printStackTrace();
        }

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, "test-file", uploadFilePath);
        try {
            oss.putObject(putObjectRequest);
        } catch (ClientException clientException) {
            clientException.printStackTrace();
        } catch (ServiceException serviceException) {
            serviceException.printStackTrace();
        }
        final DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(bucketName);
        OSSAsyncTask deleteBucketTask = oss.asyncDeleteBucket(deleteBucketRequest, new OSSCompletedCallback<DeleteBucketRequest, DeleteBucketResult>() {
            @Override
            public void onSuccess(DeleteBucketRequest request, DeleteBucketResult result) {
                Log.d("DeleteBucket", "Success!");
            }

            @Override
            public void onFailure(DeleteBucketRequest request, ClientException clientException, ServiceException serviceException) {
                // request exception
                if (clientException != null) {
                    // client side exception,  such as network exception
                    clientException.printStackTrace();
                }
                if (serviceException != null) {
                    // The bucket to delete is not empty.
                    if (serviceException.getStatusCode() == 409) {
                        // Delete files
                        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, "test-file");
                        try {
                            oss.deleteObject(deleteObjectRequest);
                        } catch (ClientException clientexception) {
                            clientexception.printStackTrace();
                        } catch (ServiceException serviceexception) {
                            serviceexception.printStackTrace();
                        }
                        // Delete bucket again
                        DeleteBucketRequest deleteBucketRequest1 = new DeleteBucketRequest(bucketName);
                        try {
                            oss.deleteBucket(deleteBucketRequest1);
                        } catch (ClientException clientexception) {
                            clientexception.printStackTrace();
                            return;
                        } catch (ServiceException serviceexception) {
                            serviceexception.printStackTrace();
                            return;
                        }
                        Log.d("DeleteBucket", "Success!");
                    }
                }
            }
        });
    }
}
