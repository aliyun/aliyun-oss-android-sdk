package com.alibaba.sdk.android.oss.common;

import android.util.Log;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSLog {

    private static final String TAG = "OSS-Android-SDK";
    private static boolean enableLog = false;

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
    public static void logInfo(String msg) {
        if (enableLog) {
            Log.i(TAG, msg);
            OSSLogToFileUtils.getInstance().write(msg);
        }
    }

    /**
     * verbose级别log
     *
     * @param msg
     */
    public static void logVerbose(String msg) {
        if (enableLog) {
            Log.v(TAG, msg);
            OSSLogToFileUtils.getInstance().write(msg);
        }
    }

    /**
     * warning级别log
     *
     * @param msg
     */
    public static void logWarn(String msg) {
        if (enableLog) {
            Log.w(TAG, msg);
            OSSLogToFileUtils.getInstance().write(msg);
        }
    }

    /**
     * debug级别log
     *
     * @param msg
     */
    public static void logDebug(String msg) {
        logDebug(msg,true);
    }

    /**
     * debug级别log
     * @param write2local 是否需要写入本地
     * @param msg
     */
    public static void logDebug(String msg, boolean write2local) {
        if (enableLog) {
            Log.d(TAG, msg);
        }
        if(write2local) {
            OSSLogToFileUtils.getInstance().write(msg);
        }
    }

    /**
     * error级别log
     *
     * @param msg
     */
    public static void logError(String msg) {
        logError(msg,true);
    }

    /**
     * error级别log
     *
     * @param msg
     */
    public static void logError(String msg, boolean write2local) {
        if (enableLog) {
            Log.e(TAG, msg);
        }
        if(write2local) {
            OSSLogToFileUtils.getInstance().write(msg);
        }
    }

    public static void logThrowable2Local(Throwable throwable){
        if (enableLog) {
            OSSLogToFileUtils.getInstance().write(throwable);
        }
    }


}
