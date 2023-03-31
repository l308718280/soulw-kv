package com.soulw.kv.node.core.cluster.model.runner;

import com.google.common.collect.Lists;
import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.core.cluster.model.Node;
import com.soulw.kv.node.core.cluster.model.VoteApply;
import com.soulw.kv.node.core.cluster.model.WorkNode;
import com.soulw.kv.node.utils.ThreadPoolUtils;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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
public class HeartbeatRunner implements RunnerLifecycle, Runnable {
    WorkNode workNode;
    Cluster cluster;
    ScheduledThreadPoolExecutor executor = ThreadPoolUtils.newScheduler(1, "heartbeatRunner");
    AtomicBoolean status = new AtomicBoolean(false);

    /**
     * 应用启动
     * @param workNode 工作节点
     * @param cluster 集群
     */
    public HeartbeatRunner(WorkNode workNode, Cluster cluster) {
        this.workNode = workNode;
        this.cluster = cluster;
        this.executor.scheduleAtFixedRate(this, cluster.getHeartbeatInterval(), cluster.getHeartbeatInterval(), TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            // step1. 判断启动状态
            if (!status.get() || !workNode.getIsMaster().get()) {
                log.debug("not master node");
                return;
            }

            // step2. 进行心跳
            List<Node> allNodes = cluster.getNodeRepository().queryAllNodes();
            List<Node> aliveNodes = Lists.newArrayList();

            VoteApply request = new VoteApply();
            request.setVoteTime(workNode.getLastVoteTime().get());
            request.setCurrentNode(workNode.toSimpleNode());
            for (Node node : allNodes) {
                if (node.equals(workNode)) {
                    aliveNodes.add(node);
                    continue;
                }

                if (cluster.getNodeRequestGateway().heartbeat(node, request)) {
                    aliveNodes.add(node);
                }
            }
            // step3. 设置存活节点
            workNode.getAliveNodes().set(aliveNodes);
            log.info("heartbeat alive nodes size: " + aliveNodes.size());
        } catch (Exception e) {
            log.error("heartbeat error", e);
        }
    }

    @Override
    public void start() {
        status.set(true);
    }

    @Override
    public void shutdown() {
        status.set(false);
    }
}
