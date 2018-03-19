package com.alibaba.sdk.android.oss;

/**
 * Created by huaixu on 2018/2/9.
 */

public class TaskCancelException extends Exception {
    /**
     * Constructor
     */
    public TaskCancelException() {
        super();
    }

    /**
     * Constructor with message
     *
     * @param message the error message
     */
    public TaskCancelException(String message) {
        super("[ErrorMessage]: " + message);
    }

    /**
     * Constructor with exception
     *
     * @param cause the exception
     */
    public TaskCancelException(Throwable cause) {
        super(cause);
    }
}
