package com.alibaba.sdk.android.oss.signer;

import static com.alibaba.sdk.android.oss.common.RequestParameters.PART_NUMBER;
import static com.alibaba.sdk.android.oss.common.RequestParameters.POSITION;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CACHE_CONTROL;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CONTENT_DISPOSITION;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CONTENT_ENCODING;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CONTENT_LANGUAGE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_CONTENT_TYPE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.RESPONSE_HEADER_EXPIRES;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SECURITY_TOKEN;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_ACL;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_APPEND;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_BUCKETINFO;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_CORS;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_DELETE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_LIFECYCLE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_LOCATION;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_LOGGING;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_OBJECT_META;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_REFERER;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_SEQUENTIAL;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_UPLOADS;
import static com.alibaba.sdk.android.oss.common.RequestParameters.SUBRESOURCE_WEBSITE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.UPLOAD_ID;
import static com.alibaba.sdk.android.oss.common.RequestParameters.X_OSS_PROCESS;
import static com.alibaba.sdk.android.oss.common.RequestParameters.X_OSS_RESTORE;
import static com.alibaba.sdk.android.oss.common.RequestParameters.X_OSS_SYMLINK;
import static com.alibaba.sdk.android.oss.common.RequestParameters.X_OSS_TAGGING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignParameters {
    public static final String AUTHORIZATION_PREFIX = "OSS ";
    public static final String NEW_LINE = "\n";

    public static final List<String> SIGNED_PARAMTERS = Arrays.asList(new String[]{
        SUBRESOURCE_BUCKETINFO, SUBRESOURCE_ACL, SUBRESOURCE_UPLOADS, SUBRESOURCE_LOCATION,
                SUBRESOURCE_CORS, SUBRESOURCE_LOGGING, SUBRESOURCE_WEBSITE,
                SUBRESOURCE_REFERER, SUBRESOURCE_LIFECYCLE, SUBRESOURCE_DELETE,
                SUBRESOURCE_APPEND, UPLOAD_ID, PART_NUMBER, SECURITY_TOKEN, POSITION,
                RESPONSE_HEADER_CACHE_CONTROL, RESPONSE_HEADER_CONTENT_DISPOSITION,
                RESPONSE_HEADER_CONTENT_ENCODING, RESPONSE_HEADER_CONTENT_LANGUAGE,
                RESPONSE_HEADER_CONTENT_TYPE, RESPONSE_HEADER_EXPIRES, X_OSS_PROCESS,
                SUBRESOURCE_SEQUENTIAL, X_OSS_SYMLINK, X_OSS_RESTORE, X_OSS_TAGGING, SUBRESOURCE_OBJECT_META
    });
}
