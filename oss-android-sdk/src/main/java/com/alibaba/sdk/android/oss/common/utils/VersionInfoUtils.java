/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * <p>
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss.common.utils;

import android.os.Build;

import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSLog;

public class VersionInfoUtils {
    private static String userAgent = null;

    /*
     * UA sample : aliyun-sdk-java/2.0.5(Windows 7/6.1/amd64;1.7.0_55)/oss-import
     */
    public static String getUserAgent(String customInfo) {
        if (OSSUtils.isEmptyString(userAgent)) {
            userAgent = "aliyun-sdk-android/" + getVersion() + getSystemInfo();
        }

        if (OSSUtils.isEmptyString(customInfo)) {
            return userAgent;
        } else {
            return userAgent + "/" + customInfo;
        }
    }

    public static String getVersion() {
        return OSSConstants.SDK_VERSION;
    }


    /**
     * 获取系统+用户自定义的UA值,添加至最后位置
     *
     * @return
     */
    private static String getSystemInfo() {
        StringBuilder customUA = new StringBuilder();
        customUA.append("(");
        customUA.append(System.getProperty("os.name"));
        customUA.append("/Android " + Build.VERSION.RELEASE);
        customUA.append("/");
        //build may has chinese
        customUA.append(HttpUtil.urlEncode(Build.MODEL, OSSConstants.DEFAULT_CHARSET_NAME) + ";" + HttpUtil.urlEncode(Build.ID, OSSConstants.DEFAULT_CHARSET_NAME));
        customUA.append(")");
        String ua = customUA.toString();
        OSSLog.logDebug("user agent : " + ua);
        if (OSSUtils.isEmptyString(ua)) {
            String propertyUA = System.getProperty("http.agent");
            ua = propertyUA.replaceAll("[^\\p{ASCII}]", "?");
        }
        return ua;
    }
}
