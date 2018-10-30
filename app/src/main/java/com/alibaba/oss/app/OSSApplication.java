package com.alibaba.oss.app;

import android.app.Application;

public class OSSApplication extends Application {
    private static Application sApplication;

    public static Application getInstance(){
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }
}
