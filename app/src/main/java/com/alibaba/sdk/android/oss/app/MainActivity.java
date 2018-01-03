package com.alibaba.sdk.android.oss.app;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.alibaba.sdk.android.oss.sample.BatchUploadSamples;
import com.alibaba.sdk.android.oss.sample.GetObjectSamples;
import com.alibaba.sdk.android.oss.sample.ListObjectsSamples;
import com.alibaba.sdk.android.oss.sample.ManageBucketSamples;
import com.alibaba.sdk.android.oss.sample.ManageObjectSamples;
import com.alibaba.sdk.android.oss.sample.MultipartUploadSamples;
import com.alibaba.sdk.android.oss.sample.PutObjectSamples;
import com.alibaba.sdk.android.oss.sample.ResuambleUploadSamples;
import com.alibaba.sdk.android.oss.sample.SignURLSamples;
import com.alibaba.sdk.android.oss.sample.customprovider.AuthTestActivity;
import com.tangxiaolv.telegramgallery.GalleryActivity;
import com.tangxiaolv.telegramgallery.GalleryConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    /**
     * oss接口api的入口
     * 目前所有api接口传入的callback接口回调都是在子线程，
     * 底层并没有进行线程切换（子线程切换到ui线程），这个目前需要调用者自行来控制！
     */
    private OSS oss;

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
    public static final int REQUESTCODE_AUTH = 10111;
    public static final int REQUESTCODE_LOCALPHOTOS = 10112;


    public static final int MESSAGE_UPLOAD_2_OSS = 10002;

    //对话框
    private MaterialDialog loadingDialog;

    public static final String UPLOADING = "上传中...";

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
                    dismissLoading();
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
                case MESSAGE_UPLOAD_2_OSS:
                    showLoading();
                    final List<String> localPhotos = (List<String>) msg.obj;
                    batchUploadSamples = new BatchUploadSamples(oss, Config.bucket, localPhotos, handler);
                    batchUploadSamples.upload();
                    return true;
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
    private BatchUploadSamples batchUploadSamples;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
//                .detectNetwork()   // or .detectAll() for all detectable problems
//                .penaltyLog()
//                .build());
//
//        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                .detectLeakedSqlLiteObjects()
//                .detectLeakedClosableObjects()
//                .penaltyLog()
//                .penaltyDeath()
//                .build());

        setContentView(R.layout.activity_main);

        Log.d("cpu", Build.CPU_ABI);
        initViews();
        initDialog();
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
                startActivityForResult(new Intent(MainActivity.this, AuthTestActivity.class), REQUESTCODE_AUTH);
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

        // batch upload
        Button batch = (Button) findViewById(R.id.batch_upload);
        batch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开本地照片库
                GalleryConfig config = new GalleryConfig.Build()
                        .singlePhoto(false).build();
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                intent.putExtra("GALLERY_CONFIG", config);
                startActivityForResult(intent, REQUESTCODE_LOCALPHOTOS);
            }
        });

    }

    private void initDialog(){
        loadingDialog = new MaterialDialog.Builder(MainActivity.this)
                .content(UPLOADING)
                .progress(true, 0)
                .build();
    }


    private void showLoading() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoading(){
        if (loadingDialog != null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }

    public void setOssClient() {
        if (mCredentialProvider == null || oss == null) {
//        移动端是不安全环境，不建议直接使用阿里云主账号ak，sk的方式。建议使用STS方式。具体参
//        https://help.aliyun.com/document_detail/31920.html
//        注意：SDK 提供的 PlainTextAKSKCredentialProvider 只建议在测试环境或者用户可以保证阿里云主账号AK，SK安全的前提下使用。具体使用如下
//        主账户使用方式
//        String AK = "******";
//        String SK = "******";
//        credentialProvider = new PlainTextAKSKCredentialProvider(AK,SK)
//        以下是使用STS Sever方式。

            mCredentialProvider = new OSSAuthCredentialsProvider(Config.STSSERVER);
            ClientConfiguration conf = new ClientConfiguration();
            conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
            conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
            conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
            conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
            OSSLog.enableLog(); //这个开启会支持写入手机sd卡中的一份日志文件位置在SD_path\OSSLog\logs.csv
            oss = new OSSClient(getApplicationContext(), Config.endpoint, mCredentialProvider, conf);

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
        multipartUploadSamples = new MultipartUploadSamples(oss, Config.bucket, Config.uploadObject, Config.uploadFilePath, handler);
        manageObjectSamples = new ManageObjectSamples(oss, Config.bucket, Config.uploadObject, handler);
        listObjectsSamples = new ListObjectsSamples(oss, Config.bucket, handler);
        getObjectSamples = new GetObjectSamples(oss, Config.bucket, Config.downloadObject);
        putObjectSamples = new PutObjectSamples(oss, Config.bucket, Config.uploadObject, Config.uploadFilePath);
        resuambleUploadSamples = new ResuambleUploadSamples(oss, Config.bucket, Config.uploadObject, Config.uploadFilePath, handler);
        signURLSamples = new SignURLSamples(oss, Config.bucket, Config.uploadObject, Config.uploadFilePath, handler);
        manageBucketSamples = new ManageBucketSamples(oss, "sample-bucket-test", Config.uploadFilePath, handler);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUESTCODE_AUTH && resultCode == RESULT_OK) {
            if (data != null) {
                String url = data.getStringExtra("url");
                String endpoint = data.getStringExtra("endpoint");
                String bucketName = data.getStringExtra("bucketName");
                OSSAuthCredentialsProvider provider = new OSSAuthCredentialsProvider(url);
                ClientConfiguration conf = new ClientConfiguration();
                conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
                conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
                conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
                conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
                OSSLog.enableLog(); //这个开启会支持写入手机sd卡中的一份日志文件位置在SD_path\OSSLog\logs.csv
                oss = new OSSClient(getApplicationContext(), endpoint, provider, conf);
                setSamplesBucket(bucketName, oss);
            }
        }

        if (requestCode == REQUESTCODE_LOCALPHOTOS && resultCode == RESULT_OK) {
            List<String> localPhotos = (List<String>) data.getSerializableExtra(GalleryActivity.PHOTOS);
            Message message = handler.obtainMessage();
            message.what = MESSAGE_UPLOAD_2_OSS;
            message.obj = localPhotos;
            message.sendToTarget();
        }


    }

    private void setSamplesBucket(String bucket, OSS oss) {
        multipartUploadSamples.setTestBucket(bucket);
        manageObjectSamples.setTestBucket(bucket);
        listObjectsSamples.setTestBucket(bucket);
        getObjectSamples.setTestBucket(bucket);
        putObjectSamples.setTestBucket(bucket);
        resuambleUploadSamples.setTestBucket(bucket);
        signURLSamples.setTestBucket(bucket);

        multipartUploadSamples.setOss(oss);
        manageObjectSamples.setOss(oss);
        listObjectsSamples.setOss(oss);
        getObjectSamples.setOss(oss);
        putObjectSamples.setOss(oss);
        resuambleUploadSamples.setOss(oss);
        signURLSamples.setOss(oss);
        manageBucketSamples.setOss(oss);
    }
}
