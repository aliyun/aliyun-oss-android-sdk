package com.alibaba.sdk.android.oss.internal;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.model.OSSResult;
import com.alibaba.sdk.android.oss.network.ExecutionContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by zhouzhuo on 11/23/15.
 */
public class OSSAsyncTask<T extends OSSResult> {

    private Future<T> future;

    private ExecutionContext context;

    private volatile boolean canceled;

    public static OSSAsyncTask wrapRequestTask(Future future, ExecutionContext context) {
        OSSAsyncTask asynTask = new OSSAsyncTask();
        asynTask.future = future;
        asynTask.context = context;
        return asynTask;
    }

    /**
     * Cancel the task
     */
    public void cancel() {
        canceled = true;
        if (context != null) {
            context.getCancellationHandler().cancel();
        }
    }

    /**
     * Checks if the task is complete
     *
     * @return
     */
    public boolean isCompleted() {
        return future.isDone();
    }

    /**
     * Waits and gets the result.
     *
     * @return
     * @throws ClientException
     * @throws ServiceException
     */
    public T getResult() throws ClientException, ServiceException {
        try {
            T result = future.get();
            return result;
        } catch (InterruptedException e) {
            throw new ClientException(" InterruptedException and message : " + e.getMessage(), e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClientException) {
                throw (ClientException) cause;
            } else if (cause instanceof ServiceException) {
                throw (ServiceException) cause;
            } else {
                cause.printStackTrace();
                throw new ClientException("Unexpected exception!" + cause.getMessage());
            }
        }
    }

    /**
     * Waits until the task is finished
     */
    public void waitUntilFinished() {
        try {
            future.get();
        } catch (Exception ignore) {
        }
    }

    /**
     * Gets the flag if the task has been canceled.
     */
    public boolean isCanceled() {
        return canceled;
    }
}
