package com.soulw.kv.node.utils;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 17:58
 */
@Slf4j
public class ThreadPoolUtils {

    /**
     * 创建定时任务线程池
     *
     * @param coreSize         核心线程数
     * @param threadNamePrefix 线程名称
     * @return 结果
     */
    public static ScheduledThreadPoolExecutor newScheduler(int coreSize, String threadNamePrefix) {
        return new ScheduledThreadPoolExecutor(coreSize, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(@Nonnull Runnable r) {
                return createThread(r, threadNamePrefix + "-" + counter.incrementAndGet());
            }
        });
    }

    /**
     * 创建线程
     *
     * @param r    执行器
     * @param name 线程名称
     * @return 结果
     */
    public static Thread createThread(Runnable r, String name) {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setName(name);
        thread.setUncaughtExceptionHandler((t, e) ->
                log.error("uncaught exception by thread: {}", t.toString(), e));
        return thread;
    }
}
