package com.alibaba.sdk.android;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
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
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetBucketACLRequest;
import com.alibaba.sdk.android.oss.model.GetBucketACLResult;
import com.alibaba.sdk.android.oss.model.GetBucketInfoRequest;
import com.alibaba.sdk.android.oss.model.GetBucketInfoResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.GetSymlinkRequest;
import com.alibaba.sdk.android.oss.model.GetSymlinkResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.MultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.PutSymlinkRequest;
import com.alibaba.sdk.android.oss.model.PutSymlinkResult;
import com.alibaba.sdk.android.oss.model.RestoreObjectRequest;
import com.alibaba.sdk.android.oss.model.RestoreObjectResult;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhouzhuo on 11/24/15.
 */
public class OSSTestConfig {

    public static final String ENDPOINT = "https://oss-cn-hangzhou.aliyuncs.com";

    public static final String EXCLUDE_HOST = "oss-cn-hangzhou.aliyuncs.com";

    public static final String EXCLUDE_HOST_WITH_HTTP = "http://oss-cn-hangzhou.aliyuncs.com";

    public static final String CALLBACK_SERVER = "oss-demo.aliyuncs.com:23450";

    public static final String ANDROID_TEST_CNAME = "http://www.cnametest.com";

    public static final String INTERNAL_FILE_DIR = InstrumentationRegistry.getContext().getFilesDir().getAbsolutePath() + "/oss/";

    public static final String EXTERNAL_FILE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/oss/";

    public static final String FILE_DIR = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? EXTERNAL_FILE_DIR : INTERNAL_FILE_DIR;

    public static final String MEDIA_FILE_DIR = "Documents/oss/";

    public static final String ERROR_TOKEN_URL = "http://0.0.0.0:3000";

    public static final String TOKEN_URL = "http://0.0.0.0:0000/sts/getsts";//set your sts server address

    public static final String AK = "*********************";//set your ak

    public static final String SK = "*********************";//set your sk

    public static OSSCredentialProvider credentialProvider;
    public static OSSCredentialProvider stsCredentialProvider;
    public static OSSCredentialProvider authCredentialProvider;
    public static OSSCredentialProvider fadercredentialProvider;
    public static OSSCredentialProvider fadercredentialProviderWrong;
    public static OSSCredentialProvider plainTextAKSKcredentialProvider = newPlainTextAKSKCredentialProvider();
    private static OSSTestConfig sInstance;

    private static Context mContext;

    private OSSTestConfig(Context context) {
        credentialProvider = plainTextAKSKcredentialProvider;
        stsCredentialProvider = newStsTokenCredentialProvider(context);
        fadercredentialProvider = newFederationCredentialProvider(context);
        fadercredentialProviderWrong = newFederationCredentialProviderWrongExpiration(context);
        authCredentialProvider = new OSSAuthCredentialsProvider(TOKEN_URL);
        mContext = context;
    }

    public static OSSTestConfig instance(Context context) {
        if (sInstance == null) {
            sInstance = new OSSTestConfig(context.getApplicationContext());
        }
        return sInstance;
    }

    public static OSSCredentialProvider newCustomSignerCredentialProvider() {
        return new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                return OSSUtils.sign(AK, SK, content);
            }
        };
    }

    public static OSSCredentialProvider newStsTokenCredentialProvider(Context context) {
        try {
            OSSFederationToken ossFederationToken = getOssFederationToken();
            OSSStsTokenCredentialProvider ossStsTokenCredentialProvider = new OSSStsTokenCredentialProvider(ossFederationToken);
            OSSLog.logDebug("[ak] " + ossStsTokenCredentialProvider.getAccessKeyId(), false);
            OSSLog.logDebug("[sk] " + ossStsTokenCredentialProvider.getSecretKeyId(), false);
            OSSLog.logDebug("[token] " + ossStsTokenCredentialProvider.getSecurityToken(), false);
            return ossStsTokenCredentialProvider;
        } catch (Exception e) {
            OSSLog.logError(e.toString());
            e.printStackTrace();
            return new OSSStsTokenCredentialProvider("", "", "");
        }
    }

    public static OSSCredentialProvider newFederationCredentialProvider(final Context context) {
        return new OSSFederationCredentialProvider() {
            @Override
            public OSSFederationToken getFederationToken() {
                OSSLog.logError("[getFederationToken] -------------------- ");
                try {
                    return getOssFederationToken();
                } catch (Exception e) {
                    OSSLog.logError(e.toString());
                    e.printStackTrace();
                }
                return null;
            }
        };
    }


    public static OSSCredentialProvider newFederationCredentialProviderWrongExpiration(final Context context) {
        return new OSSFederationCredentialProvider() {
            @Override
            public OSSFederationToken getFederationToken() {
                OSSLog.logError("[getFederationToken] -------------------- ");
                try {
                    return getOssFederationToken();
                } catch (Exception e) {
                    OSSLog.logError(e.toString());
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private static OSSFederationToken getOssFederationToken() throws IOException, JSONException {
        URL stsUrl = new URL(OSSTestConfig.TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) stsUrl.openConnection();
        InputStream input = conn.getInputStream();
        String jsonText = IOUtils.readStreamAsString(input, OSSConstants.DEFAULT_CHARSET_NAME);
        JSONObject credentials = new JSONObject(jsonText);
        String ak = credentials.optString("AccessKeyId");
        String sk = credentials.optString("AccessKeySecret");
        String token = credentials.optString("SecurityToken");
        String expiration = credentials.optString("Expiration");
        return new OSSFederationToken(ak, sk, token, expiration);
    }

    public static OSSCredentialProvider newPlainTextAKSKCredentialProvider() {
        OSSPlainTextAKSKCredentialProvider provider = new OSSPlainTextAKSKCredentialProvider(AK, SK);
        OSSLog.logDebug("[ak] " + provider.getAccessKeyId(), false);
        OSSLog.logDebug("[sk] " + provider.getAccessKeySecret(), false);
        return provider;
    }

    public static void initLocalFile() {
        String[] fileNames = {"file1k", "file10k", "file100k", "file1m", "file10m"};
        int[] fileSize = {1024, 10240, 102400, 1024000, 10240000};

        for (int i = 0; i < fileNames.length; i++) {
            try {
                initLocalFileByFile(fileNames[i], fileSize[i]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    initLocalFileByUri(fileNames[i], fileSize[i]);
                }
                OSSLog.logDebug("OSSTEST", "file write" + fileNames[i] + " ok");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void initLocalFileByFile(String fileName, long fileSize) throws Exception {
        String filePath = OSSTestConfig.FILE_DIR + fileName;
        OSSLog.logDebug("OSSTEST", "filePath : " + filePath);
        File path = new File(OSSTestConfig.FILE_DIR);
        File file = new File(filePath);
        if (!path.exists()) {
            OSSLog.logDebug("OSSTEST", "Create the path:" + path.getAbsolutePath());
            path.mkdir();
        }
        if (!file.exists()) {
            file.createNewFile();
            OSSLog.logDebug("OSSTEST", "create : " + file.getAbsolutePath());
        } else {
            return;
        }
        OSSLog.logDebug("OSSTEST", "write file : " + filePath);
        InputStream in = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file);
        long index = 0;
        int buf_size = 1024;
        long part = fileSize / buf_size;
        while (index < part) {
            byte[] buf = new byte[1024];
            fos.write(buf);
            index++;
        }
        in.close();
        fos.close();
    }

    public static void initLocalFileByUri(String fileName, long fileSize) throws Exception {
        Uri uri = queryUri(fileName);
        ParcelFileDescriptor parcelFileDescriptor = mContext.getContentResolver().openFileDescriptor(uri, "w");
        FileOutputStream fos = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
        long index = 0;
        int buf_size = 1024;
        long part = fileSize / buf_size;
        while (index < part) {
            byte[] buf = new byte[1024];
            fos.write(buf);
            index++;
        }
        fos.close();
    }

    public static void initDemoFile(String demoFile) {
        String resumbleFile = demoFile;
        try {
            initLocalFileByFile(resumbleFile, 500 * 1024);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                initLocalFileByUri(resumbleFile, 500 * 1024);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initDemoFile(String demoFile, long size) {
        String resumbleFile = demoFile;
        try {
            initLocalFileByFile(resumbleFile, size);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                initLocalFileByUri(resumbleFile, size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFilesFassets(Context context, String fileName, boolean cover) {
        String newPath = OSSTestConfig.FILE_DIR;
        copyFilesFassets(context, fileName, newPath, cover);
    }

    private static void copyFilesFassets(Context context, String fileName, String newPath, boolean cover) {
        try {
            File path = new File(newPath);
            File file = new File(newPath + fileName);
            if (!path.exists()) {
                OSSLog.logDebug("OSSTEST", "Create the path:" + path.getAbsolutePath());
                path.mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
                OSSLog.logDebug("OSSTEST", "create : " + file.getAbsolutePath());
            } else if (!cover) {
                return;
            }
            InputStream is = context.getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int byteCount=0;
            while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
            is.close();
            fos.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //如果捕捉到错误则通知UI线程
        }
    }

    public static Uri queryUri(String fileName) {
        Uri uri = null;

        ContentResolver contentResolver = mContext.getContentResolver();
        Uri external = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? AND "
                + MediaStore.Files.FileColumns.DISPLAY_NAME + "=?";
        String[] args = new String[]{MEDIA_FILE_DIR + "%", fileName};
        String[] projection = new String[]{MediaStore.Files.FileColumns._ID};
        Cursor cursor = contentResolver.query(external, projection, selection, args, null);

        if (cursor != null && cursor.moveToFirst()) {
            uri = ContentUris.withAppendedId(external, cursor.getLong(0));
            cursor.close();
        } else {
            uri = createFile(fileName);
        }
        return uri;
    }

    public static Uri errorUri() {
        Uri uri = null;
        uri = Uri.parse("https://photo-line.oss-cn-beijing.aliyuncs.com/111");
        return uri;
    }

    public static Uri createFile(String fileName) {
        Uri uri = null;
        ContentResolver contentResolver = mContext.getContentResolver();

        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.Files.FileColumns.MIME_TYPE, "file/*");
            values.put(MediaStore.Files.FileColumns.TITLE, fileName);
            values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, MEDIA_FILE_DIR);

            uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values);

            try {
                contentResolver.openFileDescriptor(uri, "w");
            } catch (Exception e) {
                return null;
            }
        }

        return uri;
    }

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

    public final static class CreateBucketCallback implements OSSCompletedCallback<CreateBucketRequest, CreateBucketResult> {

        public CreateBucketRequest request;
        public CreateBucketResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(CreateBucketRequest request, CreateBucketResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(CreateBucketRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestPutSymlinkCallback implements OSSCompletedCallback<PutSymlinkRequest, PutSymlinkResult> {

        public PutSymlinkRequest request;
        public PutSymlinkResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(PutSymlinkRequest request, PutSymlinkResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(PutSymlinkRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestGetSymlinkCallback implements OSSCompletedCallback<GetSymlinkRequest, GetSymlinkResult> {

        public GetSymlinkRequest request;
        public GetSymlinkResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(GetSymlinkRequest request, GetSymlinkResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(GetSymlinkRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestRestoreObjectCallback implements OSSCompletedCallback<RestoreObjectRequest, RestoreObjectResult> {

        public RestoreObjectRequest request;
        public RestoreObjectResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(RestoreObjectRequest request, RestoreObjectResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(RestoreObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
            if (clientExcepion != null) {
                clientExcepion.printStackTrace();
            }
            if (serviceException != null) {
                serviceException.printStackTrace();
            }
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestMultipartUploadCallback implements OSSCompletedCallback<MultipartUploadRequest, CompleteMultipartUploadResult> {

        public MultipartUploadRequest request;
        public CompleteMultipartUploadResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(MultipartUploadRequest request, CompleteMultipartUploadResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(MultipartUploadRequest request, ClientException clientExcepion, ServiceException serviceException) {
            if (clientExcepion != null) {
                clientExcepion.printStackTrace();
            }
            if (serviceException != null) {
                serviceException.printStackTrace();
            }
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
            OSSLog.logDebug("ObjectCRC64: " + result.getObjectCRC64());
            OSSLog.logDebug("NextPosition: " + result.getNextPosition());
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

    public final static class TestCreateBucketCallback implements OSSCompletedCallback<CreateBucketRequest, CreateBucketResult> {

        public CreateBucketRequest request;
        public CreateBucketResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(CreateBucketRequest request, CreateBucketResult result) {
            this.request = request;
            this.result = result;
            OSSLog.logVerbose("[Location]=" + result.bucketLocation);
        }

        @Override
        public void onFailure(CreateBucketRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestDeleteBucketCallback implements OSSCompletedCallback<DeleteBucketRequest, DeleteBucketResult> {

        public DeleteBucketRequest request;
        public DeleteBucketResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(DeleteBucketRequest request, DeleteBucketResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(DeleteBucketRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestGetBucketInfoCallback implements OSSCompletedCallback<GetBucketInfoRequest, GetBucketInfoResult> {

        public GetBucketInfoRequest request;
        public GetBucketInfoResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(GetBucketInfoRequest request, GetBucketInfoResult result) {
            this.request = request;
            this.result = result;
            OSSLog.logDebug("BucketOwner " + result.getBucket().owner, false);
            OSSLog.logDebug("BucketOwnerID " + result.getBucket().owner.getId(), false);
        }

        @Override
        public void onFailure(GetBucketInfoRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestGetBucketACLCallback implements OSSCompletedCallback<GetBucketACLRequest, GetBucketACLResult> {

        public GetBucketACLRequest request;
        public GetBucketACLResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(GetBucketACLRequest request, GetBucketACLResult result) {
            this.request = request;
            this.result = result;
            OSSLog.logDebug("BucketOwner " + result.getBucketOwner(), false);
            OSSLog.logDebug("BucketOwnerID " + result.getBucketOwnerID(), false);
        }

        @Override
        public void onFailure(GetBucketACLRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestHeadObjectCallback implements OSSCompletedCallback<HeadObjectRequest, HeadObjectResult> {

        public HeadObjectRequest request;
        public HeadObjectResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(HeadObjectRequest request, HeadObjectResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(HeadObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestCopyObjectCallback implements OSSCompletedCallback<CopyObjectRequest, CopyObjectResult> {

        public CopyObjectRequest request;
        public CopyObjectResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(CopyObjectRequest request, CopyObjectResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(CopyObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestInitiateMultipartCallback implements OSSCompletedCallback<InitiateMultipartUploadRequest, InitiateMultipartUploadResult> {

        public InitiateMultipartUploadRequest request;
        public InitiateMultipartUploadResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(InitiateMultipartUploadRequest request, InitiateMultipartUploadResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(InitiateMultipartUploadRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestAbortMultipartCallback implements OSSCompletedCallback<AbortMultipartUploadRequest, AbortMultipartUploadResult> {

        public AbortMultipartUploadRequest request;
        public AbortMultipartUploadResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(AbortMultipartUploadRequest request, AbortMultipartUploadResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(AbortMultipartUploadRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestListPartsCallback implements OSSCompletedCallback<ListPartsRequest, ListPartsResult> {

        public ListPartsRequest request;
        public ListPartsResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(ListPartsRequest request, ListPartsResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(ListPartsRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestUploadPartsCallback implements OSSCompletedCallback<UploadPartRequest, UploadPartResult> {

        public UploadPartRequest request;
        public UploadPartResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(UploadPartRequest request, UploadPartResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(UploadPartRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }

    public final static class TestCompleteMultipartCallback implements OSSCompletedCallback<CompleteMultipartUploadRequest, CompleteMultipartUploadResult> {

        public CompleteMultipartUploadRequest request;
        public CompleteMultipartUploadResult result;
        public ClientException clientException;
        public ServiceException serviceException;

        @Override
        public void onSuccess(CompleteMultipartUploadRequest request, CompleteMultipartUploadResult result) {
            this.request = request;
            this.result = result;
        }

        @Override
        public void onFailure(CompleteMultipartUploadRequest request, ClientException clientExcepion, ServiceException serviceException) {
            this.request = request;
            this.clientException = clientExcepion;
            this.serviceException = serviceException;
        }
    }
}
