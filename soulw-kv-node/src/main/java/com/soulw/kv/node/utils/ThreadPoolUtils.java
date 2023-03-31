package com.soulw.kv.node.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
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

    private static final int DEFAULT_KEEP_ALIVE_SEC = 3600;

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
     * 创建线程池
     *
     * @param coreSize         核心
     * @param maxSize          最大
     * @param queueSize        队列
     * @param handler          超限处理
     * @param threadNamePrefix 线程名称前缀
     * @return 结果
     */
    public static ThreadPoolTaskExecutor newExecutor(int coreSize, int maxSize, int queueSize, RejectedExecutionHandler handler, String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueSize);
        executor.setKeepAliveSeconds(DEFAULT_KEEP_ALIVE_SEC);
        executor.setThreadFactory(new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(@Nonnull Runnable r) {
                return createThread(r, threadNamePrefix + "-" + counter.incrementAndGet());
            }
        });
        if (Objects.nonNull(handler)) {
            executor.setRejectedExecutionHandler(handler);
        }
        executor.initialize();
        return executor;
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
