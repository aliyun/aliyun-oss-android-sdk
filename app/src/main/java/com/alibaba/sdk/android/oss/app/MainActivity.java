package com.alibaba.sdk.android.oss.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.sample.GetObjectSamples;
import com.alibaba.sdk.android.oss.sample.ListObjectsSamples;
import com.alibaba.sdk.android.oss.sample.ManageBucketSamples;
import com.alibaba.sdk.android.oss.sample.ManageObjectSamples;
import com.alibaba.sdk.android.oss.sample.MultipartUploadSamples;
import com.alibaba.sdk.android.oss.sample.PutObjectSamples;
import com.alibaba.sdk.android.oss.sample.ResuambleUploadSamples;
import com.alibaba.sdk.android.oss.sample.SignURLSamples;
import com.alibaba.sdk.android.oss.sample.customprovider.AuthTestActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    /**
     * oss接口api的入口
     * 目前所有api接口传入的callback接口回调都是在子线程，
     * 底层并没有进行线程切换（子线程切换到ui线程），这个目前需要调用者自行来控制！
     */
    private OSS oss;

    // To run the sample correctly, the following variables must have valid values.
    // The endpoint value below is just the example. Please use proper value according to your region.
    private static final String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    private static final String uploadFilePath = "<upload_file_path>";
    private static final String testBucket = "<bucket_name>";
    private static final String uploadObject = "sampleObject";
    private static final String downloadObject = "sampleObject";

    private String mAuthUrl;

    private final String DIR_NAME = "oss";
    private final String FILE_NAME = "caifang.m4a";


    private ProgressBar mPb;
    public static final int DOWNLOAD_SUC = 1;
    public static final int DOWNLOAD_Fail = 2;
    public static final int UPLOAD_SUC = 3;
    public static final int UPLOAD_Fail = 4;
    public static final int UPLOAD_PROGRESS = 5;
    public static final int LIST_SUC = 6;
    public static final int HEAD_SUC = 7;
    public static final int RESUMABLE_SUC = 8;
    public static final int SIGN_SUC = 9;
    public static final int BUCKET_SUC = 10;
    public static final int GET_STS_SUC = 11;
    public static final int MULTIPART_SUC = 12;
    public static final int STS_TOKEN_SUC = 13;
    public static final int FAIL = 9999;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mPb.setVisibility(View.GONE);
            boolean handled = false;
            switch (msg.what) {
                case DOWNLOAD_SUC:
                    Toast.makeText(MainActivity.this, "down_suc", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
                case DOWNLOAD_Fail:
                    handled = true;
                    break;
                case UPLOAD_SUC:
                    mUploadPb.setProgress(0);
                    Toast.makeText(MainActivity.this, "upload_suc", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
                case UPLOAD_Fail:
                    mUploadPb.setProgress(0);
                    handled = true;
                    break;
                case UPLOAD_PROGRESS:
                    Bundle data = msg.getData();
                    long currentSize = data.getLong("currentSize");
                    long totalSize = data.getLong("totalSize");
                    OSSLog.logDebug("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
                    mUploadPb.setProgress((int) ((currentSize * 100) / totalSize));
                    handled = true;
                    break;
                case LIST_SUC:
                    Toast.makeText(MainActivity.this, "list_suc", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
                case HEAD_SUC:
                    Toast.makeText(MainActivity.this, "manage_suc", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
                case RESUMABLE_SUC:
                    Toast.makeText(MainActivity.this, "resumable_suc", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
                case SIGN_SUC:
                    Toast.makeText(MainActivity.this, "sign_suc", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
                case BUCKET_SUC:
                    Toast.makeText(MainActivity.this, "bucket_suc", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
                case MULTIPART_SUC:
                    Toast.makeText(MainActivity.this, "multipart_suc", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
                case FAIL:
                    Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                    handled = true;
                    break;
            }

            return handled;
        }
    });
    private ProgressBar mUploadPb;
    private OSSCredentialProvider mCredentialProvider;
    private ManageBucketSamples manageBucketSamples;
    private SignURLSamples signURLSamples;
    private ResuambleUploadSamples resuambleUploadSamples;
    private PutObjectSamples putObjectSamples;
    private GetObjectSamples getObjectSamples;
    private ListObjectsSamples listObjectsSamples;
    private ManageObjectSamples manageObjectSamples;
    private MultipartUploadSamples multipartUploadSamples;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        setContentView(R.layout.activity_main);

        initViews();

        //please init local sts server firstly. please check python/*.py for more info.
        setOssClient();
    }

    private void initViews() {
        mPb = (ProgressBar) findViewById(R.id.progress_bar);
        mUploadPb = (ProgressBar) findViewById(R.id.upload_progress);

        // auth
        Button auth = (Button) findViewById(R.id.auth);
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, AuthTestActivity.class), 9999);
            }
        });

        // upload
        Button upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNotNull(putObjectSamples)) {
                    putObjectSamples.asyncPutObjectFromLocalFile(new ProgressCallback<PutObjectRequest, PutObjectResult>() {
                        @Override
                        public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                            handler.sendEmptyMessage(UPLOAD_SUC);
                        }

                        @Override
                        public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                            handler.sendEmptyMessage(UPLOAD_Fail);
                        }

                        @Override
                        public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                            Message msg = Message.obtain();
                            msg.what = UPLOAD_PROGRESS;
                            Bundle data = new Bundle();
                            data.putLong("currentSize", currentSize);
                            data.putLong("totalSize", totalSize);
                            msg.setData(data);
                            handler.sendMessage(msg);
                        }

                    });
                }
            }
        });

        // download
        Button download = (Button) findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPb.setVisibility(View.VISIBLE);
                if (checkNotNull(getObjectSamples)) {
                    getObjectSamples.asyncGetObjectSample(new Callback<GetObjectRequest, GetObjectResult>() {
                        @Override
                        public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                            // 请求成功 处理数据
                            InputStream inputStream = result.getObjectContent();

                            byte[] buffer = new byte[2048];
                            int len;

                            try {
                                while ((len = inputStream.read(buffer)) != -1) {
                                    // 处理下载的数据
                                    OSSLog.logDebug("asyncGetObjectSample", "read length: " + len, false);
                                }
                                OSSLog.logDebug("asyncGetObjectSample", "download success.");
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.sendEmptyMessage(DOWNLOAD_SUC);//发起ui回调
                        }

                        @Override
                        public void onFailure(GetObjectRequest request, ClientException clientException, ServiceException serviceException) {
                            handler.sendEmptyMessage(DOWNLOAD_Fail);//发起ui回调
                            // 请求异常
                            if (clientException != null) {
                                // 本地异常如网络异常等
                                clientException.printStackTrace();
                            }
                            if (serviceException != null) {
                                // 服务异常
                                OSSLog.logDebug("ErrorCode", serviceException.getErrorCode());
                                OSSLog.logDebug("RequestId", serviceException.getRequestId());
                                OSSLog.logDebug("HostId", serviceException.getHostId());
                                OSSLog.logDebug("RawMessage", serviceException.getRawMessage());
                            }
                        }
                    });
                }
            }
        });

        // list
        Button list = (Button) findViewById(R.id.list);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNotNull(listObjectsSamples)) {
                    mPb.setVisibility(View.VISIBLE);
                    listObjectsSamples.asyncListObjectsWithPrefix();
                }
            }
        });

        // manage
        Button manage = (Button) findViewById(R.id.manage);
        manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNotNull(manageObjectSamples)) {
                    mPb.setVisibility(View.VISIBLE);
                    manageObjectSamples.headObject();
                }
            }
        });

        // multipart upload
        Button multipart = (Button) findViewById(R.id.multipart);
        multipart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNotNull(multipartUploadSamples)) {
                    mPb.setVisibility(View.VISIBLE);
                    multipartUploadSamples.asyncMultipartUpload();
                }
            }
        });


        // resumable uploads
        Button resumable = (Button) findViewById(R.id.resumable);
        resumable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNotNull(resuambleUploadSamples)) {
                    mPb.setVisibility(View.VISIBLE);
                    resuambleUploadSamples.resumableUpload();
                }
            }
        });

        // sign URL
        Button sign = (Button) findViewById(R.id.sign);
        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNotNull(signURLSamples)) {
                    mPb.setVisibility(View.VISIBLE);
                    signURLSamples.presignConstrainedURL();
                }
            }
        });

        // bucket management
        Button bucket = (Button) findViewById(R.id.bucket);
        bucket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNotNull(manageBucketSamples)) {
                    mPb.setVisibility(View.VISIBLE);
                    manageBucketSamples.deleteNotEmptyBucket();
                }
            }
        });

    }


    public void setOssClient() {
        if (mCredentialProvider == null || oss == null) {
            mCredentialProvider = new OSSAuthCredentialsProvider(AuthTestActivity.STSSERVER);
            ClientConfiguration conf = new ClientConfiguration();
            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
            conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
            OSSLog.enableLog(); //这个开启会支持写入手机sd卡中的一份日志文件位置在SD_path\OSSLog\logs.csv
            oss = new OSSClient(getApplicationContext(), endpoint, mCredentialProvider, conf);

            initSamples();
        }
    }

    private boolean checkNotNull(Object obj) {
        if (obj != null) {
            return true;
        }
        Toast.makeText(MainActivity.this, "init Samples fail", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void initSamples() {
        multipartUploadSamples = new MultipartUploadSamples(oss, testBucket, uploadObject, uploadFilePath, handler);
        manageObjectSamples = new ManageObjectSamples(oss, testBucket, uploadObject, handler);
        listObjectsSamples = new ListObjectsSamples(oss, testBucket, handler);
        getObjectSamples = new GetObjectSamples(oss, testBucket, downloadObject);
        putObjectSamples = new PutObjectSamples(oss, testBucket, uploadObject, uploadFilePath);
        resuambleUploadSamples = new ResuambleUploadSamples(oss, testBucket, uploadObject, uploadFilePath, handler);
        signURLSamples = new SignURLSamples(oss, testBucket, uploadObject, uploadFilePath, handler);
        manageBucketSamples = new ManageBucketSamples(oss, "sample-bucket-test", uploadFilePath, handler);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9999 && resultCode == RESULT_OK) {
            if (data != null) {
                String url = data.getStringExtra("url");
                String bucketName = data.getStringExtra("bucketName");
                mAuthUrl = url;
                setSamplesBucket(bucketName);
                OSSAuthCredentialsProvider provider = new OSSAuthCredentialsProvider(mAuthUrl);
                oss.updateCredentialProvider(provider);
            }
        }
    }

    private void setSamplesBucket(String bucket){
        multipartUploadSamples.setTestBucket(bucket);
        manageObjectSamples.setTestBucket(bucket);
        listObjectsSamples.setTestBucket(bucket);
        getObjectSamples.setTestBucket(bucket);
        putObjectSamples.setTestBucket(bucket);
        resuambleUploadSamples.setTestBucket(bucket);
        signURLSamples.setTestBucket(bucket);
        manageBucketSamples.setBucketName(bucket);
    }
}
