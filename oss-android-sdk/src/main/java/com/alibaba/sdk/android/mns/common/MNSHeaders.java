package com.alibaba.sdk.android.mns.common;

/**
 * Created by pan.zengp on 2016/7/27.
 */
import com.alibaba.sdk.android.common.utils.HttpHeaders;
public interface MNSHeaders extends HttpHeaders{
    public static final String MNS_PREFIX = "x-mns-";
    public static final String MNS_HEADER_REQUEST_ID = "x-mns-request-id";
    public static final String MNS_SECURITY_TOKEN = "x-mns-security-token";
    public static final String MNS_META_OVERRIDE = "metaoverride";
    public static final String MNS_QUEUE_PREFIX = "x-mns-prefix";
    public static final String MNS_MARKER = "x-mns-marker";
    public static final String MNS_RET_NUMBERS = "x-mns-ret-number";
    public static final String MNS_WITH_META = "x-mns-with-meta";
    public static final String MNS_PEEK_ONLY = "peekonly";
}
