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
 * @since 2023/3/30 17:25
 */
@Value
@Slf4j
public class LossMasterChecker implements RunnerLifecycle, Runnable {

    /**
     * 工作节点
     */
    WorkNode workNode;
    Cluster cluster;
    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = ThreadPoolUtils.newScheduler(1, "lossMasterChecker");
    AtomicBoolean status = new AtomicBoolean(false);

    /**
     * 构建
     *
     * @param workNode 工作节点
     * @param cluster  集群节点
     */
    public LossMasterChecker(WorkNode workNode, Cluster cluster) {
        this.cluster = cluster;
        this.workNode = workNode;
        this.scheduledThreadPoolExecutor.scheduleAtFixedRate(this,
                cluster.getCheckLossMasterInterval(), cluster.getCheckLossMasterInterval(), TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            // step1. 不执行：已停止、主节点、正在选举中
            if (!status.get() || workNode.getIsMaster().get() ||
                    cluster.isVoting()) {
                log.debug("not check loss master");
                return;
            }

            // step3. 执行检查
            long subTime = System.currentTimeMillis() - workNode.getLastHeartbeatTime().get();
            if (subTime >= Duration.ofSeconds(cluster.getHeartbeatTimeout()).toMillis()) {
                log.info("master is die, start voting....");
                workNode.startVote();
            } else {
                log.debug("slave: heartbeat is ok");
            }
        } catch (Exception e) {
            log.error("check loss master error", e);
        }
    }

    @Override
    public void start() {
        this.status.set(true);
    }

    @Override
    public void shutdown() {
        this.status.set(false);
    }
}
