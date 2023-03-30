package com.soulw.kv.node.core.cluster.model;

import com.soulw.kv.node.core.cluster.gateway.NodeRequestGateway;
import com.soulw.kv.node.core.cluster.repository.NodeRepository;
import com.soulw.kv.node.core.log.model.LogItem;
import com.soulw.kv.node.utils.EnvironmentUtils;
import com.soulw.kv.node.utils.NetworkUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.Objects;
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
    private WorkNode currentNode;
    private NodeRequestGateway nodeRequestGateway;
    private Environment environment;

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
        this.currentNode = new WorkNode(this);
        this.currentNode.setIp(NetworkUtils.loadIp())
                .setPort(EnvironmentUtils.getPort(environment));
    }

    /**
     * 同步日志给集群
     *
     * @param logItem 日志
     */
    public void syncLog(LogItem logItem) {

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
    public void switchRunning(WorkNode masterNode) {
        int prevVoteStatus = this.status.get();
        if (!this.status.compareAndSet(prevVoteStatus, ClusterStatusEnum.RUNNING.getCode())) {
            throw new RuntimeException("mutex update voting status to running error, retry...");
        }
        this.masterNode.set(masterNode);
    }
}
