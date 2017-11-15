package com.alibaba.sdk.android.oss.common.auth;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jingdan on 2017/11/15.
 * Authentication server issued under the agreement of the official website agreement, you can directly use the provider
 */

public class OSSAuthCredentialsProvider extends OSSFederationCredentialProvider {

    private String mAuthServerUrl;

    public OSSAuthCredentialsProvider(String authServerUrl){
       this.mAuthServerUrl = authServerUrl;
    }

    @Override
    public OSSFederationToken getFederationToken() throws ClientException{
        OSSFederationToken authToken;
        String stsJson;
        try {
            URL stsUrl = new URL(mAuthServerUrl);
            HttpURLConnection conn = (HttpURLConnection) stsUrl.openConnection();
            InputStream input = conn.getInputStream();
            stsJson = IOUtils.readStreamAsString(input, OSSConstants.DEFAULT_CHARSET_NAME);
            JSONObject jsonObj = new JSONObject(stsJson);
            int statusCode = jsonObj.getInt("StatusCode");
            if (statusCode == 200) {
                String ak = jsonObj.getString("AccessKeyId");
                String sk = jsonObj.getString("AccessKeySecret");
                String token = jsonObj.getString("SecurityToken");
                String expiration = jsonObj.getString("Expiration");
                authToken =  new OSSFederationToken(ak, sk, token, expiration);
            } else {
                String errorCode = jsonObj.getString("ErrorCode");
                String errorMessage = jsonObj.getString("ErrorMessage");
                throw new ClientException("ErrorCode: " + errorCode + "| ErrorMessage: " + errorMessage);
            }
            return authToken;
        }catch (Exception e){
            throw new ClientException(e);
        }
    }
}
