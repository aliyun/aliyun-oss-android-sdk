package com.alibaba.sdk.android.oss.common;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jingdan on 2017/9/5.
 * ThreadPool For Log
 */

public class LogThreadPoolManager {
    private static final int SIZE_CORE_POOL = 1;
    private static final int SIZE_MAX_POOL = 1;
    private static final int TIME_KEEP_ALIVE = 5000;
    private static final int SIZE_WORK_QUEUE = 500;
    private static final int PERIOD_TASK_QOS = 1000;
    private static final int SIZE_CACHE_QUEUE = 200;
    private static LogThreadPoolManager sThreadPoolManager = new LogThreadPoolManager();
    private final Queue<Runnable> mTaskQueue = new LinkedList<Runnable>();
    private final RejectedExecutionHandler mHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
            if (mTaskQueue.size() >= SIZE_CACHE_QUEUE) {
                mTaskQueue.poll();//remove old
            }
            mTaskQueue.offer(task);
        }
    };
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL,
            TIME_KEEP_ALIVE, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(SIZE_WORK_QUEUE), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "oss-android-log-thread");
        }
    }, mHandler);
    private final Runnable mAccessBufferThread = new Runnable() {
        @Override
        public void run() {
            if (hasMoreAcquire()) {
                mThreadPool.execute(mTaskQueue.poll());
            }
        }
    };
    protected final ScheduledFuture<?> mTaskHandler = scheduler.scheduleAtFixedRate(mAccessBufferThread, 0,
            PERIOD_TASK_QOS, TimeUnit.MILLISECONDS);

    private LogThreadPoolManager() {
    }

    public static LogThreadPoolManager newInstance() {
        if (sThreadPoolManager == null) {
            sThreadPoolManager = new LogThreadPoolManager();
        }
        return sThreadPoolManager;
    }

    private boolean hasMoreAcquire() {
        return !mTaskQueue.isEmpty();
    }

    public void addExecuteTask(Runnable task) {
        if (task != null) {
            mThreadPool.execute(task);
        }
    }
}
