package com.alibaba.sdk.android.oss.sample.customprovider;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by OSS on 2015/12/7 0007.
 * 完成显示图片，上传下载对话框显示，进度条更新等操作。
 */
public class UIDisplayer {

    private ImageView imageView;
    private ProgressBar bar;
    private TextView infoView;
    private Activity activity;

    private Handler handler;


    private static final int DOWNLOAD_OK = 1;
    private static final int DOWNLOAD_FAIL = 2;
    private static final int UPLOAD_OK = 3;
    private static final int UPLOAD_FAIL = 4;
    private static final int UPDATE_PROGRESS = 5;
    private static final int DISPLAY_IMAGE = 6;
    private static final int DISPLAY_INFO = 7;
    private static final int SETTING_OK = 88;


    /* 必须在UI线程中初始化handler */
    public UIDisplayer(ImageView imageView, ProgressBar bar, TextView infoView, Activity activity) {
        this.imageView = imageView;
        this.bar = bar;
        this.infoView = infoView;
        this.activity = activity;

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {

                String info;
                switch (inputMessage.what) {


                    case UPLOAD_OK:
                        new AlertDialog.Builder(UIDisplayer.this.activity).setTitle("上传成功").setMessage("upload to OSS OK!").show();
                        break;
                    case UPLOAD_FAIL:
                        info = (String) inputMessage.obj;
                        new AlertDialog.Builder(UIDisplayer.this.activity).setTitle("上传失败").setMessage(info).show();
                        break;
                    case DOWNLOAD_OK:
                        new AlertDialog.Builder(UIDisplayer.this.activity).setTitle("下载成功").setMessage("download from OSS OK!").show();
                        break;
                    case SETTING_OK:
                        new AlertDialog.Builder(UIDisplayer.this.activity).setTitle("设置成功").setMessage("设置域名信息成功,现在<选择图片>, 然后上传图片").show();
                        break;
                    case DOWNLOAD_FAIL:
                        info = (String) inputMessage.obj;
                        new AlertDialog.Builder(UIDisplayer.this.activity).setTitle("下载失败").setMessage(info).show();
                        break;
                    case UPDATE_PROGRESS:
                        UIDisplayer.this.bar.setProgress(inputMessage.arg1);
                        //Log.d("UpdateProgress", String.valueOf(inputMessage.arg1));
                        break;
                    case DISPLAY_IMAGE:
                        Bitmap bm = (Bitmap) inputMessage.obj;
                        UIDisplayer.this.imageView.setImageBitmap(bm);
                        break;
                    case DISPLAY_INFO:
                        info = (String) inputMessage.obj;
                        UIDisplayer.this.infoView.setText(info);

                    default:
                        break;
                }

            }
        };

    }

    //下载成功，显示对应的图片
    public void downloadComplete(Bitmap bm) {
        if (null != bm) {
            displayImage(bm);
        }

        Message mes = handler.obtainMessage(DOWNLOAD_OK);
        mes.sendToTarget();
    }

    public void settingOK() {
        Message mes = handler.obtainMessage(SETTING_OK);
        mes.sendToTarget();
    }

    //下载失败，显示对应的失败信息
    public void downloadFail(String info) {
        Message mes = handler.obtainMessage(DOWNLOAD_FAIL, info);
        mes.sendToTarget();
    }

    //上传成功
    public void uploadComplete() {
        Message mes = handler.obtainMessage(UPLOAD_OK);
        mes.sendToTarget();
    }

    //上传失败，显示对应的失败信息
    public void uploadFail(String info) {
        Message mes = handler.obtainMessage(UPLOAD_FAIL, info);
        mes.sendToTarget();
    }

    //更新进度，取值范围为[0,100]
    public void updateProgress(int progress) {
        //Log.d("UpdateProgress", String.valueOf(progress));
        if (progress > 100) {
            progress = 100;
        } else if (progress < 0) {
            progress = 0;
        }

        Message mes = handler.obtainMessage(UPDATE_PROGRESS, progress);
        mes.arg1 = progress;
        mes.sendToTarget();
    }

    //显示图像
    public void displayImage(Bitmap bm) {
        Message mes = handler.obtainMessage(DISPLAY_IMAGE, bm);
        mes.sendToTarget();
    }

    //在主界面输出文字信息
    public void displayInfo(String info) {
        Message mes = handler.obtainMessage(DISPLAY_INFO, info);
        mes.sendToTarget();
    }

    //根据ImageView的大小自动缩放图片
    public Bitmap autoResizeFromLocalFile(String picturePath) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imageView.getWidth(), imageView.getHeight());
        Log.d("ImageHeight", String.valueOf(options.outHeight));
        Log.d("ImageWidth", String.valueOf(options.outWidth));
        Log.d("Height", String.valueOf(imageView.getWidth()));
        Log.d("Width", String.valueOf(imageView.getWidth()));
        //options.inSampleSize = 10;

        Log.d("SampleSize", String.valueOf(options.inSampleSize));
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(picturePath, options);

    }

    //根据ImageView大小自动缩放图片
    public Bitmap autoResizeFromStream(InputStream stream) throws IOException {

        byte[] data;
        {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = stream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            data = outStream.toByteArray();
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, imageView.getWidth(), imageView.getHeight());
        Log.d("ImageHeight", String.valueOf(options.outHeight));
        Log.d("ImageWidth", String.valueOf(options.outWidth));
        Log.d("Height", String.valueOf(imageView.getWidth()));
        Log.d("Width", String.valueOf(imageView.getWidth()));
        //options.inSampleSize = 10;

        Log.d("SampleSize", String.valueOf(options.inSampleSize));
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }


    //计算图片缩放比例
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
