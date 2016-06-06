package com.alibaba.sdk.android.oss.sample;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhouzhuo on 6/6/16.
 */
public class EndpointSettingSamples {

    // 运行sample前需要配置以下字段为有效的值
    private static final String accessKeyId = "**************";
    private static final String accessKeySecret = "*******************";

    private static final String testBucket = "<bucket_name>";
    private static final String downloadObject = "sampleObject";

    private static Context ctx;

    public EndpointSettingSamples(Context ctx) {
        this.ctx = ctx;
    }

    /*
    普通公有云Endpoint
     */
    public void PublicEndpointSample() {

        String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";

        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);

        OSSClient oss = new OSSClient(this.ctx, endpoint, credentialProvider);

        GetObjectRequest request = new GetObjectRequest(testBucket, downloadObject);

        doDownloadHelper(oss, request);
    }

    /*
    Endpoint使用https前缀，请求走HTTPS安全链路
     */
    public void PublicSecurityEndpointSample() {

        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";

        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);

        OSSClient oss = new OSSClient(this.ctx, endpoint, credentialProvider);

        GetObjectRequest request = new GetObjectRequest(testBucket, downloadObject);

        doDownloadHelper(oss, request);
    }

    /*
    如果使用Cname作为Endpoint，直接设置即可
     */
    public void CnameSample() {

        String endpoint = "http://cname.sample.com";

        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);

        OSSClient oss = new OSSClient(this.ctx, endpoint, credentialProvider);

        GetObjectRequest request = new GetObjectRequest(testBucket, downloadObject);

        doDownloadHelper(oss, request);
    }

    /*
    如果使用非标Endpoint，需要在构造OSSclient的conf中设置cnameExcludeList
     */
    public void VpcEndpointSample() {

        String endpoint = "http://vpc.endpoint.sample.com";

        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);

        ClientConfiguration conf = ClientConfiguration.getDefaultConf();
        List cnameExclude = new ArrayList();
        cnameExclude.add("vpc.endpoint.sample.com");
        conf.setCustomCnameExcludeList(cnameExclude);

        OSSClient oss = new OSSClient(this.ctx, endpoint, credentialProvider, conf);

        GetObjectRequest request = new GetObjectRequest(testBucket, downloadObject);

        doDownloadHelper(oss, request);
    }

    private void doDownloadHelper(OSSClient oss, GetObjectRequest get) {

        try {
            // 同步执行下载请求，返回结果
            GetObjectResult getResult = oss.getObject(get);

            Log.d("Content-Length", "" + getResult.getContentLength());

            // 获取文件输入流
            InputStream inputStream = getResult.getObjectContent();

            byte[] buffer = new byte[2048];
            int len;

            while ((len = inputStream.read(buffer)) != -1) {
                // 处理下载的数据，比如图片展示或者写入文件等
                Log.d("asyncGetObjectSample", "read length: " + len);
            }
            Log.d("asyncGetObjectSample", "download success.");

            // 下载后可以查看文件元信息
            ObjectMetadata metadata = getResult.getMetadata();
            Log.d("ContentType", metadata.getContentType());


        } catch (ClientException e) {
            // 本地异常如网络异常等
            e.printStackTrace();
        } catch (ServiceException e) {
            // 服务异常
            Log.e("RequestId", e.getRequestId());
            Log.e("ErrorCode", e.getErrorCode());
            Log.e("HostId", e.getHostId());
            Log.e("RawMessage", e.getRawMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
