package com.alibaba.sdk.android.oss.internal;

import android.content.Context;
import android.os.Build;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.HttpMethod;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSHeaders;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.DateUtil;
import com.alibaba.sdk.android.oss.common.utils.HttpHeaders;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.common.utils.VersionInfoUtils;
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
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;
import com.alibaba.sdk.android.oss.network.OSSRequestTask;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class InternalRequestOperation {

    private volatile URI endpoint;
    private OkHttpClient innerClient;
    private Context applicationContext;
    private OSSCredentialProvider credentialProvider;
    private int maxRetryCount = OSSConstants.DEFAULT_RETRY_COUNT;

    private static ExecutorService executorService = Executors.newFixedThreadPool(OSSConstants.DEFAULT_BASE_THREAD_POOL_SIZE);

    public InternalRequestOperation(Context context, final URI endpoint, OSSCredentialProvider credentialProvider, ClientConfiguration conf) {
        this.applicationContext = context;
        this.endpoint = endpoint;
        this.credentialProvider = credentialProvider;

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

            this.maxRetryCount = conf.getMaxErrorRetry();

        }

        this.innerClient = builder.build();
    }

    public OSSAsyncTask<PutObjectResult> putObject(
            PutObjectRequest request, OSSCompletedCallback<PutObjectRequest, PutObjectResult> completedCallback) {

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

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<PutObjectRequest> executionContext = new ExecutionContext<PutObjectRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        executionContext.setProgressCallback(request.getProgressCallback());
        ResponseParser<PutObjectResult> parser = new ResponseParsers.PutObjectReponseParser();

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
            requestMessage.createBucketRequestBodyMarshall(request.getLocationConstraint());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        canonicalizeRequestMessage(requestMessage);
        ExecutionContext<CreateBucketRequest> executionContext = new ExecutionContext<CreateBucketRequest>(getInnerClient(), request);
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
        canonicalizeRequestMessage(requestMessage);
        ExecutionContext<DeleteBucketRequest> executionContext = new ExecutionContext<DeleteBucketRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<DeleteBucketResult> parser = new ResponseParsers.DeleteBucketResponseParser();
        Callable<DeleteBucketResult> callable = new OSSRequestTask<DeleteBucketResult>(requestMessage, parser, executionContext, maxRetryCount);
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
        canonicalizeRequestMessage(requestMessage);
        ExecutionContext<GetBucketACLRequest> executionContext = new ExecutionContext<GetBucketACLRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<GetBucketACLResult> parser = new ResponseParsers.GetBucketACLResponseParser();
        Callable<GetBucketACLResult> callable = new OSSRequestTask<GetBucketACLResult>(requestMessage, parser, executionContext, maxRetryCount);
        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<AppendObjectResult> appendObject(
            AppendObjectRequest request, OSSCompletedCallback<AppendObjectRequest, AppendObjectResult> completedCallback) {

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
        requestMessage.getParameters().put("append", "");
        requestMessage.getParameters().put("position", String.valueOf(request.getPosition()));

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<AppendObjectRequest> executionContext = new ExecutionContext<AppendObjectRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
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

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<HeadObjectRequest> executionContext = new ExecutionContext<HeadObjectRequest>(getInnerClient(), request);
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

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<GetObjectRequest> executionContext = new ExecutionContext<GetObjectRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<GetObjectResult> parser = new ResponseParsers.GetObjectResponseParser();

        Callable<GetObjectResult> callable = new OSSRequestTask<GetObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

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

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<CopyObjectRequest> executionContext = new ExecutionContext<CopyObjectRequest>(getInnerClient(), request);
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

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<DeleteObjectRequest> executionContext = new ExecutionContext<DeleteObjectRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<DeleteObjectResult> parser = new ResponseParsers.DeleteObjectResponseParser();

        Callable<DeleteObjectResult> callable = new OSSRequestTask<DeleteObjectResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<ListObjectsResult> listObjects(
            ListObjectsRequest request, OSSCompletedCallback<ListObjectsRequest, ListObjectsResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.GET);
        requestMessage.setBucketName(request.getBucketName());

        canonicalizeRequestMessage(requestMessage);

        OSSUtils.populateListObjectsRequestParameters(request, requestMessage.getParameters());

        ExecutionContext<ListObjectsRequest> executionContext = new ExecutionContext<ListObjectsRequest>(getInnerClient(), request);
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
        requestMessage.getParameters().put("uploads", "");

        OSSUtils.populateRequestMetadata(requestMessage.getHeaders(), request.getMetadata());

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<InitiateMultipartUploadRequest> executionContext = new ExecutionContext<InitiateMultipartUploadRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<InitiateMultipartUploadResult> parser = new ResponseParsers.InitMultipartResponseParser();

        Callable<InitiateMultipartUploadResult> callable = new OSSRequestTask<InitiateMultipartUploadResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<UploadPartResult> uploadPart(
            UploadPartRequest request, OSSCompletedCallback<UploadPartRequest, UploadPartResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.PUT);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());

        requestMessage.getParameters().put("uploadId", request.getUploadId());
        requestMessage.getParameters().put("partNumber", String.valueOf(request.getPartNumber()));
        requestMessage.setUploadData(request.getPartContent());

        if (request.getMd5Digest() != null) {
            requestMessage.getHeaders().put(OSSHeaders.CONTENT_MD5, request.getMd5Digest());
        }

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<UploadPartRequest> executionContext = new ExecutionContext<UploadPartRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        executionContext.setProgressCallback(request.getProgressCallback());
        ResponseParser<UploadPartResult> parser = new ResponseParsers.UploadPartResponseParser();

        Callable<UploadPartResult> callable = new OSSRequestTask<UploadPartResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    public OSSAsyncTask<CompleteMultipartUploadResult> completeMultipartUpload(
            CompleteMultipartUploadRequest request, OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> completedCallback) {

        RequestMessage requestMessage = new RequestMessage();
        requestMessage.setIsAuthorizationRequired(request.isAuthorizationRequired());
        requestMessage.setEndpoint(endpoint);
        requestMessage.setMethod(HttpMethod.POST);
        requestMessage.setBucketName(request.getBucketName());
        requestMessage.setObjectKey(request.getObjectKey());
        requestMessage.setUploadData(OSSUtils.buildXMLFromPartEtagList(request.getPartETags()).getBytes());

        requestMessage.getParameters().put("uploadId", request.getUploadId());
        if (request.getCallbackParam() != null) {
            requestMessage.getHeaders().put("x-oss-callback", OSSUtils.populateMapToBase64JsonString(request.getCallbackParam()));
        }
        if (request.getCallbackVars() != null) {
            requestMessage.getHeaders().put("x-oss-callback-var", OSSUtils.populateMapToBase64JsonString(request.getCallbackVars()));
        }

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<CompleteMultipartUploadRequest> executionContext = new ExecutionContext<CompleteMultipartUploadRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
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

        requestMessage.getParameters().put("uploadId", request.getUploadId());

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<AbortMultipartUploadRequest> executionContext = new ExecutionContext<AbortMultipartUploadRequest>(getInnerClient(), request);
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

        requestMessage.getParameters().put("uploadId", request.getUploadId());

        canonicalizeRequestMessage(requestMessage);

        ExecutionContext<ListPartsRequest> executionContext = new ExecutionContext<ListPartsRequest>(getInnerClient(), request);
        if (completedCallback != null) {
            executionContext.setCompletedCallback(completedCallback);
        }
        ResponseParser<ListPartsResult> parser = new ResponseParsers.ListPartsResponseParser();

        Callable<ListPartsResult> callable = new OSSRequestTask<ListPartsResult>(requestMessage, parser, executionContext, maxRetryCount);

        return OSSAsyncTask.wrapRequestTask(executorService.submit(callable), executionContext);
    }

    private boolean checkIfHttpdnsAwailable() {
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
        return proxyHost == null;
    }

    public OkHttpClient getInnerClient() {
        return innerClient;
    }

    private void canonicalizeRequestMessage(RequestMessage message) {
        Map<String, String> header = message.getHeaders();

        if (header.get(OSSHeaders.DATE) == null) {
            header.put(OSSHeaders.DATE, DateUtil.currentFixedSkewedTimeInRFC822Format());
        }

        if (message.getMethod() == HttpMethod.POST || message.getMethod() == HttpMethod.PUT) {
            if (header.get(OSSHeaders.CONTENT_TYPE) == null) {
                String determineContentType = OSSUtils.determineContentType(null,
                        message.getUploadFilePath(), message.getObjectKey());
                header.put(OSSHeaders.CONTENT_TYPE, determineContentType);
            }
        }

        message.setIsHttpdnsEnable(checkIfHttpdnsAwailable());
        message.setCredentialProvider(credentialProvider);

        message.getHeaders().put(HttpHeaders.USER_AGENT, VersionInfoUtils.getUserAgent());
    }

    public void setCredentialProvider(OSSCredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }
}
