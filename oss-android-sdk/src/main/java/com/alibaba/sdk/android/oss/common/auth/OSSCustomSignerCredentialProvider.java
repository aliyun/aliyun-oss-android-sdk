package com.alibaba.sdk.android.oss.common.auth;

/**
 * Created by zhouzhuo on 11/4/15.
 */
public abstract class OSSCustomSignerCredentialProvider extends OSSCredentialProvider {
    /**
     * 自定义的加签函数，考虑到移动端不适合保存AcessKeyId/AccessKeySecret在本地，那么可以在业务server上进行加签。
     * 比如，在这个函数中，把Content POST到业务server，业务server用保存的AccessKeyId/AccessKeySecret进行加签后，返回签名结果。
     *
     * 签名算法参考：http://help.aliyun.com/document_detail/oss/api-reference/access-control/signature-header.html
     *
     * content是已经根据请求各个参数拼接后的字符串，所以算法为：
     *      signature = "OSS " + AccessKeyId + ":" + base64(hmac-sha1(AccessKeySecret, content))
     *
     * @param content 根据请求各个参数拼接后的字符串
     * @return "OSS " + AccessKeyId + ":" + base64(hmac-sha1(AccessKeySecret, content))
     */
    public abstract String signContent(String content);
}
