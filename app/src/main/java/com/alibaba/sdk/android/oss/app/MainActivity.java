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

    // To run the sample correctly, the following variables must have valid values.
    // The endpoint value below is just the example. Please use proper value according to your region.
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
        conf.setConnectionTimeout(15 * 1000); // set the connection timeout with 15s
        conf.setSocketTimeout(15 * 1000); // set the socket timeout with 15s
        conf.setMaxConcurrentRequest(5); // set Max concurrent request count with 5
        conf.setMaxErrorRetry(2); // set the max 2 retry on failure
        OSSLog.enableLog();
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


        // upload
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

        // download
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

        // list
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

        // multipart upload
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


        // resumable uploads
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

        // sign URL
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

        // bucket management
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
