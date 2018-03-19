package com.alibaba.sdk.android.oss.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jingdan on 2017/12/6.
 */

public class OSSSharedPreferences {

    private static OSSSharedPreferences sInstance;
    private SharedPreferences mSp;

    private OSSSharedPreferences(Context context) {
        mSp = context.getSharedPreferences("oss_android_sdk_sp", Context.MODE_PRIVATE);
    }


    public static OSSSharedPreferences instance(Context context) {
        if (sInstance == null) {
            synchronized (OSSSharedPreferences.class) {
                if (sInstance == null) {
                    sInstance = new OSSSharedPreferences(context);
                }
            }
        }
        return sInstance;
    }

    public void setStringValue(String key, String value) {
        SharedPreferences.Editor edit = mSp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public String getStringValue(String key) {
        return mSp.getString(key, "");
    }

    public void removeKey(String key) {
        SharedPreferences.Editor edit = mSp.edit();
        edit.remove(key);
        edit.commit();
    }

    public boolean contains(String key) {
        return mSp.contains(key);
    }
}
