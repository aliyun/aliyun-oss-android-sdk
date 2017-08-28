/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss.common.utils;

import android.os.Build;

import com.alibaba.sdk.android.oss.common.OSSConstants;

public class VersionInfoUtils {
    private static String userAgent = null;
    

    public static String getUserAgent() {
        if (userAgent == null) {
            userAgent = "aliyun-sdk-android/" + getVersion() + "/" + getDefaultUserAgent();
        }
        return userAgent;
    }

    public static String getVersion() {
        return OSSConstants.SDK_VERSION;
    }

    /**
     * 获取系统UA值
     *
     * @return
     */
    public static String getDefaultUserAgent() {
        String result = System.getProperty("http.agent");
        if (OSSUtils.isEmptyString(result)) {
            result = "("+System.getProperty("os.name")+"/Android "  + Build.VERSION.RELEASE + "/" +
                    Build.MODEL+"/"+Build.ID+")";
        }
        return result.replaceAll("[^\\p{ASCII}]", "?");
    }

}
