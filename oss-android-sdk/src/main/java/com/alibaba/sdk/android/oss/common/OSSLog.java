package com.alibaba.sdk.android.oss.common;

import android.util.Log;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSLog {

    private static final String TAG = "OSS-Android-SDK";
    private static boolean enableLog;

    /**
     * 打开log观察调试信息
     */
    public static void enableLog() {
        enableLog = true;
    }

    /**
     * 打开log观察调试信息
     */
    public static void disableLog() {
        enableLog = false;
    }

    /**
     * @return 是否打开了log
     */
    public static boolean isEnableLog() {
        return enableLog;
    }

    /**
     * info级别log
     *
     * @param msg
     */
    public static void logI(String msg) {
        if (enableLog) {
            Log.i(TAG, msg);
        }
    }

    /**
     * verbose级别log
     *
     * @param msg
     */
    public static void logV(String msg) {
        if (enableLog) {
            Log.v(TAG, msg);
        }
    }

    /**
     * warning级别log
     *
     * @param msg
     */
    public static void logW(String msg) {
        if (enableLog) {
            Log.w(TAG, msg);
        }
    }

    /**
     * debug级别log
     *
     * @param msg
     */
    public static void logD(String msg) {
        if (enableLog) {
            Log.d(TAG, msg);
        }
    }

    /**
     * error级别log
     *
     * @param msg
     */
    public static void logE(String msg) {
        if (enableLog) {
            Log.e(TAG, msg);
        }
    }
}
