package com.alibaba.sdk.android.oss.common;

import android.util.Log;

public class DefaultLogPrinter implements LogPrinter {

    private static final String TAG = "OSS-Android-SDK";

    @Override
    public void log(LogLevel level, String message) {
        switch (level) {
            case INFO:
                Log.i(TAG, "[INFO]: ".concat(message));
                break;
            case VERBOSE:
                Log.v(TAG, "[VERBOSE]: ".concat(message));
                break;
            case WARN:
                Log.w(TAG, "[WARN]: ".concat(message));
                break;
            case DEBUG:
                Log.d(TAG, "[DEBUG]: ".concat(message));
                break;
            case ERROR:
                Log.e(TAG, "[ERROR]: ".concat(message));
                break;
        }
    }
}
