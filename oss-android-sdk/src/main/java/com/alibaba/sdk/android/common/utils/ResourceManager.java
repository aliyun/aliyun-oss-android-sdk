/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */
package com.alibaba.sdk.android.common.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Manager class to get localized resources.
 */
public class ResourceManager {
    private ResourceBundle bundle;

    ResourceManager(String baseName, Locale locale){
        this.bundle = ResourceBundle.getBundle(baseName, locale);
    }

    public static ResourceManager getInstance(String baseName){
        return new ResourceManager(baseName, Locale.getDefault());
    }

    public static ResourceManager getInstance(String baseName, Locale locale){
        return new ResourceManager(baseName, locale);
    }

    public String getString(String key){
        return bundle.getString(key);
    }

    public String getFormattedString(String key, Object... args){
        return MessageFormat.format(getString(key), args);
    }
}
