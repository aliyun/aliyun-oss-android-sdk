package com.alibaba.sdk.android.oss.model;


/**
 * @author: zhouzhuo
 * 2014年11月3日
 */
public class Range {

    public static final long INFINITE = -1;
    /**
     * The start point of the download range
     */
    private long begin;
    /**
     * The end point of the download range
     */
    private long end;

    /**
     * Constructor
     *
     * @param begin The start index
     * @param end   The end index
     */
    public Range(long begin, long end) {
        setBegin(begin);
        setEnd(end);
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
