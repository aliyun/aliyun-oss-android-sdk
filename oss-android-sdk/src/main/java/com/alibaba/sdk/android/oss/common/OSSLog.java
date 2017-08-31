package com.alibaba.sdk.android.oss.common;

import android.util.Log;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSLog {

    private static final String TAG = "OSS-Android-SDK";
    private static boolean enableLog;

    /**
     * enable log
     */
    public static void enableLog() {
        enableLog = true;
    }

    /**
     * disable log
     */
    public static void disableLog() {
        enableLog = false;
    }

    /**
     * @return return log flag
     */
    public static boolean isEnableLog() {
        return enableLog;
    }

    /**
     * info level log
     *
     * @param msg
     */
    public static void logI(String msg) {
        if (enableLog) {
            Log.i(TAG, msg);
        }
    }

    /**
     * verbose level log
     *
     * @param msg
     */
    public static void logV(String msg) {
        if (enableLog) {
            Log.v(TAG, msg);
        }
    }

    /**
     * warning level log
     *
     * @param msg
     */
    public static void logW(String msg) {
        if (enableLog) {
            Log.w(TAG, msg);
        }
    }

    /**
     * debug level log
     *
     * @param msg
     */
    public static void logD(String msg) {
        if (enableLog) {
            Log.d(TAG, msg);
        }
    }

    /**
     * error level log
     *
     * @param msg
     */
    public static void logE(String msg) {
        if (enableLog) {
            Log.e(TAG, msg);
        }
    }
}
