package com.alibaba.sdk.android.mns.common;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import android.util.Log;

public class MNSLog {

    private static final String TAG = "MNS-Android-SDK";
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

