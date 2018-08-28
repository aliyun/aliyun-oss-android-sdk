package com.alibaba.sdk.android.oss.internal;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.HttpMethod;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.RequestParameters;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.common.utils.CRC64;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpHeaders;
import com.alibaba.sdk.android.oss.common.utils.HttpUtil;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.common.utils.VersionInfoUtils;
import com.alibaba.sdk.android.oss.exception.InconsistentException;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;
import com.alibaba.sdk.android.oss.model.CreateBucketRequest;
import com.alibaba.sdk.android.oss.model.CreateBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteMultipleObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteMultipleObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.GetBucketInfoRequest;
import com.alibaba.sdk.android.oss.model.GetBucketInfoResult;
import com.alibaba.sdk.android.oss.model.GetObjectACLRequest;
import com.alibaba.sdk.android.oss.model.GetObjectACLResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.GetSymlinkRequest;
import com.alibaba.sdk.android.oss.model.GetSymlinkResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.ImagePersistRequest;
import com.alibaba.sdk.android.oss.model.ImagePersistResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListBucketsRequest;
import com.alibaba.sdk.android.oss.model.ListBucketsResult;
import com.alibaba.sdk.android.oss.model.ListMultipartUploadsRequest;
import com.alibaba.sdk.android.oss.model.ListMultipartUploadsResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.PutSymlinkRequest;
import com.alibaba.sdk.android.oss.model.PutSymlinkResult;
import com.alibaba.sdk.android.oss.model.RestoreObjectRequest;
import com.alibaba.sdk.android.oss.model.RestoreObjectResult;
import com.alibaba.sdk.android.oss.model.TriggerCallbackRequest;
import com.alibaba.sdk.android.oss.model.TriggerCallbackResult;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;
import com.alibaba.sdk.android.oss.network.OSSRequestTask;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class InternalRequestOperation {

    private static final int LIST_PART_MAX_RETURNS = 1000;
    private static final int MAX_PART_NUMBER = 10000;
    private static ExecutorService executorService =
            Executors.newFixedThreadPool(OSSConstants.DEFAULT_BASE_THREAD_POOL_SIZE, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "oss-android-api-thread");
                }
            });
    private volatile URI endpoint;
    private URI service;
    private OkHttpClient innerClient;
    private Context applicationContext;
    private OSSCredentialProvider credentialProvider;
    private int maxRetryCount = OSSConstants.DEFAULT_RETRY_COUNT;
    private ClientConfiguration conf;

    public InternalRequestOperation(Context context, final URI endpoint, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
        this.applicationContext = context;
        this.endpoint = endpoint;
        this.credentialProvider = credentialProvider;
        this.conf = conf;

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .cache(null)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return HttpsURLConnection.getDefaultHostnameVerifier().verify(endpoint.getHost(), session);
                    }
                });

        if (conf != null) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(conf.getMaxConcurrentRequest());

            builder.connectTimeout(conf.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .dispatcher(dispatcher);

            if (conf.getProxyHost() != null && conf.getProxyPort() != 0) {
                builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(conf.getProxyHost(), conf.getProxyPort())));
            }

            this.maxRetryCount = conf.getMaxErrorRetry();
        }
        this.innerClient = builder.build();
    }

    public InternalRequestOperation(Context context, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
        try {
            service = new URI("http://oss.aliyuncs.com");
            endpoint = new URI("http://127.0.0.1"); //构造假的endpoint
        } catch (Exception e) {
            throw new IllegalArgumentException("Endpoint must be a string like 'http://oss-cn-****.aliyuncs.com'," +
                    "or your cname like 'http://image.cnamedomain.com'!");
        }
        this.applicationContext = context;
        this.credentialProvider = credentialProvider;
        this.conf = conf;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .retryOnConnectionFailure(false)
                .cache(null)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return HttpsURLConnection.getDefaultHostnameVerifier().verify(service.getHost(), session);
                    }
                });

        if (conf != null) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(conf.getMaxConcurrentRequest());

            builder.connectTimeout(conf.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(conf.getSocketTimeout(), TimeUnit.MILLISECONDS)
                    .dispatcher(dispatcher);

            if (conf.getProxyHost() != null && conf.getProxyPort() != 0) {
                builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(conf.getProxyHost(), conf.getProxyPort())));
            }

            this.maxRetryCount = conf.getMaxErrorRetry();
        }
        this.innerClient = builder.build();
    }

    public PutObjectResult syncPutObject(
            PutObjectRequest request) throws ClientException, ServiceException {
        PutObjectResult result = putObject(request, null).getResult();
        checkCRC64(request, result);
        return result;
    }

    public OSSAsyncTask<PutObjectResult> putObject(
            PutObjectRequest request, final OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());
        if (request.getUploadData() != null) {
            requestMessage.setUploadData(request.getUploadData());
        }
        if (request.getUploadFilePath() != null) {
            requestMessage.setUploadFilePath(request.getUploadFilePath());
        }
        if (request.getCallbackParam() != null) {
            requestMessage.getHeaders().put("x-oss-callback", OSSUtils.populateMapToBase64JsonString(request.getCallbackParam()));
        }
        if (request.getCallbackVars() != null) {
            requestMessage.getHeaders().put("x-oss-callback-var", OSSUtils.populateMapToBase64JsonString(request.getCallbackVars()));
        }

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<PutObjectRequest, PutObjectResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                @Override
                public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                    checkCRC64(request, result, completedCallback);
                }

                @Override
                public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                    completedCallback.onFailure(request, clientException, serviceException);
                }
            });
        }

        if (request.getRetryCallback() != null) {
            executionContext.setRetryCallback(request.getRetryCallback());
        }

        executionContext.setProgressCallback(request.getProgressCallback());
        ResponseParser<PutObjectResult> parser = new ResponseParsers.PutObjectResponseParser();

        Callable<PutObjectResult> callable = new OSSRequestTask<PutObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<CreateBucketResult> createBucket(
            CreateBucketRequest request, OSSCompletedCallback<CreateBucketRequest, CreateBucketResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(request.getBucketName());
        if (request.getBucketACL() != null) {
            requestMessage.getHeaders().put(OSSHeaders.OSS_CANNED_ACL, request.getBucketACL().toString());
        }
        try {
            Map<String, String> configures = new HashMap<String, String>();
            if (request.getLocationConstraint() != null) {
                configures.put(CreateBucketRequest.TAB_LOCATIONCONSTRAINT, request.getLocationConstraint());
            }
            configures.put(CreateBucketRequest.TAB_STORAGECLASS, request.getBucketStorageClass().toString());
            requestMessage.createBucketRequestBodyMarshall(configures);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<CreateBucketRequest, CreateBucketResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<CreateBucketResult> parser = new ResponseParsers.CreateBucketResponseParser();

        Callable<CreateBucketResult> callable = new OSSRequestTask<CreateBucketResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<DeleteBucketResult> deleteBucket(
            DeleteBucketRequest request, OSSCompletedCallback<DeleteBucketRequest, DeleteBucketResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.DELETE);
        requestMessage.setBucketName(request.getBucketName());
        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<DeleteBucketRequest, DeleteBucketResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<DeleteBucketResult> parser = new ResponseParsers.DeleteBucketResponseParser();
        Callable<DeleteBucketResult> callable = new OSSRequestTask<DeleteBucketResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<GetBucketInfoResult> getBucketInfo(
            GetBucketInfoRequest request, OSSCompletedCallback<GetBucketInfoRequest, GetBucketInfoResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("bucketInfo", "");

        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setParameters(query);
        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<GetBucketInfoRequest, GetBucketInfoResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<GetBucketInfoResult> parser = new ResponseParsers.GetBucketInfoResponseParser();
        Callable<GetBucketInfoResult> callable = new OSSRequestTask<GetBucketInfoResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<GetBucketACLResult> getBucketACL(
            GetBucketACLRequest request, OSSCompletedCallback<GetBucketACLRequest, GetBucketACLResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("acl", "");

        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setParameters(query);
        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<GetBucketACLRequest, GetBucketACLResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<GetBucketACLResult> parser = new ResponseParsers.GetBucketACLResponseParser();
        Callable<GetBucketACLResult> callable = new OSSRequestTask<GetBucketACLResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public AppendObjectResult syncAppendObject(
            AppendObjectRequest request) throws ClientException, ServiceException {
        AppendObjectResult result = appendObject(request, null).getResult();
        boolean checkCRC = request.getCRC64() == OSSRequest.CRC64Config.YES ? true : false;
        if (request.getInitCRC64() != null && checkCRC) {
            result.setClientCRC(CRC64.combine(request.getInitCRC64(), result.getClientCRC(),
                    (result.getNextPosition() - request.getPosition())));
        }
        checkCRC64(request, result);
        return result;
    }

    public OSSAsyncTask<AppendObjectResult> appendObject(
            AppendObjectRequest request, final OSSCompletedCallback<AppendObjectRequest, AppendObjectResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        if (request.getUploadData() != null) {
            requestMessage.setUploadData(request.getUploadData());
        }
        if (request.getUploadFilePath() != null) {
            requestMessage.setUploadFilePath(request.getUploadFilePath());
        }
        requestMessage.getParameters().put(RequestParameters.SUBRESOURCE_APPEND, "");
        requestMessage.getParameters().put(RequestParameters.POSITION, String.valueOf(request.getPosition()));

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<AppendObjectRequest, AppendObjectResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(new OSSCompletedCallback<AppendObjectRequest, AppendObjectResult>() {
                @Override
                public void onSuccess(AppendObjectRequest request, AppendObjectResult result) {
                    boolean checkCRC = request.getCRC64() == OSSRequest.CRC64Config.YES ? true : false;
                    if (request.getInitCRC64() != null && checkCRC) {
                        result.setClientCRC(CRC64.combine(request.getInitCRC64(), result.getClientCRC(),
                                (result.getNextPosition() - request.getPosition())));
                    }
                    checkCRC64(request, result, completedCallback);
                }

                @Override
                public void onFailure(AppendObjectRequest request, ClientException clientException, ServiceException serviceException) {
                    completedCallback.onFailure(request, clientException, serviceException);
                }
            });
        }
        executionContext.setProgressCallback(request.getProgressCallback());
        ResponseParser<AppendObjectResult> parser = new ResponseParsers.AppendObjectResponseParser();

        Callable<AppendObjectResult> callable = new OSSRequestTask<AppendObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<HeadObjectResult> headObject(
            HeadObjectRequest request, OSSCompletedCallback<HeadObjectRequest, HeadObjectResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.HEAD);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<HeadObjectRequest, HeadObjectResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<HeadObjectResult> parser = new ResponseParsers.HeadObjectResponseParser();

        Callable<HeadObjectResult> callable = new OSSRequestTask<HeadObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<GetObjectResult> getObject(
            GetObjectRequest request, OSSCompletedCallback<GetObjectRequest, GetObjectResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        if (request.getRange() != null) {
            requestMessage.getHeaders().put(OSSHeaders.RANGE, request.getRange().toString());
        }

        if (request.getxOssProcess() != null) {
            requestMessage.getParameters().put(RequestParameters.X_OSS_PROCESS, request.getxOssProcess());
        }

        canonicalizeRequestMessage(requestMessage, request);

        if (request.getRequestHeaders() != null) {
            for (Map.Entry<String, String> entry : request.getRequestHeaders().entrySet()) {
                requestMessage.getHeaders().put(entry.getKey(), entry.getValue());
            }
        }

        ExecutionContext<GetObjectRequest, GetObjectResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        executionContext.setProgressCallback(request.getProgressListener());
        ResponseParser<GetObjectResult> parser = new ResponseParsers.GetObjectResponseParser();

        Callable<GetObjectResult> callable = new OSSRequestTask<GetObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<GetObjectACLResult> getObjectACL(GetObjectACLRequest request, OSSCompletedCallback<GetObjectACLRequest, GetObjectACLResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("acl", "");

        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setParameters(query);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<GetObjectACLRequest, GetObjectACLResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<GetObjectACLResult> parser = new ResponseParsers.GetObjectACLResponseParser();

        Callable<GetObjectACLResult> callable = new OSSRequestTask<GetObjectACLResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<CopyObjectResult> copyObject(
            CopyObjectRequest request, OSSCompletedCallback<CopyObjectRequest, CopyObjectResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(request.getDestinationBucketName());
        requestMessage.setObjectKey(request.getDestinationKey());

        OSSUtils.populateCopyObjectHeaders(request, requestMessage.getHeaders());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<CopyObjectRequest, CopyObjectResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<CopyObjectResult> parser = new ResponseParsers.CopyObjectResponseParser();

        Callable<CopyObjectResult> callable = new OSSRequestTask<CopyObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<DeleteObjectResult> deleteObject(
            DeleteObjectRequest request, OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.DELETE);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<DeleteObjectRequest, DeleteObjectResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<DeleteObjectResult> parser = new ResponseParsers.DeleteObjectResponseParser();

        Callable<DeleteObjectResult> callable = new OSSRequestTask<DeleteObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<DeleteMultipleObjectResult> deleteMultipleObject(
            DeleteMultipleObjectRequest request, OSSCompletedCallback<DeleteMultipleObjectRequest, DeleteMultipleObjectResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put("delete", "");

        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setParameters(query);
        try {
            byte[] bodyBytes = requestMessage.deleteMultipleObjectRequestBodyMarshall(request.getObjectKeys(), request.getQuiet());
            if (bodyBytes != null && bodyBytes.length > 0) {
                requestMessage.getHeaders().put(OSSHeaders.CONTENT_MD5, BinaryUtil.calculateBase64Md5(bodyBytes));
                requestMessage.getHeaders().put(OSSHeaders.CONTENT_LENGTH, String.valueOf(bodyBytes.length));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<DeleteMultipleObjectRequest, DeleteMultipleObjectResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<DeleteMultipleObjectResult> parser = new ResponseParsers.DeleteMultipleObjectResponseParser();

        Callable<DeleteMultipleObjectResult> callable = new OSSRequestTask<DeleteMultipleObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);


    }

    public OSSAsyncTask<ListBucketsResult> listBuckets(
            ListBucketsRequest request, OSSCompletedCallback<ListBucketsRequest, ListBucketsResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setService(service);
        requestMessage.setEndpoint(endpoint); //设置假Endpoint

        canonicalizeRequestMessage(requestMessage, request);

        OSSUtils.populateListBucketRequestParameters(request, requestMessage.getParameters());
        ExecutionContext<ListBucketsRequest, ListBucketsResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ListBucketsResult> parser = new ResponseParsers.ListBucketResponseParser();
        Callable<ListBucketsResult> callable = new OSSRequestTask<ListBucketsResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<ListObjectsResult> listObjects(
            ListObjectsRequest request, OSSCompletedCallback<ListObjectsRequest, ListObjectsResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setBucketName(request.getBucketName());

        canonicalizeRequestMessage(requestMessage, request);

        OSSUtils.populateListObjectsRequestParameters(request, requestMessage.getParameters());

        ExecutionContext<ListObjectsRequest, ListObjectsResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ListObjectsResult> parser = new ResponseParsers.ListObjectsResponseParser();

        Callable<ListObjectsResult> callable = new OSSRequestTask<ListObjectsResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<InitiateMultipartUploadResult> initMultipartUpload(
            InitiateMultipartUploadRequest request, OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        requestMessage.getParameters().put(RequestParameters.SUBRESOURCE_UPLOADS, "");
        if (request.isSequential) {
            requestMessage.getParameters().put(RequestParameters.SUBRESOURCE_SEQUENTIAL, "");
        }

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<InitiateMultipartUploadResult> parser = new ResponseParsers.InitMultipartResponseParser();

        Callable<InitiateMultipartUploadResult> callable = new OSSRequestTask<InitiateMultipartUploadResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public UploadPartResult syncUploadPart(
            UploadPartRequest request) throws ClientException, ServiceException {
        UploadPartResult result = uploadPart(request, null).getResult();
        checkCRC64(request, result);
        return result;
    }

    public OSSAsyncTask<UploadPartResult> uploadPart(
            UploadPartRequest request, final OSSCompletedCallback<UploadPartRequest, UploadPartResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        requestMessage.getParameters().put(RequestParameters.UPLOAD_ID, request.getUploadId());
        requestMessage.getParameters().put(RequestParameters.PART_NUMBER, String.valueOf(request.getPartNumber()));
        requestMessage.setUploadData(request.getPartContent());
        if (request.getMd5Digest() != null) {
            requestMessage.getHeaders().put(OSSHeaders.CONTENT_MD5, request.getMd5Digest());
        }

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<UploadPartRequest, UploadPartResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(new OSSCompletedCallback<UploadPartRequest, UploadPartResult>() {
                @Override
                public void onSuccess(UploadPartRequest request, UploadPartResult result) {
                    checkCRC64(request, result, completedCallback);
                }

                @Override
                public void onFailure(UploadPartRequest request, ClientException clientException, ServiceException serviceException) {
                    completedCallback.onFailure(request, clientException, serviceException);
                }
            });
        }
        executionContext.setProgressCallback(request.getProgressCallback());
        ResponseParser<UploadPartResult> parser = new ResponseParsers.UploadPartResponseParser();

        Callable<UploadPartResult> callable = new OSSRequestTask<UploadPartResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public CompleteMultipartUploadResult syncCompleteMultipartUpload(
            CompleteMultipartUploadRequest request) throws ClientException, ServiceException {
        CompleteMultipartUploadResult result = completeMultipartUpload(request, null).getResult();
        if (result.getServerCRC() != null) {
            long crc64 = calcObjectCRCFromParts(request.getPartETags());
            result.setClientCRC(crc64);
        }
        checkCRC64(request, result);
        return result;
    }

    public OSSAsyncTask<CompleteMultipartUploadResult> completeMultipartUpload(
            CompleteMultipartUploadRequest request, final OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());
        requestMessage.setStringBody(OSSUtils.buildXMLFromPartEtagList(request.getPartETags()));

        requestMessage.getParameters().put(RequestParameters.UPLOAD_ID, request.getUploadId());

        if (request.getCallbackParam() != null) {
            requestMessage.getHeaders().put("x-oss-callback", OSSUtils.populateMapToBase64JsonString(request.getCallbackParam()));
        }
        if (request.getCallbackVars() != null) {
            requestMessage.getHeaders().put("x-oss-callback-var", OSSUtils.populateMapToBase64JsonString(request.getCallbackVars()));
        }

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(new OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult>() {
                @Override
                public void onSuccess(CompleteMultipartUploadRequest request, CompleteMultipartUploadResult result) {
                    if (result.getServerCRC() != null) {
                        long crc64 = calcObjectCRCFromParts(request.getPartETags());
                        result.setClientCRC(crc64);
                    }
                    checkCRC64(request, result, completedCallback);
                }

                @Override
                public void onFailure(CompleteMultipartUploadRequest request, ClientException clientException, ServiceException serviceException) {
                    completedCallback.onFailure(request, clientException, serviceException);
                }
            });
        }
        ResponseParser<CompleteMultipartUploadResult> parser = new ResponseParsers.CompleteMultipartUploadResponseParser();

        Callable<CompleteMultipartUploadResult> callable = new OSSRequestTask<CompleteMultipartUploadResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<AbortMultipartUploadResult> abortMultipartUpload(
            AbortMultipartUploadRequest request, OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.DELETE);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        requestMessage.getParameters().put(RequestParameters.UPLOAD_ID, request.getUploadId());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<AbortMultipartUploadRequest, AbortMultipartUploadResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<AbortMultipartUploadResult> parser = new ResponseParsers.AbortMultipartUploadResponseParser();

        Callable<AbortMultipartUploadResult> callable = new OSSRequestTask<AbortMultipartUploadResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<ListPartsResult> listParts(
            ListPartsRequest request, OSSCompletedCallback<ListPartsRequest, ListPartsResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        requestMessage.getParameters().put(RequestParameters.UPLOAD_ID, request.getUploadId());

        Integer maxParts = request.getMaxParts();
        if (maxParts != null) {
            if (!OSSUtils.checkParamRange(maxParts, 0, true, LIST_PART_MAX_RETURNS, true)) {
                throw new IllegalArgumentException("MaxPartsOutOfRange: " + LIST_PART_MAX_RETURNS);
            }
            requestMessage.getParameters().put(RequestParameters.MAX_PARTS, maxParts.toString());
        }

        Integer partNumberMarker = request.getPartNumberMarker();
        if (partNumberMarker != null) {
            if (!OSSUtils.checkParamRange(partNumberMarker, 0, false, MAX_PART_NUMBER, true)) {
                throw new IllegalArgumentException("PartNumberMarkerOutOfRange: " + MAX_PART_NUMBER);
            }
            requestMessage.getParameters().put(RequestParameters.PART_NUMBER_MARKER, partNumberMarker.toString());
        }

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<ListPartsRequest, ListPartsResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ListPartsResult> parser = new ResponseParsers.ListPartsResponseParser();

        Callable<ListPartsResult> callable = new OSSRequestTask<ListPartsResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<ListMultipartUploadsResult> listMultipartUploads(
            ListMultipartUploadsRequest request, OSSCompletedCallback<ListMultipartUploadsRequest, ListMultipartUploadsResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setBucketName(request.getBucketName());

        requestMessage.getParameters().put(RequestParameters.SUBRESOURCE_UPLOADS, "");

        OSSUtils.populateListMultipartUploadsRequestParameters(request, requestMessage.getParameters());

        canonicalizeRequestMessage(requestMessage, request);

        ExecutionContext<ListMultipartUploadsRequest, ListMultipartUploadsResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ListMultipartUploadsResult> parser = new ResponseParsers.ListMultipartUploadsResponseParser();

        Callable<ListMultipartUploadsResult> callable = new OSSRequestTask<ListMultipartUploadsResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    private boolean checkIfHttpDnsAvailable(boolean httpDnsEnable) {
        if (httpDnsEnable) {
            if (applicationContext == null) {
                return false;
            }

            boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

            String proxyHost;

            if (IS_ICS_OR_LATER) {
                proxyHost = System.getProperty("http.proxyHost");
            } else {
                proxyHost = android.net.Proxy.getHost(applicationContext);
            }

            String confProxyHost = conf.getProxyHost();
            if (!TextUtils.isEmpty(confProxyHost)) {
                proxyHost = confProxyHost;
            }

            return TextUtils.isEmpty(proxyHost);
        }
        return false;
    }

    public OkHttpClient getInnerClient() {
        return innerClient;
    }

    private void canonicalizeRequestMessage(RequestMessage message, OSSRequest request) {
        Map<String, String> header = message.getHeaders();

        if (header.get(OSSHeaders.DATE) == null) {
            header.put(OSSHeaders.DATE, DateUtil.currentFixedSkewedTimeInRFC822Format());
        }

        if (message.getMethod() == HttpMethod.POST || message.getMethod() == HttpMethod.PUT) {
            if (OSSUtils.isEmptyString(header.get(OSSHeaders.CONTENT_TYPE))) {
                String determineContentType = OSSUtils.determineContentType(null,
                        message.getUploadFilePath(), message.getObjectKey());
                header.put(OSSHeaders.CONTENT_TYPE, determineContentType);
            }
        }

        // When the HTTP proxy is set, httpDNS is not enabled.
        message.setHttpDnsEnable(checkIfHttpDnsAvailable(conf.isHttpDnsEnable()));
        message.setCredentialProvider(credentialProvider);

        message.getHeaders().put(HttpHeaders.USER_AGENT, VersionInfoUtils.getUserAgent(conf.getCustomUserMark()));

        if (message.getHeaders().containsKey(OSSHeaders.RANGE) || message.getParameters().containsKey(RequestParameters.X_OSS_PROCESS)) {
            //if contain range or x-oss-process , then don't crc64
            message.setCheckCRC64(false);
        }

        // Private cloud user could have special endpoint and we need to differentiate it with the CName here.
        message.setIsInCustomCnameExcludeList(OSSUtils.isInCustomCnameExcludeList(this.endpoint.getHost(), this.conf.getCustomCnameExcludeList()));

        boolean checkCRC64 = request.getCRC64() != OSSRequest.CRC64Config.NULL
                ? (request.getCRC64() == OSSRequest.CRC64Config.YES ? true : false) : conf.isCheckCRC64();
        message.setCheckCRC64(checkCRC64);
        request.setCRC64(checkCRC64 ? OSSRequest.CRC64Config.YES : OSSRequest.CRC64Config.NO);
    }

    public void setCredentialProvider(OSSCredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    private <Request extends OSSRequest, Result extends OSSResult> void checkCRC64(Request request
            , Result result) throws ClientException {
        if (request.getCRC64() == OSSRequest.CRC64Config.YES ? true : false) {
            try {
                OSSUtils.checkChecksum(result.getClientCRC(), result.getServerCRC(), result.getRequestId());
            } catch (InconsistentException e) {
                throw new ClientException(e.getMessage(), e);
            }
        }
    }

    private <Request extends OSSRequest, Result extends OSSResult> void checkCRC64(Request request
            , Result result, OSSCompletedCallback<Request, Result> completedCallback) {
        try {
            checkCRC64(request, result);
            if (completedCallback != null) {
                completedCallback.onSuccess(request, result);
            }
        } catch (ClientException e) {
            if (completedCallback != null) {
                completedCallback.onFailure(request, e, null);
            }
        }
    }

    private long calcObjectCRCFromParts(List<PartETag> partETags) {
        long crc = 0;
        for (PartETag partETag : partETags) {
            if (partETag.getCRC64() == 0 || partETag.getPartSize() <= 0) {
                return 0;
            }
            crc = CRC64.combine(crc, partETag.getCRC64(), partETag.getPartSize());
        }
        return crc;
    }

    public Context getApplicationContext() {
        return applicationContext;
    }

    public ClientConfiguration getConf() {
        return conf;
    }

    public OSSAsyncTask<TriggerCallbackResult> triggerCallback(TriggerCallbackRequest request, OSSCompletedCallback<TriggerCallbackRequest, TriggerCallbackResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put(RequestParameters.X_OSS_PROCESS, "");

        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());
        requestMessage.setParameters(query);

        String bodyString = OSSUtils.buildTriggerCallbackBody(request.getCallbackParam(), request.getCallbackVars());
        requestMessage.setStringBody(bodyString);

        String md5String = BinaryUtil.calculateBase64Md5(bodyString.getBytes());
        requestMessage.getHeaders().put(HttpHeaders.CONTENT_MD5, md5String);

        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<TriggerCallbackRequest, TriggerCallbackResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<TriggerCallbackResult> parser = new ResponseParsers.TriggerCallbackResponseParser();
        Callable<TriggerCallbackResult> callable = new OSSRequestTask<TriggerCallbackResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public TriggerCallbackResult asyncTriggerCallback(TriggerCallbackRequest request) throws ClientException, ServiceException {
        return triggerCallback(request, null).getResult();
    }

    public OSSAsyncTask<ImagePersistResult> imageActionPersist(ImagePersistRequest request, OSSCompletedCallback<ImagePersistRequest, ImagePersistResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put(RequestParameters.X_OSS_PROCESS, "");

        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setBucketName(request.mFromBucket);
        requestMessage.setObjectKey(request.mFromObjectkey);
        requestMessage.setParameters(query);

        String bodyString = OSSUtils.buildImagePersistentBody(request.mToBucketName, request.mToObjectKey, request.mAction);
        requestMessage.setStringBody(bodyString);

        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<ImagePersistRequest, ImagePersistResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ImagePersistResult> parser = new ResponseParsers.ImagePersistResponseParser();
        Callable<ImagePersistResult> callable = new OSSRequestTask<ImagePersistResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public PutSymlinkResult syncPutSymlink(PutSymlinkRequest request) throws ClientException, ServiceException {
        return putSymlink(request, null).getResult();
    }

    ;

    public OSSAsyncTask<PutSymlinkResult> putSymlink(PutSymlinkRequest request, OSSCompletedCallback<PutSymlinkRequest, PutSymlinkResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put(RequestParameters.X_OSS_SYMLINK, "");

        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());
        requestMessage.setParameters(query);

        if (!OSSUtils.isEmptyString(request.getTargetObjectName())) {
            String targetObjectName = HttpUtil.urlEncode(request.getTargetObjectName(), OSSConstants.DEFAULT_CHARSET_NAME);
            requestMessage.getHeaders().put(OSSHeaders.OSS_HEADER_SYMLINK_TARGET, targetObjectName);
        }

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<PutSymlinkRequest, PutSymlinkResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<PutSymlinkResult> parser = new ResponseParsers.PutSymlinkResponseParser();
        Callable<PutSymlinkResult> callable = new OSSRequestTask<PutSymlinkResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public GetSymlinkResult syncGetSymlink(GetSymlinkRequest request) throws ClientException, ServiceException {
        return getSymlink(request, null).getResult();
    }

    public OSSAsyncTask<GetSymlinkResult> getSymlink(GetSymlinkRequest request, OSSCompletedCallback<GetSymlinkRequest, GetSymlinkResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put(RequestParameters.X_OSS_SYMLINK, "");

        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());
        requestMessage.setParameters(query);

        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<GetSymlinkRequest, GetSymlinkResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<GetSymlinkResult> parser = new ResponseParsers.GetSymlinkResponseParser();
        Callable<GetSymlinkResult> callable = new OSSRequestTask<GetSymlinkResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public RestoreObjectResult syncRestoreObject(RestoreObjectRequest request) throws ClientException, ServiceException {
        return restoreObject(request, null).getResult();
    }

    public OSSAsyncTask<RestoreObjectResult> restoreObject(RestoreObjectRequest request, OSSCompletedCallback<RestoreObjectRequest, RestoreObjectResult> completedCallback) {
        RequestMessage requestMessage = new RequestMessage();
        Map<String, String> query = new LinkedHashMap<String, String>();
        query.put(RequestParameters.X_OSS_RESTORE, "");

        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());
        requestMessage.setParameters(query);

        canonicalizeRequestMessage(requestMessage, request);
        ExecutionContext<RestoreObjectRequest, RestoreObjectResult> executionContext = new ExecutionContext(getInnerClient(), request, applicationContext);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<RestoreObjectResult> parser = new ResponseParsers.RestoreObjectResponseParser();
        Callable<RestoreObjectResult> callable = new OSSRequestTask<RestoreObjectResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }
}
