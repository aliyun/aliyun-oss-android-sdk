package com.alibaba.sdk.android.oss.sample;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.sdk.android.oss.app.MainActivity;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.OSSUtils;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;

/**
 * Created by jingdan on 2017/8/31.
 * need to https://help.aliyun.com/document_detail/28787.html?spm=5176.doc28756.6.705.iE1EVJ this site
 * download sts java sdk
 */
public class StsTokenSamples {

    //建议sts的token获取等放在服务器端进行获取对提高安全性
    public void getStsTokenAndSet(final OSSStsTokenCredentialProvider provider, final Handler handler){
        new Thread(){
            @Override
            public void run() {
                // 通过管理控制后台-访问控制
                // 只有 RAM用户（子账号）才能调用 AssumeRole 接口
                // 阿里云主账号的AccessKeys不能用于发起AssumeRole请求
                // 请首先在RAM控制台创建一个RAM用户，并为这个用户创建AccessKeys
                // 对子账户需要设置AliyunSTSAssumeRoleAccess权限
                String accessKeyId = "***********************";
                String accessKeySecret = "***********************";

                // RoleArn 需要在 RAM 控制台上获取
                String roleArn = "***********************";

                // RoleSessionName 是临时Token的会话名称，自己指定用于标识你的用户，主要用于审计，或者用于区分Token颁发给谁
                // 但是注意RoleSessionName的长度和规则，不要有空格，只能有'-' '.' '@' 字母和数字等字符
                // 具体规则请参考API文档中的格式要求
                String roleSessionName = "alice-001";

                //OSS Policy settings
                //can read https://help.aliyun.com/document_detail/56288.html
                //case https://help.aliyun.com/knowledge_detail/39717.html?spm=5176.product28625.6.735.5etPTf
                //case https://help.aliyun.com/knowledge_detail/39712.html?spm=5176.7739717.6.729.aZiRgD
                String policy = "{\n" +
                        "    \"Version\": \"1\", \n" +
                        "    \"Statement\": [\n" +
                        "        {\n" +
                        "            \"Action\": [\n" +
                        "                \"oss:*\"\n" +
                        "            ], \n" +
                        "            \"Resource\": [\n" +
                        "                \"acs:oss:*:*:*\" \n" +
                        "            ], \n" +
                        "            \"Effect\": \"Allow\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";

                // 此处必须为 HTTPS
                ProtocolType protocolType = ProtocolType.HTTPS;

                try {
                    final AssumeRoleResponse response = OSSUtils.assumeRole(accessKeyId, accessKeySecret,
                            roleArn, roleSessionName, policy, protocolType);

                    //设置ak,sk,sts_token
                    provider.setAccessKeyId(response.getCredentials().getAccessKeyId());
                    provider.setSecretKeyId(response.getCredentials().getAccessKeySecret());
                    provider.setSecurityToken(response.getCredentials().getSecurityToken());

                    Message msg = Message.obtain();
                    msg.obj = response;
                    msg.what = MainActivity.STS_TOKEN_SUC;
                    handler.sendMessage(msg);

                    Log.d("StsTokenSamples","Expiration: " + response.getCredentials().getExpiration());
                    Log.d("StsTokenSamples","Access Key Id: " + response.getCredentials().getAccessKeyId());
                    Log.d("StsTokenSamples","Access Key Secret: " + response.getCredentials().getAccessKeySecret());
                    Log.d("StsTokenSamples","Security Token: " + response.getCredentials().getSecurityToken());
                } catch (ClientException e) {
                    Log.d("StsTokenSamples","Failed to get a token.");
                    Log.d("StsTokenSamples","Error code: " + e.getErrCode());
                    Log.d("StsTokenSamples","Error message: " + e.getErrMsg());
                    handler.sendEmptyMessage(MainActivity.FAIL);
                }
            }
        }.start();
    }
}
