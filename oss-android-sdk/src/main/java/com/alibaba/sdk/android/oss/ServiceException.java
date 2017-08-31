/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss;

/**
 * <p>
 * The OSS server side exception class definition.
 * </p>
 *
 * <p>
 * {@link ClientException} means there're errors occurred when sending request to OSS or parsing the response from OSS.
 * For example when the network is unavailable, this exception will be thrown.
 * </p>
 *
 * <p>
 * {@link ServiceException} means there're errors occurred in OSS service side. For example, the Access Id
 * does not exist for authentication, then {@link ServiceException} or its subclass is thrown.
 * The ServiceException has the error code for the caller to have some specific handling.
 * </p>
 *
 * <p>
 * Generally speaking, the caller only needs to handle {@link ServiceException} as it means the request
 * has reached OSS, but there're some errors occurred. This error in most of cases are expected due to
 * wrong parameters int the request or some wrong settings in user's account. The error code is very helpful
 * for troubleshooting.
 * </p>
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = 430933593095358673L;

    /**
     * http status code
     */
    private int statusCode;

    /**
     * OSS error code, check out：http://help.aliyun.com/document_detail/oss/api-reference/error-response.html
     */
    private String errorCode;

    /**
     * OSS request Id
     */
    private String requestId;

    /**
     * The OSS host Id which is same as the one in the request
     */
    private String hostId;

    /**
     * The raw message in the response
     */
    private String rawMessage;


    /**
     * The constructor with status code, message, error code , request Id and host Id
     * @param statusCode HTTP status code
     * @param message error message
     * @param errorCode error code
     * @param requestId Request ID
     * @param hostId Host ID
     */
    public ServiceException(int statusCode, String message,
            String errorCode, String requestId, String hostId, String rawMessage){

        super(message);

        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.requestId = requestId;
        this.hostId = hostId;
        this.rawMessage = rawMessage;
    }

    /**
     * Gets the http status code
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the error code
     * @return error code in string
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the request Id
     * @return Request Id
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Gets the host Id
     * @return Host Id
     */
    public String getHostId() {
        return hostId;
    }

    @Override
    public String toString() {
        return "[StatusCode]: " + statusCode + ", "
                + "[Code]: " + getErrorCode() + ", "
                + "[Message]: " + getMessage() + ", "
                + "[Requestid]: " + getRequestId() + ", "
                + "[HostId]: " + getHostId();
    }

    /**
     * @return The raw message
     */
    public String getRawMessage() {
        return rawMessage;
    }
}
