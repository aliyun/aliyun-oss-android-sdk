/**
 * Copyright (C) Alibaba Cloud Computing, 2015
 * All rights reserved.
 * 
 * 版权所有 （C）阿里巴巴云计算，2015
 */

package com.alibaba.sdk.android.common;

/**
 * <p>
 * This class means client side exception occurs when accessing OSS.
 * </p>
 * 
 * <p>
 * {@link ClientException} means errors occur before OSS receives the request, or after getting the response from OSS.
 * For example, when the network is unavailable, the {@link ClientException} is thrown.
 * </p>
 * 
 * <p>
 * {@link ServiceException} means OSS server side errors. For example the invalid Access ID will trigger the ServiceException.
 * It has an error code for caller's troubleshooting.
 * </p>
 * 
 * <p>
 * Generally speaking, the caller must to handle {@link ServiceException} as that exception is sometimes expected and needs client's attention.
 * The error code is very helpful for troubleshooting.
 * </p>
 * 
 */
public class ClientException extends Exception {

    private Boolean canceled = false;

    /**
     * Creates the new instance of ClientException
     */
    public ClientException(){
        super();
    }

    /**
     * Creates the new instance of CLientException with the specified error message.
     * @param message error message
     */
    public ClientException(String message){
        super("[ErrorMessage]: " + message);
    }

    /**
     * Creates the new instance of ClientException with the specified exception instance.
     * @param cause exception instance
     */
    public ClientException(Throwable cause){
        super(cause);
    }
    
    /**
     * Creates the new instance of CLientException with specified error message and exception instance.
     * @param message error message
     * @param cause exception instance
     */
    public ClientException(String message, Throwable cause){
        this(message, cause, false);
    }

    /**
     * Creates the new instance of ClientException with specified error message, exception instance and isCancelled flag.
     */
    public ClientException(String message, Throwable cause, Boolean isCancelled) {
        super("[ErrorMessage]: " + message, cause);
        this.canceled = isCancelled;
    }

    /**
     * if the flag is true, it means the exception is triggered by the cancellation.
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
