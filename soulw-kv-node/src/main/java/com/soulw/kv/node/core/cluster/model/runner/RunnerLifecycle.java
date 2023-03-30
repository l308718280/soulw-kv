package com.soulw.kv.node.core.cluster.model.runner;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 */
public interface RunnerLifecycle {
    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void shutdown();
}
