package com.alibaba.sdk.android.mns.model.request;

import com.alibaba.sdk.android.mns.model.MNSRequest;

/**
 * Created by pan.zengp on 2016/7/30.
 */
public class ListQueueRequest extends MNSRequest {
    private String prefix;
    private Integer retNum;
    private String marker;

    /**
     * 列举队列
     *
     * @param prefix    队列名前缀
     * @param marker    列举的起始位置，""表示从第一个开始，也可以是前一次列举返回的marker
     * @param retNum    最多返回的个数
     */
    public ListQueueRequest(String prefix, Integer retNum, String marker){
        this.prefix = prefix;
        this.retNum = retNum;
        this.marker = marker;
    }

    public String getPrefix() {
        return prefix;
    }

    public Integer getRetNum() {
        return retNum;
    }

    public String getMarker() {
        return marker;
    }
}
