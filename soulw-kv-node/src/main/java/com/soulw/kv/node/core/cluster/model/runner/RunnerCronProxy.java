package com.soulw.kv.node.core.cluster.model.runner;

import lombok.Value;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 20:36
 */
@Value
public class RunnerCronProxy implements Runnable {

    AtomicBoolean status;
    Long interval;
    Runnable sourceRunnable;

    @Override
    public void run() {
        while (status.get()) {
            sourceRunnable.run();
            LockSupport.parkNanos(this, Duration.ofSeconds(interval).toNanos());
        }
    }

}
