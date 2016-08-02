package com.alibaba.sdk.android.mns.internal;

import com.alibaba.sdk.android.common.ClientException;
import com.alibaba.sdk.android.common.ServiceException;
import com.alibaba.sdk.android.mns.model.MNSResult;
import com.alibaba.sdk.android.mns.network.ExecutionContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by pan.zengp on 2016/7/4.
 */
public class MNSAsyncTask<T extends MNSResult> {
    private Future<T> future;

    private ExecutionContext context;

    private volatile boolean canceled;

    /**
     * 取消任务
     */
    public void cancel() {
        canceled = true;
        if (context != null) {
            context.getCancellationHandler().cancel();
        }
    }

    /**
     * 检查任务是否已经完成
     *
     * @return
     */
    public boolean isCompleted() {
        return future.isDone();
    }

    /**
     * 阻塞等待任务完成，并获取结果
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

    public static MNSAsyncTask wrapRequestTask(Future future, ExecutionContext context) {
        MNSAsyncTask asynTask = new MNSAsyncTask();
        asynTask.future = future;
        asynTask.context = context;
        return asynTask;
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

    /**
     * 任务是否已经被取消过
     */
    public boolean isCanceled() {
        return canceled;
    }
}
