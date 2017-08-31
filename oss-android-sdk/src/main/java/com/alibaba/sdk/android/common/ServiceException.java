/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.common;

/**
 * <p>
 * This class means OSS got the request but returned an error message.
 * </p>
 * 
 * <p>
 * {@link ServiceException} is for the errors occurred in OSS server side, such as the Access ID for authentication does not exist.
 * The error code in this class is very help for troubleshooting.
 * </p>
 * 
 * <p>
 * {@link ClientException} means errors occur before OSS receives the request, or after getting the response from OSS.
 * For example, when the network is unavailable, the {@link ClientException} is thrown.
 * </p>
 * 
 * <p>
 * Generally speaking, the caller must to handle {@link ServiceException} as that exception is sometimes expected and needs client's attention.
 * The error code is very helpful for troubleshooting.
 * </p>
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = 430933593095358673L;

    /**
     * http status code
     */
    private int statusCode;

    /**
     * OSS error code，checks out this：http://help.aliyun.com/document_detail/oss/api-reference/error-response.html
     */
    private String errorCode;

    /**
     * OSS request Id, this is a globally unique Id
     */
    private String requestId;

    /**
     * The OSS host Id returned from the server
     */
    private String hostId;

    /**
     * The raw xml message in the response
     */
    private String rawMessage;


    /**
     * Creates an instance of ServiceException with specified status code, message, error code, requestId, hostId and raw message.
     * All these information are from the OSS response.
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
     * Gets the status code
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the error code in string
     * @return The error code in string.
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
     * Gets Host Id
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
     * @return The raw HTTP response
     */
    public String getRawMessage() {
        return rawMessage;
    }
}
