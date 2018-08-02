package com.alibaba.sdk.android.oss.common;

import com.alibaba.sdk.android.oss.common.utils.HttpHeaders;

public interface OSSHeaders extends HttpHeaders {

    static final String OSS_PREFIX = "x-oss-";
    static final String OSS_USER_METADATA_PREFIX = "x-oss-meta-";

    static final String OSS_CANNED_ACL = "x-oss-acl";
    static final String STORAGE_CLASS = "x-oss-storage-class";
    static final String OSS_VERSION_ID = "x-oss-version-id";

    static final String OSS_HEADER_SYMLINK_TARGET = "x-oss-symlink-target";

    static final String OSS_HASH_SHA1 = "x-oss-hash-sha1";

    static final String OSS_SERVER_SIDE_ENCRYPTION = "x-oss-server-side-encryption";

    static final String GET_OBJECT_IF_MODIFIED_SINCE = "If-Modified-Since";
    static final String GET_OBJECT_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    static final String GET_OBJECT_IF_MATCH = "If-Match";
    static final String GET_OBJECT_IF_NONE_MATCH = "If-None-Match";

    static final String HEAD_OBJECT_IF_MODIFIED_SINCE = "If-Modified-Since";
    static final String HEAD_OBJECT_IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    static final String HEAD_OBJECT_IF_MATCH = "If-Match";
    static final String HEAD_OBJECT_IF_NONE_MATCH = "If-None-Match";

    static final String COPY_OBJECT_SOURCE = "x-oss-copy-source";
    static final String COPY_SOURCE_RANGE = "x-oss-copy-source-range";
    static final String COPY_OBJECT_SOURCE_IF_MATCH = "x-oss-copy-source-if-match";
    static final String COPY_OBJECT_SOURCE_IF_NONE_MATCH = "x-oss-copy-source-if-none-match";
    static final String COPY_OBJECT_SOURCE_IF_UNMODIFIED_SINCE = "x-oss-copy-source-if-unmodified-since";
    static final String COPY_OBJECT_SOURCE_IF_MODIFIED_SINCE = "x-oss-copy-source-if-modified-since";
    static final String COPY_OBJECT_METADATA_DIRECTIVE = "x-oss-metadata-directive";

    static final String OSS_HEADER_REQUEST_ID = "x-oss-request-id";

    static final String ORIGIN = "origin";
    static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    static final String ACCESS_CONTROL_REQUEST_HEADER = "Access-Control-Request-Headers";

    static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    static final String OSS_SECURITY_TOKEN = "x-oss-security-token";

    static final String OSS_NEXT_APPEND_POSITION = "x-oss-next-append-position";
    static final String OSS_HASH_CRC64_ECMA = "x-oss-hash-crc64ecma";
    static final String OSS_OBJECT_TYPE = "x-oss-object-type";
}
