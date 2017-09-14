package com.alibaba.sdk.android.oss.sample;

import android.content.Context;
import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
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


    private static final String testBucket = "<bucket_name>";
    private static final String downloadObject = "sampleObject";

    private static Context ctx;

    public EndpointSettingSamples(Context ctx) {
        this.ctx = ctx;
    }

    /*
    Public Cloud HTTP Endpoint
     */
    public void PublicEndpointSample() {

        String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";

        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider("<StsToken.AccessKeyId>", "<StsToken.SecretKeyId>", "<StsToken.SecurityToken>");

        OSSClient oss = new OSSClient(this.ctx, endpoint, credentialProvider);

        GetObjectRequest request = new GetObjectRequest(testBucket, downloadObject);

        doDownloadHelper(oss, request);
    }

    /*
    Public Cloud HTTPS Endpoint. The traffic will go with HTTPS.
     */
    public void PublicSecurityEndpointSample() {

        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";

        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider("<StsToken.AccessKeyId>", "<StsToken.SecretKeyId>", "<StsToken.SecurityToken>");

        OSSClient oss = new OSSClient(this.ctx, endpoint, credentialProvider);

        GetObjectRequest request = new GetObjectRequest(testBucket, downloadObject);

        doDownloadHelper(oss, request);
    }

    /*
    Use the CName.
     */
    public void CnameSample() {

        String endpoint = "http://cname.sample.com";

        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider("<StsToken.AccessKeyId>", "<StsToken.SecretKeyId>", "<StsToken.SecurityToken>");

        OSSClient oss = new OSSClient(this.ctx, endpoint, credentialProvider);

        GetObjectRequest request = new GetObjectRequest(testBucket, downloadObject);

        doDownloadHelper(oss, request);
    }

    /*
    If the endpoint is a VPC endpoint, it needs to be added to the cname excluded list.
     */
    public void VpcEndpointSample() {

        String endpoint = "http://vpc.endpoint.sample.com";

        OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider("<StsToken.AccessKeyId>", "<StsToken.SecretKeyId>", "<StsToken.SecurityToken>");

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
            // Download the file in the synchronous way, return the result.
            GetObjectResult getResult = oss.getObject(get);

            OSSLog.logDebug("Content-Length", "" + getResult.getContentLength());

            // Gets the file's input stream.
            InputStream inputStream = getResult.getObjectContent();

            byte[] buffer = new byte[2048];
            int len;

            while ((len = inputStream.read(buffer)) != -1) {
                // Process the downloaded data, here just print the total length.
                OSSLog.logDebug("syncGetObjectSample", "read length: " + len);
            }
            OSSLog.logDebug("syncGetObjectSample", "download success.");

            // Lookup object metadata---it's included in the getResult object.
            ObjectMetadata metadata = getResult.getMetadata();
            OSSLog.logDebug("ContentType", metadata.getContentType());


        } catch (ClientException e) {
            // client side exception, such as network exception
            e.printStackTrace();
        } catch (ServiceException e) {
            // OSS service side exception
            OSSLog.logError("RequestId", e.getRequestId());
            OSSLog.logError("ErrorCode", e.getErrorCode());
            OSSLog.logError("HostId", e.getHostId());
            OSSLog.logError("RawMessage", e.getRawMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
