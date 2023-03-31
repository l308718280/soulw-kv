package com.soulw.kv.node.core.cluster.model;

import com.soulw.kv.node.core.cluster.gateway.NodeRequestGateway;
import com.soulw.kv.node.core.cluster.repository.NodeRepository;
import com.soulw.kv.node.core.log.model.LogItem;
import com.soulw.kv.node.utils.EnvironmentUtils;
import com.soulw.kv.node.utils.NetworkUtils;
import com.soulw.kv.node.utils.ThreadPoolUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 11:27
 */
@Slf4j
@Data
@Accessors(chain = true)
public class Cluster {
    /**
     * 检查心跳时间
     */
    private static final Long DEFAULT_CHECK_LOSS_MASTER_INTERVAL = Long.valueOf(System.getProperty("cluster.check.loss.master.interval", "5"));
    private static final Long DEFAULT_CHECK_VOTING_TIMEOUT_INTERVAL = 30L;
    private static final Long DEFAULT_HEARTBEAT_INTERVAL = 8L;
    private static final Long DEFAULT_HEARTBEAT_TIMEOUT = 10L;
    private static final Long DEFAULT_VOTING_TIMEOUT = 60L;
    private static final Long MIN_SLAVE_NODE = 0L;
    private static final Integer DEFAULT_QUEUE_SIZE = 200;

    private final AtomicInteger status = new AtomicInteger(ClusterStatusEnum.RUNNING.getCode());
    private final AtomicLong startVotingTime = new AtomicLong(0L);
    private Long minSlaveNode = MIN_SLAVE_NODE;
    private Long heartbeatTimeout = DEFAULT_HEARTBEAT_TIMEOUT;
    private Long checkLossMasterInterval = DEFAULT_CHECK_LOSS_MASTER_INTERVAL;
    private Long checkVotingTimeoutInterval = DEFAULT_CHECK_VOTING_TIMEOUT_INTERVAL;
    private Long heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
    private Long votingTimeout = DEFAULT_VOTING_TIMEOUT;
    private final AtomicReference<Node> masterNode = new AtomicReference<>();
    private NodeRepository nodeRepository;
    private WorkNode workNode;
    private NodeRequestGateway nodeRequestGateway;
    private Environment environment;
    private ThreadPoolTaskExecutor executor;
    private Boolean standardRaft;

    /**
     * 构建
     *
     * @param nodeRepository     仓储
     * @param nodeRequestGateway 网关
     * @param environment        环境
     */
    public Cluster(NodeRepository nodeRepository, NodeRequestGateway nodeRequestGateway, Environment environment) {
        this.nodeRepository = nodeRepository;
        this.nodeRequestGateway = nodeRequestGateway;
        this.environment = environment;
    }

    /**
     * 集群初始化
     */
    public void init() {
        this.workNode = new WorkNode(this);
        this.workNode.setIp(NetworkUtils.loadIp())
                .setPort(EnvironmentUtils.getPort(environment));
        if (Objects.isNull(executor)) {
            int coreSize = Runtime.getRuntime().availableProcessors() * 2 - 1;
            executor = ThreadPoolUtils.newExecutor(coreSize, coreSize << 1, DEFAULT_QUEUE_SIZE,
                    new ThreadPoolExecutor.CallerRunsPolicy(), "cluster-executor");
        }

        this.standardRaft = EnvironmentUtils.getStandardRaft(environment);
    }

    /**
     * 同步日志给集群
     *
     * @param logItem 日志
     */
    public void syncLog(LogItem logItem) {
        // step1. 检查权限
        getSpec().check(logItem);

        // step2. 开始同步日志
        int syncNums = 0;
        for (Node node : workNode.getAliveNodes().get()) {
            if (workNode.equals(node)) {
                syncNums++;
                continue;
            }

            if (this.isSatisfyRaft(syncNums)) {
                asyncSyncLog(logItem, node);
                continue;
            }

            try {
                nodeRequestGateway.syncLog(node, logItem);
                syncNums++;
            } catch (Exception e) {
                log.error("sync log error", e);
            }

        }
    }

    /**
     * 是否满足
     *
     * @param nums 目前数量
     * @return 结果
     */
    public boolean isSatisfyRaft(int nums) {
        return new RaftCompare(this, nodeRepository.queryAllNodes()).isSatisfy(nums);
    }

    /**
     * 异步同步日志
     *
     * @param logItem 日志
     * @param node    节点
     */
    private void asyncSyncLog(LogItem logItem, Node node) {
        executor.submit(() -> nodeRequestGateway.syncLog(node, logItem));
    }

    /**
     * 创建SPEC
     *
     * @return 结果
     */
    public SyncLogSpec getSpec() {
        return new SyncLogSpec(status.get(), minSlaveNode.intValue(), workNode);
    }

    /**
     * 回滚日志
     *
     * @param logItem 日志
     */
    public void rollbackLog(LogItem logItem) {

    }

    /**
     * 是否选举中
     *
     * @return 结果
     */
    public boolean isVoting() {
        return Objects.equals(ClusterStatusEnum.VOTING.getCode(), status.get());
    }

    /**
     * 接收心跳
     *
     * @param apply 接收心跳
     */
    public void receiveHeartbeat(VoteApply apply) {
        Node reqMaster = apply.getCurrentNode();
        if (!reqMaster.equals(masterNode.get())) {
            this.workNode.switchSlave(apply.getCurrentNode(), apply.getVoteTime());
            this.masterNode.set(reqMaster);
            this.switchRunning(apply.getCurrentNode());
        }
        this.workNode.getLastHeartbeatTime().set(System.currentTimeMillis());
        log.info("receive heartbeat success");
    }

    /**
     * 切换为选举状态
     */
    public void switchVoting() {
        int prevVoteStatus = this.status.get();
        if (!this.status.compareAndSet(prevVoteStatus, ClusterStatusEnum.VOTING.getCode())) {
            throw new RuntimeException("mutex update voting status error, retry...");
        }
        this.startVotingTime.set(System.currentTimeMillis());
        log.info("cluster is switch voting...");
    }

    /**
     * 回滚选举状态
     */
    public void revertVotingStatus() {
        if (isVoting()) {
            this.status.set(ClusterStatusEnum.RUNNING.getCode());
        }
        log.info("vote error, revert to running status...");
    }

    /**
     * 切换为运行状态
     *
     * @param masterNode 工作节点
     */
    public void switchRunning(Node masterNode) {
        int prevVoteStatus = this.status.get();
        if (!this.status.compareAndSet(prevVoteStatus, ClusterStatusEnum.RUNNING.getCode())) {
            throw new RuntimeException("mutex update voting status to running error, retry...");
        }
        this.masterNode.set(masterNode);
        log.info("cluster is switch running...");
    }
}
