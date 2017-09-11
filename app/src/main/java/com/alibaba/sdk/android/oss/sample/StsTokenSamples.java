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
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created by jingdan on 2017/8/31.
 * need to https://help.aliyun.com/document_detail/28787.html?spm=5176.doc28756.6.705.iE1EVJ this site
 * download sts java sdk
 */
public class StsTokenSamples {

    /**
     * 根据本地server的ip和端口进行配置
     */
    public static final String STS_SERVER_API = "http://xx.xx.xx.xx:12555/sts/getsts";

    //建议sts的token获取等放在服务器端进行获取对提高安全性
    public void getStsTokenAndSet(final OSSStsTokenCredentialProvider provider, final Handler handler){
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

                        //设置ak,sk,sts_token
                        provider.setAccessKeyId(stsModel.Credentials.AccessKeyId);
                        provider.setSecretKeyId(stsModel.Credentials.AccessKeySecret);
                        provider.setSecurityToken(stsModel.Credentials.SecurityToken);

                        Message msg = Message.obtain();
                        msg.obj = stsModel;
                        msg.what = MainActivity.STS_TOKEN_SUC;
                        handler.sendMessage(msg);
                    }else{
                        Log.d("stsSamples",responseCode+"");
                        handler.sendEmptyMessage(MainActivity.FAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(MainActivity.FAIL);
                }


            }
        }.start();
    }
}
