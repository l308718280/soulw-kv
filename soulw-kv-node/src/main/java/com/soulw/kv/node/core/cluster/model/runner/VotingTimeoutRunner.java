package com.soulw.kv.node.core.cluster.model.runner;

import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.core.cluster.model.WorkNode;
import com.soulw.kv.node.utils.ThreadPoolUtils;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 18:17
 */
@Slf4j
@Value
public class VotingTimeoutRunner implements RunnerLifecycle, Runnable {
    Cluster cluster;
    WorkNode workNode;
    AtomicBoolean status = new AtomicBoolean(false);
    ScheduledThreadPoolExecutor executor = ThreadPoolUtils.newScheduler(1, "votingTimeout");

    /**
     * 构建
     *
     * @param cluster  集群
     * @param workNode 工作节点
     */
    public VotingTimeoutRunner(Cluster cluster, WorkNode workNode) {
        this.cluster = cluster;
        this.workNode = workNode;
        executor.scheduleAtFixedRate(this, cluster.getCheckVotingTimeoutInterval(),
                cluster.getCheckVotingTimeoutInterval(), TimeUnit.SECONDS);
    }

    @Override
    public void start() {
        status.set(true);
    }

    @Override
    public void run() {
        try {
            // step1. 判断是否启动
            if (!status.get() || !cluster.isVoting()) {
                return;
            }

            // step2. 判断是否voting超时
            long usedTime = System.currentTimeMillis() - cluster.getStartVotingTime().get();
            if (usedTime >= Duration.ofSeconds(cluster.getVotingTimeout()).toMillis()) {
                cluster.revertVotingStatus();
                log.info("already revert voting status");
            }
        } catch (Exception e) {
            log.error("check voting timeout error", e);
        }
    }

    @Override
    public void shutdown() {
        status.set(false);
    }
}
