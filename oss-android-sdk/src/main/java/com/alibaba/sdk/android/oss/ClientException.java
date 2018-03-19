/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * <p>
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.oss;

import com.alibaba.sdk.android.oss.common.OSSLog;

/**
 * <p>
 * The client side exceptions when accessing OSS service
 * </p>
 * <p>
 * <p>
 * {@link ClientException} means there're errors occurred when sending request to OSS or parsing the response from OSS.
 * For example when the network is unavailable, this exception will be thrown.
 * </p>
 * <p>
 * <p>
 * {@link ServiceException} means there're errors occurred in OSS service side. For example, the Access Id
 * does not exist for authentication, then {@link ServiceException} or its subclass is thrown.
 * The ServiceException has the error code for the caller to have some specific handling.
 * </p>
 * <p>
 * <p>
 * Generally speaking, the caller only needs to handle {@link ServiceException} as it means the request
 * has reached OSS, but there're some errors occurred. This error in most of cases are expected due to
 * wrong parameters int the request or some wrong settings in user's account. The error code is very helpful
 * for troubleshooting.
 * </p>
 */
public class ClientException extends Exception {

    private Boolean canceled = false;

    /**
     * Constructor
     */
    public ClientException() {
        super();
    }

    /**
     * Constructor with message
     *
     * @param message the error message
     */
    public ClientException(String message) {
        super("[ErrorMessage]: " + message);
    }

    /**
     * Constructor with exception
     *
     * @param cause the exception
     */
    public ClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with error message and exception instance
     *
     * @param message Error message
     * @param cause   The exception instance
     */
    public ClientException(String message, Throwable cause) {
        this(message, cause, false);
    }

    /**
     * Constructor with error message, exception instance and isCancelled flag
     */
    public ClientException(String message, Throwable cause, Boolean isCancelled) {
        super("[ErrorMessage]: " + message, cause);
        this.canceled = isCancelled;
        OSSLog.logThrowable2Local(this);
    }

    /**
     * Checks if the exception is due to the cancellation
     *
     * @return
     */
    public Boolean isCanceledException() {
        return canceled;
    }


    @Override
    public String getMessage() {
        String base = super.getMessage();
        return getCause() == null ? base : getCause().getMessage() + "\n" + base;
    }
}
