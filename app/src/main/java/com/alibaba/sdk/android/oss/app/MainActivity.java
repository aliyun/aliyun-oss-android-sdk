package com.alibaba.sdk.android.oss.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.sample.GetObjectSamples;
import com.alibaba.sdk.android.oss.sample.ListObjectsSamples;
import com.alibaba.sdk.android.oss.sample.ManageBucketSamples;
import com.alibaba.sdk.android.oss.sample.ManageObjectSamples;
import com.alibaba.sdk.android.oss.sample.MultipartUploadSamples;
import com.alibaba.sdk.android.oss.sample.PutObjectSamples;
import com.alibaba.sdk.android.oss.sample.ResuambleUploadSamples;
import com.alibaba.sdk.android.oss.sample.SignURLSamples;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private OSS oss;

    // 运行sample前需要配置以下字段为有效的值
    private static final String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";
    private static final String uploadFilePath = "<upload_file_path>";

    private static final String testBucket = "<bucket_name>";
    private static final String uploadObject = "sampleObject";
    private static final String downloadObject = "sampleObject";


    private final String DIR_NAME = "oss";
    private final String FILE_NAME = "caifang.m4a";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider("<StsToken.AccessKeyId>", "<StsToken.SecretKeyId>", "<StsToken.SecurityToken>");
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSSLog.enableLog(); //这个开启会支持写入手机sd卡中的一份日志文件位置在SD_path\OSSLog\logs.csv

        oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider, conf);


        try {
            Log.i("MainActivity : ", "uploadFilePath : " + uploadFilePath);
            File uploadFile = new File(uploadFilePath);
            InputStream input = new FileInputStream(uploadFile);
            long fileLength = uploadFile.length();
            Log.i("MainActivity : ", "fileLength : " + fileLength);
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 上传
        Button upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new PutObjectSamples(oss, testBucket, uploadObject, uploadFilePath).asyncPutObjectFromLocalFile();
                    }
                }).start();
            }
        });

        // 下载
        Button download = (Button) findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new GetObjectSamples(oss, testBucket, downloadObject).asyncGetObjectSample();
                    }
                }).start();
            }
        });

        // 罗列
        Button list = (Button) findViewById(R.id.list);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ListObjectsSamples(oss, testBucket).asyncListObjectsWithPrefix();
                    }
                }).start();
            }
        });

        // manage
        Button manage = (Button) findViewById(R.id.manage);
        manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ManageObjectSamples(oss, testBucket, uploadObject).headObject();
                    }
                }).start();
            }
        });

        // multipart上传
        Button multipart = (Button) findViewById(R.id.multipart);
        multipart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new MultipartUploadSamples(oss, testBucket, uploadObject, uploadFilePath).asyncMultipartUpload();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });


        // 断点上传
        Button resumable = (Button) findViewById(R.id.resumable);
        resumable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ResuambleUploadSamples(oss, testBucket, uploadObject, uploadFilePath).resumableUpload();
                    }
                }).start();
            }
        });

        // 签名URL
        Button sign = (Button) findViewById(R.id.sign);
        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new SignURLSamples(oss, testBucket, uploadObject, uploadFilePath).presignConstrainedURL();
                    }
                }).start();
            }
        });

        // bucket管理
        Button bucket = (Button) findViewById(R.id.bucket);
        bucket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new ManageBucketSamples(oss, testBucket, uploadFilePath).deleteNotEmptyBucket();
                    }
                }).start();
            }
        });
    }
}
