package com.alibaba.sdk.android.oss.app;

import android.os.Environment;

import java.io.File;

/**
 * Created by wangzheng on 2017/11/22.
 */

public class Config {

    // To run the sample correctly, the following variables must have valid values.
    // The endpoint value below is just the example. Please use proper value according to your region

    // 访问的endpoint地址
    public static final String endpoint = "http://oss-cn-region.aliyuncs.com";
    //callback 测试地址
    public static final String callbackAddress = "http://oss-demo.aliyuncs.com:23450";
    // STS 鉴权服务器地址，使用前请参照文档 https://help.aliyun.com/document_detail/31920.html 介绍配置STS 鉴权服务器地址。
    // 或者根据工程sts_local_server目录中本地鉴权服务脚本代码启动本地STS 鉴权服务器。详情参见sts_local_server 中的脚本内容。
    public static final String STSSERVER = "http://0.0.0.0:0000/sts/getsts";//STS 地址

    public static final String uploadFilePath = ""; //本地文件上传地址
    public static final String bucket = "测试bucket名称";
    public static final String uploadObject = "上传object名称";
    public static final String downloadObject = "下载object名称";
}
