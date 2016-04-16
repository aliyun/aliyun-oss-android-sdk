package com.alibaba.sdk.android.oss.internal;

import android.support.annotation.NonNull;

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

    /**
     * 取消任务
     */
    public void cancel() {
        context.getCancellationHandler().cancel();
    }

    /**
     * 检查任务是否已经完成
     */
    public boolean isCompleted() {
        return future.isDone();
    }

    /**
     * 检查任务是否已经调用了cancel方法，不保证任务真的已经取消了
     */
    public boolean alreadyCalledCancel() {
        return context.getCancellationHandler().isCancelled();
    }

    /**
     * 阻塞等待任务完成，并获取结果
     *
     * @throws ClientException
     * @throws ServiceException
     */
    public T getResult() throws ClientException, ServiceException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new ClientException(e.getMessage(), e);
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

    public static OSSAsyncTask wrapRequestTask(@NonNull Future future, @NonNull ExecutionContext context) {
        OSSAsyncTask asyncTask = new OSSAsyncTask();
        asyncTask.future = future;
        asyncTask.context = context;
        return asyncTask;
    }

    /**
     * 阻塞等待任务完成
     */
    public void waitUntilFinished() {
        try {
            future.get();
        } catch (Exception ignore) {
        }
    }
}
