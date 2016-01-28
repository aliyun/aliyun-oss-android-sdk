package com.alibaba.sdk.android.oss.model;


/**
 * @author: zhouzhuo
 * 2014年11月3日
 */
public class Range {

    /**
     * 下载范围的起点
     */
    private long begin;

    /**
     * 下载范围的终点
     */
    private long end;

    public static final long INFINITE = -1;

    /**
     *  构造新的下载范围
     * @param begin 下载范围起点
     * @param end 下载范围终点
     */
    public Range(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public boolean checkIsValid() {
        if (begin < -1 || end < -1) {
            return false;
        }
        if (begin >= 0 && end >= 0 && begin > end) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "bytes=" + (begin == -1 ? "" : String.valueOf(begin)) + "-" + (end == -1 ? "" : String.valueOf(end));
    }
}
