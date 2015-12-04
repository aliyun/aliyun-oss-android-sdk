package com.alibaba.sdk.android;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSTestConfig {

    public static final String ENDPOINT = "http://oss-cn-hangzhou.aliyuncs.com";

    public static final String ANDROID_TEST_BUCKET = "android-test";

    public static final String FOR_LISTOBJECT_BUCKET = "constant-listobject";

    public static final String FILE_DIR = "/storage/sdcard0/src_file/";

    public static final String TOKEN_URL = "http://30.9.173.145:8080/distribute-token.json";

    public static final String AK = "***********";

    public static final String SK = "****************";

    public static final OSSCredentialProvider credetialProvider = new OSSFederationCredentialProvider() {
        @Override
        public OSSFederationToken getFederationToken() {
            OSSLog.logD("[getFederationToken] - ");
            try {
                URL stsUrl = new URL(OSSTestConfig.TOKEN_URL);
                HttpURLConnection conn = (HttpURLConnection) stsUrl.openConnection();
                InputStream input = conn.getInputStream();
                String jsonText = IOUtils.readStreamAsString(input, OSSConstants.DEFAULT_CHARSET_NAME);
                JSONObject jsonObjs = new JSONObject(jsonText);
                String ak = jsonObjs.getString("accessKeyId");
                String sk = jsonObjs.getString("accessKeySecret");
                String token = jsonObjs.getString("securityToken");
                String expiration = jsonObjs.getString("expiration");
                return new OSSFederationToken(ak, sk, token, expiration);
            } catch (Exception e) {
                OSSLog.logE(e.toString());
                e.printStackTrace();
            }
            return null;
        }
    };

    public final static class TestDeleteCallback implements OSSCompletedCallback<DeleteObjectRequest, DeleteObjectResult> {

        public DeleteObjectRequest request;
        public DeleteObjectResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(DeleteObjectRequest request, DeleteObjectResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(DeleteObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestGetCallback implements OSSCompletedCallback<GetObjectRequest, GetObjectResult> {

        public GetObjectRequest request;
        public GetObjectResult result;
        public ClientException clientException;
        public ServiceException serviceException;


        @Override
        public void onSuccess(GetObjectRequest request, GetObjectResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestPutCallback implements OSSCompletedCallback<PutObjectRequest, PutObjectResult> {

        public PutObjectRequest request;
        public PutObjectResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(PutObjectRequest request, PutObjectResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestResumableUploadCallback implements OSSCompletedCallback<ResumableUploadRequest, ResumableUploadResult> {

        public ResumableUploadRequest request;
        public ResumableUploadResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(ResumableUploadRequest request, ResumableUploadResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(ResumableUploadRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestAppendCallback implements OSSCompletedCallback<AppendObjectRequest, AppendObjectResult> {

        public AppendObjectRequest request;
        public AppendObjectResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(AppendObjectRequest request, AppendObjectResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(AppendObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestListObjectsCallback implements OSSCompletedCallback<ListObjectsRequest, ListObjectsResult> {

        public ListObjectsRequest request;
        public ListObjectsResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(ListObjectsRequest request, ListObjectsResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(ListObjectsRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

}
