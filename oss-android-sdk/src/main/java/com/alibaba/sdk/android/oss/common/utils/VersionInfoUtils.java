/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss.common.utils;

import android.os.Build;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.common.OSSConstants;

public class VersionInfoUtils {
    private static String userAgent = null;
    

    public static String getUserAgent(String customUserAgent) {
        if (userAgent == null) {
            userAgent = "aliyun-sdk-android/" + getVersion() + "/" + getCustomUserAgent(customUserAgent);
        }
        return userAgent;
    }

    public static String getVersion() {
        return OSSConstants.SDK_VERSION;
    }


    /**
     * 获取系统+用户自定义的UA值,添加至最后位置
     * @return
     */
    public static String getCustomUserAgent(String customUserAgent) {
        String result = System.getProperty("http.agent");
        if (OSSUtils.isEmptyString(result)) {
            result = System.getProperty("java.vm.name") + "/" + System.getProperty("java.vm.version")
                    + " (Android "  + Build.VERSION.RELEASE + "; " +
                    Build.MODEL + " Build/" + Build.ID + ")";
        }
        if(!TextUtils.isEmpty(customUserAgent)){
            result = result.concat("/").concat(customUserAgent);
        }
        return result.replaceAll("[^\\p{ASCII}]", "?");
    }

}
