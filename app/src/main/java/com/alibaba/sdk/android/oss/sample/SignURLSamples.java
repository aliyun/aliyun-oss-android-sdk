package com.alibaba.sdk.android.oss.sample;


import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.app.MainActivity;
import com.alibaba.sdk.android.oss.common.OSSLog;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.os.Handler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhouzhuo on 12/3/15.
 */
public class SignURLSamples {

    private OSS oss;
    private String testBucket;
    private String testObject;
    private WeakReference<Handler> handler;

    public SignURLSamples(OSS client, String testBucket, String testObject, String uploadFilePath,Handler handler) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
        this.handler = new WeakReference<>(handler);
    }

    // If the bucket is private, the signed URL is required for the access.
    // Expiration time is specified in the signed URL.
    public void presignConstrainedURL() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Gets the signed url, the expiration time is 5 minute
                        String url = oss.presignConstrainedObjectURL(testBucket, testObject, 5 * 60);
                        OSSLog.logDebug("signContrainedURL", "get url: " + url);
                        // 访问该url
                        Request request = new Request.Builder().url(url).build();
                        Response resp = null;

                        resp = new OkHttpClient().newCall(request).execute();

                        if (resp.code() == 200) {
                            OSSLog.logDebug("signContrainedURL", "object size: " + resp.body().contentLength());
                            handler.get().sendEmptyMessage(MainActivity.SIGN_SUC);
                        } else {
                            OSSLog.logDebug("signContrainedURL", "get object failed, error code: " + resp.code()
                                    + "error message: " + resp.message());
                            handler.get().sendEmptyMessage(MainActivity.FAIL);
                        }
                    }catch (IOException e) {
                        e.printStackTrace();
                        handler.get().sendEmptyMessage(MainActivity.FAIL);
                    }catch (ClientException e) {
                        e.printStackTrace();
                        handler.get().sendEmptyMessage(MainActivity.FAIL);
                    }
                }
            }).start();
    }


    // If the bucket is public, the URL does not need sign and no need to set the expiration time
    public void presignPublicURL() {
        try {
            // Gets the url, no expiration time
            String url = oss.presignPublicObjectURL(testBucket, testObject);
            OSSLog.logDebug("signPublicURL", "get url: " + url);
            // gets the object via the url
            Request request = new Request.Builder().url(url).build();
            Response resp = new OkHttpClient().newCall(request).execute();
            if (resp.code() == 200) {
                OSSLog.logDebug("signPublicURL", "get object size: " + resp.body().contentLength());
            } else {
                OSSLog.logError("signPublicURL", "get object failed, error code: " + resp.code()
                        + "error message: " + resp.message());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
