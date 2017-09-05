package com.alibaba.sdk.android.oss.common;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by jingdan on 2017/9/5.
 * 日志服务使用到的线程池
 */

public class LogThreadPoolManager {
    private static LogThreadPoolManager sThreadPoolManager = new LogThreadPoolManager();

    // 线程池维护线程的最少数量
    private static final int SIZE_CORE_POOL = 1;

    // 线程池维护线程的最大数量
    private static final int SIZE_MAX_POOL = 1;

    // 线程池维护线程所允许的空闲时间
    private static final int TIME_KEEP_ALIVE = 5000;

    // 线程池所使用的缓冲队列大小
    private static final int SIZE_WORK_QUEUE = 500;

    // 任务调度周期
    private static final int PERIOD_TASK_QOS = 1000;

    // 线程池超出界线所使用的缓冲队列大小
    private static final int SIZE_CACHE_QUEUE = 200;

    /*
     * 线程池单例创建方法
     */
    public static LogThreadPoolManager newInstance() {
        return sThreadPoolManager;
    }

    // 任务缓冲队列
    private final Queue<Runnable> mTaskQueue = new LinkedList<Runnable>();

    /*
     * 线程池超出界线时将任务加入缓冲队列
     */
    private final RejectedExecutionHandler mHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
            if(mTaskQueue.size() >= SIZE_CACHE_QUEUE){
                mTaskQueue.poll();//remove old
            }
            mTaskQueue.offer(task);
            OSSLog.logDEBUG("cache queue size: " + mTaskQueue.size(), false);
        }
    };

    /*
     * 将缓冲队列中的任务重新加载到线程池
     */
    private final Runnable mAccessBufferThread = new Runnable() {
        @Override
        public void run() {
            if (hasMoreAcquire()) {
                mThreadPool.execute(mTaskQueue.poll());
            }
        }
    };

    /*
     * 创建一个调度线程池
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /*
     * 通过调度线程周期性的执行缓冲队列中任务
     */
    protected final ScheduledFuture<?> mTaskHandler = scheduler.scheduleAtFixedRate(mAccessBufferThread, 0,
            PERIOD_TASK_QOS, TimeUnit.MILLISECONDS);

    /*
     * 线程池
     */
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL,
            TIME_KEEP_ALIVE, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(SIZE_WORK_QUEUE), mHandler);

    /*
     * 将构造方法访问修饰符设为私有，禁止任意实例化。
     */
    private LogThreadPoolManager() {
    }

    /*
     * 消息队列检查方法
     */
    private boolean hasMoreAcquire() {
        return !mTaskQueue.isEmpty();
    }

    /*
     * 向线程池中添加任务方法
     */
    public void addExecuteTask(Runnable task) {
        if (task != null) {
            mThreadPool.execute(task);
            OSSLog.logDEBUG("work queue size: " + mThreadPool.getQueue().size(), false);
        }
    }
}
