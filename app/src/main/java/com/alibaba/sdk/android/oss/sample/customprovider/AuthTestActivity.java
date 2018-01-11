package com.alibaba.sdk.android.oss.sample.customprovider;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.app.Config;
import com.alibaba.sdk.android.oss.app.R;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.tangxiaolv.telegramgallery.*;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.sdk.android.oss.sample.BatchUploadSamples;

import static com.alibaba.sdk.android.oss.app.Config.MESSAGE_UPLOAD_2_OSS;
import static com.alibaba.sdk.android.oss.app.Config.STSSERVER;
import static com.alibaba.sdk.android.oss.app.Config.UPLOAD_SUC;

public class AuthTestActivity extends AppCompatActivity {

    private String imgEndpoint = "http://img-cn-shenzhen.aliyuncs.com";
    private final String mBucket = Config.bucket;
    private String mRegion = "";//杭州
    //负责所有的界面更新
    private UIDisplayer mUIDisplayer;

    //OSS的上传下载
    private OssService ossService;
    private ImageService imageService;
    private String picturePath = "";

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String FILE_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "oss/";
    private static final  String FILE_PATH = FILE_DIR + "wangwang.zip";
    private MaterialDialog loadingDialog;
    private BatchUploadSamples batchUploadSamples;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boolean handled = false;
            switch (msg.what) {
                case  UPLOAD_SUC:
                    dismissLoading();
                    return true;

                case MESSAGE_UPLOAD_2_OSS:
                    showLoading();
                    final List<String> localPhotos = (List<String>) msg.obj;
                    batchUploadSamples = new BatchUploadSamples(ossService.mOss, Config.bucket, localPhotos, handler);
                    batchUploadSamples.upload();
                    return true;
            }

            return handled;
        }
    });

    private void initDialog(){
        loadingDialog = new MaterialDialog.Builder(AuthTestActivity.this)
                .content("上传中...")
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

    public OssService initOSS(String endpoint, String bucket, UIDisplayer displayer) {

//        移动端是不安全环境，不建议直接使用阿里云主账号ak，sk的方式。建议使用STS方式。具体参
//        https://help.aliyun.com/document_detail/31920.html
//        注意：SDK 提供的 PlainTextAKSKCredentialProvider 只建议在测试环境或者用户可以保证阿里云主账号AK，SK安全的前提下使用。具体使用如下
//        主账户使用方式
//        String AK = "******";
//        String SK = "******";
//        credentialProvider = new PlainTextAKSKCredentialProvider(AK,SK)
//        以下是使用STS Sever方式。

        OSSCredentialProvider credentialProvider;
        //使用自己的获取STSToken的类
        String stsServer = ((EditText) findViewById(R.id.stsserver)).getText().toString();
        if (TextUtils.isEmpty(stsServer)) {
            credentialProvider = new OSSAuthCredentialsProvider(Config.STSSERVER);
            ((EditText) findViewById(R.id.stsserver)).setText(Config.STSSERVER);
        } else {
            credentialProvider = new OSSAuthCredentialsProvider(stsServer);
        }

        String editBucketName = ((EditText) findViewById(R.id.bucketname)).getText().toString();
        if (TextUtils.isEmpty(editBucketName)) {
            editBucketName = bucket;
            ((EditText) findViewById(R.id.bucketname)).setText(bucket);
        }
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider, conf);
        return new OssService(oss, editBucketName, displayer);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRegion();
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        ProgressBar bar = (ProgressBar) findViewById(R.id.bar);
        TextView textView = (TextView) findViewById(R.id.output_info);

        mUIDisplayer = new UIDisplayer(imageView, bar, textView, this);
        ossService = initOSS(Config.endpoint, Config.bucket, mUIDisplayer);
        //设置上传的callback地址，目前暂时只支持putObject的回调
        ossService.setCallbackAddress(Config.callbackAddress);

        //图片服务和OSS使用不同的endpoint，但是可以共用SDK，因此只需要初始化不同endpoint的OssService即可
        imageService = new ImageService(initOSS(imgEndpoint, mBucket, mUIDisplayer));

        //从系统相册选择图片
        final Button select = (Button) findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        Button upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.edit_text);
                String objectName = editText.getText().toString();

                ossService.asyncPutImage(objectName, picturePath);

            }
        });

        Button setting = (Button) findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String bucketName = ((EditText) findViewById(R.id.bucketname)).getText().toString();
                ossService.setBucketName(bucketName);
                String newOssEndpoint = getOssEndpoint();
                String newImageEndpoint = getImgEndpoint();
                Log.d(newOssEndpoint, newImageEndpoint);

                OSSCredentialProvider credentialProvider;
                //使用自己的获取STSToken的类
                String stsServer = ((EditText) findViewById(R.id.stsserver)).getText().toString();
                if (TextUtils.isEmpty(stsServer)) {
                    stsServer = STSSERVER;
                }
                credentialProvider = new OSSAuthCredentialsProvider(stsServer);
                ClientConfiguration conf = new ClientConfiguration();
                conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
                conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
                conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
                conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
                OSS oss = new OSSClient(getApplicationContext(), newOssEndpoint, credentialProvider, conf);
                imageService = new ImageService(initOSS(newImageEndpoint, bucketName, mUIDisplayer));
                ossService.initOss(oss);

                mUIDisplayer.settingOK();
                Intent data = new Intent();
                data.putExtra("bucketName", bucketName);
                data.putExtra("url", stsServer);
                data.putExtra("endpoint", newOssEndpoint);
                setResult(RESULT_OK, data);

            }
        });

        Button download = (Button) findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.edit_text);
                String objectName = editText.getText().toString();
                ossService.asyncGetImage(objectName);
            }
        });

        Button watermark = (Button) findViewById(R.id.watermark);
        watermark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.edit_text);
                String objectName = editText.getText().toString();

                String text = ((EditText) findViewById(R.id.watermark_text)).getText().toString();
                try {
                    int size = Integer.valueOf(((EditText) findViewById(R.id.watermark_size)).getText().toString());
                    if (!text.equals("")) {
                        imageService.textWatermark(objectName, text, size);
                    }
                } catch (NumberFormatException e) {
                    new AlertDialog.Builder(AuthTestActivity.this).setTitle("错误").setMessage(e.toString()).show();
                }


            }
        });

        Button resize = (Button) findViewById(R.id.resize);
        resize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.edit_text);
                String objectName = editText.getText().toString();

                try {
                    int width = Integer.valueOf(((EditText) findViewById(R.id.resize_width)).getText().toString());
                    int height = Integer.valueOf(((EditText) findViewById(R.id.resize_height)).getText().toString());
                    imageService.resize(objectName, width, height);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(AuthTestActivity.this).setTitle("错误").setMessage(e.toString()).show();
                }

            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.region);
        String[] regions = getResources().getStringArray(R.array.bucketregion);
        for (int i = 0; i < regions.length; i++) {
            String region = regions[i];
            if (mRegion.equals(region)) {
                spinner.setSelection(i);
                break;
            }
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                String[] regions = getResources().getStringArray(R.array.bucketregion);
                mRegion = regions[pos];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button bucketBucket = (Button) findViewById(R.id.manageBuckets);
        final String bucketToDelete = new String("runsheng-delete");
        bucketBucket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ossService.deleteNotEmptyBucket(bucketToDelete,FILE_DIR+"file1k");
            }
        });

        Button signButton = (Button) findViewById(R.id.ossSign);
        signButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ossService.presignConstrainedURL();
            }
        });

        Button headButton = (Button) findViewById(R.id.headObject);
        headButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ossService.headObject("androidTest.jpeg");
            }
        });

        Button listButton = (Button) findViewById(R.id.listObjects);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ossService.asyncListObjectsWithBucketName();
            }
        });

        Button multipartButton = (Button) findViewById(R.id.multipartUpload);
        multipartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ossService.asyncMultipartUpload("multipartObject",FILE_PATH);
            }
        });

        Button resumableButton = (Button) findViewById(R.id.resumableUpload);
        resumableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ossService.asyncResumableUpload(FILE_PATH);
            }
        });

        Button customSignBtn = (Button) findViewById(R.id.cutomSign);
        customSignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ossService.customSign(getApplicationContext(),Config.endpoint);
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
                Intent intent = new Intent(AuthTestActivity.this, GalleryActivity.class);
                intent.putExtra("GALLERY_CONFIG", config);
                startActivityForResult(intent, Config.REQUESTCODE_LOCALPHOTOS);
            }
        });

        copyLocalFile();
        initLocalFiles();
        initDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.REQUESTCODE_AUTH && resultCode == RESULT_OK) {
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

            }
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            Log.d("PickPicture", picturePath);
            cursor.close();

            try {
                Bitmap bm = mUIDisplayer.autoResizeFromLocalFile(picturePath);
                mUIDisplayer.displayImage(bm);
                /*
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bm);*/
                File file = new File(picturePath);

                mUIDisplayer.displayInfo("文件: " + picturePath + "\n大小: " + String.valueOf(file.length()));
            } catch (IOException e) {
                e.printStackTrace();
                mUIDisplayer.displayInfo(e.toString());
            }

        }

        if (requestCode == Config.REQUESTCODE_LOCALPHOTOS && resultCode == RESULT_OK) {
            List<String> localPhotos = (List<String>) data.getSerializableExtra(GalleryActivity.PHOTOS);
            Message message = handler.obtainMessage();
            message.what = MESSAGE_UPLOAD_2_OSS;
            message.obj = localPhotos;
            message.sendToTarget();
        }

    }

    protected void initRegion() {
        if (TextUtils.isEmpty(Config.endpoint)) {
            return;
        }
        if (Config.endpoint.contains("oss-cn-hangzhou")) {
            mRegion = "杭州";
            imgEndpoint = getImgEndpoint();
        } else if (Config.endpoint.contains("oss-cn-qingdao")) {
            mRegion = "青岛";
            imgEndpoint = getImgEndpoint();
        } else if (Config.endpoint.contains("oss-cn-beijing")) {
            mRegion = "北京";
            imgEndpoint = getImgEndpoint();
        } else if (Config.endpoint.contains("oss-cn-shenzhen")) {
            mRegion = "深圳";
            imgEndpoint = getImgEndpoint();
        } else if (Config.endpoint.contains("oss-us-west-1")) {
            mRegion = "美国";
            imgEndpoint = getImgEndpoint();
        } else if (Config.endpoint.contains("oss-cn-shanghai")) {
            mRegion = "上海";
            imgEndpoint = getImgEndpoint();
        } else {
            Toast.makeText(AuthTestActivity.this,"错误的区域",Toast.LENGTH_SHORT).show();
//            new AlertDialog.Builder(AuthTestActivity.this).setTitle("错误的区域").setMessage(mRegion).show();
        }
    }

    protected String getOssEndpoint() {
        String ossEndpoint = "";
        if (mRegion.equals("杭州")) {
            ossEndpoint = "http://oss-cn-hangzhou.aliyuncs.com";
        } else if (mRegion.equals("青岛")) {
            ossEndpoint = "http://oss-cn-qingdao.aliyuncs.com";
        } else if (mRegion.equals("北京")) {
            ossEndpoint = "http://oss-cn-beijing.aliyuncs.com";
        } else if (mRegion.equals("深圳")) {
            ossEndpoint = "http://oss-cn-shenzhen.aliyuncs.com";
        } else if (mRegion.equals("美国")) {
            ossEndpoint = "http://oss-us-west-1.aliyuncs.com";
        } else if (mRegion.equals("上海")) {
            ossEndpoint = "http://oss-cn-shanghai.aliyuncs.com";
        } else {
            new AlertDialog.Builder(AuthTestActivity.this).setTitle("错误的区域").setMessage(mRegion).show();
        }
        return ossEndpoint;
    }

    protected String getImgEndpoint() {
        String imgEndpoint = "";
        if (mRegion.equals("杭州")) {
            imgEndpoint = "http://img-cn-hangzhou.aliyuncs.com";
        } else if (mRegion.equals("青岛")) {
            imgEndpoint = "http://img-cn-qingdao.aliyuncs.com";
        } else if (mRegion.equals("北京")) {
            imgEndpoint = "http://img-cn-beijing.aliyuncs.com";
        } else if (mRegion.equals("深圳")) {
            imgEndpoint = "http://img-cn-shenzhen.aliyuncs.com";
        } else if (mRegion.equals("美国")) {
            imgEndpoint = "http://img-us-west-1.aliyuncs.com";
        } else if (mRegion.equals("上海")) {
            imgEndpoint = "http://img-cn-shanghai.aliyuncs.com";
        } else {
            new AlertDialog.Builder(AuthTestActivity.this).setTitle("错误的区域").setMessage(mRegion).show();
            imgEndpoint = "";
        }
        return imgEndpoint;
    }

    private void copyLocalFile() {
        String zipFile = "wangwang.zip";
        String filePath = this.FILE_DIR + zipFile;
        try {
            File path = new File(this.FILE_DIR);
            File file = new File(filePath);
            if (!path.exists()) {
                OSSLog.logDebug("MULTIPART_UPLOAD", "Create the path:" + path.getAbsolutePath());
                path.mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
                OSSLog.logDebug("MULTIPART_UPLOAD", "create : " + file.getAbsolutePath());
            } else {
                return;
            }


            InputStream input = getBaseContext().getAssets().open(zipFile);

            OSSLog.logDebug("input.available() : " + input.available());

            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[500 * 1024];
            int byteCount = 0;
            int totalReadByte = 0;
            while ((byteCount = input.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                totalReadByte += byteCount;
            }
            OSSLog.logDebug("totalReadByte : " + totalReadByte);
            fos.flush();//刷新缓冲区
            input.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLocalFiles() {
        String[] fileNames = {"file1k", "file10k", "file100k", "file1m", "file10m"};
        int[] fileSize = {1024, 10240, 102400, 1024000, 10240000};

        for (int i = 0; i < fileNames.length; i++) {
            try {
                String filePath = FILE_DIR + fileNames[i];
                OSSLog.logDebug("OSSTEST", "filePath : " + filePath);
                File path = new File(FILE_DIR);
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
                int part = fileSize[i] / buf_size;
                while (index < part) {
                    byte[] buf = new byte[1024];
                    fos.write(buf);
                    index++;
                }
                in.close();
                fos.close();
                OSSLog.logDebug("OSSTEST", "file write" + fileNames[i] + " ok");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
