package com.alibaba.sdk.android.oss.sample;


import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ClientException;
import java.io.IOException;
import android.util.Log;

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

    public SignURLSamples(OSS client, String testBucket, String testObject, String uploadFilePath) {
        this.oss = client;
        this.testBucket = testBucket;
        this.testObject = testObject;
    }

    // If the bucket is private, the signed URL is required for the access.
    // Expiration time is specified in the signed URL.
    public void presignConstrainedURL() {
        try {
            // Gets the signed url, the expiration time is 5 minute
            String url = oss.presignConstrainedObjectURL(testBucket, testObject, 5 * 60);
            Log.d("signContrainedURL", "get url: " + url);
            // access it with the url
            Request request = new Request.Builder().url(url).build();
            Response resp = new OkHttpClient().newCall(request).execute();
            if (resp.code() == 200) {
                Log.d("signContrainedURL", "object size: " + resp.body().contentLength());
            } else {
                Log.e("signContrainedURL", "get object failed, error code: " + resp.code()
                        + "error message: " + resp.message());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ClientException e) {
            e.printStackTrace();
        }
    }


    // If the bucket is public, the URL does not need sign and no need to set the expiration time
    public void presignPublicURL() {
        try {
            // Gets the url, no expiration time
            String url = oss.presignPublicObjectURL(testBucket, testObject);
            Log.d("signPublicURL", "get url: " + url);
            // gets the object via the url
            Request request = new Request.Builder().url(url).build();
            Response resp = new OkHttpClient().newCall(request).execute();
            if (resp.code() == 200) {
                Log.d("signPublicURL", "get object size: " + resp.body().contentLength());
            } else {
                Log.e("signPublicURL", "get object failed, error code: " + resp.code()
                        + "error message: " + resp.message());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
