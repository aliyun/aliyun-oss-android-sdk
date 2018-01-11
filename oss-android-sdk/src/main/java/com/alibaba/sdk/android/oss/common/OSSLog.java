package com.alibaba.sdk.android.oss.common;

import android.util.Log;

/**
 * Created by zhouzhuo on 11/22/15.
 */
public class OSSLog {

    private static final String TAG = "OSS-Android-SDK";
    private static boolean enableLog = false;

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
    public static void logInfo(String msg) {
        logInfo(msg, true);
    }

    public static void logInfo(String msg, boolean write2local) {
        if (enableLog) {
            Log.i(TAG, "[INFO]: ".concat(msg));
            log2Local(msg, write2local);
        }
    }

    /**
     * verbose level log
     *
     * @param msg
     */
    public static void logVerbose(String msg) {
        logVerbose(msg, true);
    }

    public static void logVerbose(String msg, boolean write2local) {
        if (enableLog) {
            Log.v(TAG, "[Verbose]: ".concat(msg));
            log2Local(msg, write2local);
        }
    }

    /**
     * warning level log
     *
     * @param msg
     */
    public static void logWarn(String msg) {
        logWarn(msg, true);
    }

    public static void logWarn(String msg, boolean write2local) {
        if (enableLog) {
            Log.w(TAG, "[Warn]: ".concat(msg));
            log2Local(msg, write2local);
        }
    }

    /**
     * debug level log
     *
     * @param msg
     */
    public static void logDebug(String msg) {
        logDebug(TAG, msg);
    }

    public static void logDebug(String tag, String msg) {
        logDebug(tag, msg, true);
    }

    /**
     * debug级别log
     *
     * @param write2local 是否需要写入本地
     * @param msg
     */
    public static void logDebug(String msg, boolean write2local) {
        logDebug(TAG, msg, write2local);
    }

    public static void logDebug(String tag, String msg, boolean write2local) {
        if (enableLog) {
            Log.d(tag, "[Debug]: ".concat(msg));
            log2Local(msg, write2local);
        }
    }

    /**
     * error level log
     *
     * @param msg
     */
    public static void logError(String msg) {
        logError(TAG, msg);
    }

    public static void logError(String tag, String msg) {
        logDebug(tag, msg, true);
    }

    /**
     * error级别log
     *
     * @param msg
     */
    public static void logError(String msg, boolean write2local) {
        logError(TAG, msg, write2local);
    }

    public static void logError(String tag, String msg, boolean write2local) {
        if (enableLog) {
            Log.d(tag, "[Error]: ".concat(msg));
            log2Local(msg, write2local);
        }
    }

    public static void logThrowable2Local(Throwable throwable) {
        if (enableLog) {
            OSSLogToFileUtils.getInstance().write(throwable);
        }
    }

    private static void log2Local(String msg, boolean write2local) {
        if (write2local) {
            OSSLogToFileUtils.getInstance().write(msg);
        }
    }

}
