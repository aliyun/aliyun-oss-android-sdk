/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.common;

/**
 * <p>
 * 表示阿里云服务返回的错误消息。
 * </p>
 * 
 * <p>
 * {@link ServiceException}用于处理阿里云服务返回的错误消息。比如，用于身份验证的Access ID不存在，
 * 则会抛出{@link ServiceException}（严格上讲，会是该类的一个继承类。比如，OSSClient会抛出OSSException）。
 * 异常中包含了错误代码，用于让调用者进行特定的处理。
 * </p>
 * 
 * <p>
 * {@link ClientException}表示的则是在向阿里云服务发送请求时出现的错误，以及客户端无法处理返回结果。
 * 例如，在发送请求时网络连接不可用，则会抛出{@link ClientException}的异常。
 * </p>
 * 
 * <p>
 * 通常来讲，调用者只需要处理{@link ServiceException}。因为该异常表明请求被服务处理，但处理的结果表明
 * 存在错误。异常中包含了细节的信息，特别是错误代码，可以帮助调用者进行处理。
 * </p>
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = 430933593095358673L;

    /**
     * http状态码
     */
    private int statusCode;

    /**
     * OSS错误码，参考：http://help.aliyun.com/document_detail/oss/api-reference/error-response.html
     */
    private String errorCode;

    /**
     * OSS请求的唯一标示ID
     */
    private String requestId;

    /**
     * 用于标识访问的OSS集群，与用户请求时使用的Host一致。
     */
    private String hostId;

    /**
     * 返回的原始xml信息
     */
    private String rawMessage;


    /**
     * 用异常消息和表示异常原因及其他信息的对象构造新实例。
     * @param statusCode HTTP状态码
     * @param message 异常信息。
     * @param errorCode 错误代码。
     * @param requestId Request ID。
     * @param hostId Host ID。
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
     * 发生错误的http响应码
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * 返回错误代码的字符串表示。
     * @return 错误代码的字符串表示。
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 返回Request标识。
     * @return Request标识。
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 返回Host标识。
     * @return Host标识。
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
     * @return 原始的HTTP响应信息
     */
    public String getRawMessage() {
        return rawMessage;
    }
}
