package com.alibaba.sdk.android.oss.common.utils;

import java.util.HashMap;

/**
 * Created by wangzheng on 2018/7/12.
 */

public class CaseInsensitiveHashMap<k,v> extends HashMap<k,v> {
    @Override
    //只能做到小写和驼峰兼容。
    public v get(Object key) {
        //兼容http2.0
        if (key != null && !containsKey(key) && key instanceof String){
            String lowCaseKey = ((String) key).toLowerCase();
            if (containsKey(lowCaseKey)){
                return get(lowCaseKey);
            }else{
                return null;
            }
        }
        return super.get(key);
    }


}