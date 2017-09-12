package com.alibaba.sdk.android.oss.sample;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.sdk.android.oss.app.MainActivity;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.alibaba.sdk.android.oss.model.StsModel;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created by jingdan on 2017/8/31.
 */
public class StsTokenSamples {

    private WeakReference<Handler> handler;


    public StsTokenSamples(Handler handler){
        this.handler = new WeakReference<>(handler);
    }

    /**
     * config by local ip and port
     */
    public static final String STS_SERVER_API = "http://0.0.0.0:12555/sts/getsts";

    //建议sts的token获取等放在服务器端进行获取对提高安全性
    public void getStsTokenAndSet(){
        new Thread(){
            @Override
            public void run() {
                try {
                    URL stsUrl = new URL(STS_SERVER_API);
                    HttpURLConnection conn = (HttpURLConnection) stsUrl.openConnection();
                    conn.setConnectTimeout(3000);
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    if(responseCode == 200) {
                        InputStream inputStream = conn.getInputStream();
                        String result = IOUtils.readStreamAsString(inputStream, "utf-8");
                        Gson gson = new Gson();
                        StsModel stsModel = gson.fromJson(result, StsModel.class);

                        Message msg = Message.obtain();
                        msg.obj = stsModel;
                        msg.what = MainActivity.STS_TOKEN_SUC;
                        StsTokenSamples.this.handler.get().sendMessage(msg);
                    }else{
                        Log.d("stsSamples",responseCode+"");
                        StsTokenSamples.this.handler.get().sendEmptyMessage(MainActivity.FAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    StsTokenSamples.this.handler.get().sendEmptyMessage(MainActivity.FAIL);
                }


            }
        }.start();
    }
}
