package com.soulw.kv.node.core.cluster.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.soulw.kv.node.core.cluster.model.runner.ClusterRegisterRunner;
import com.soulw.kv.node.core.cluster.model.runner.HeartbeatRunner;
import com.soulw.kv.node.core.cluster.model.runner.LossMasterChecker;
import com.soulw.kv.node.core.cluster.model.runner.VoteRunner;
import com.soulw.kv.node.core.cluster.model.runner.VotingTimeoutRunner;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 11:10
 */
@Setter
@Getter
@Accessors(chain = true)
@Slf4j
public class WorkNode extends Node {
    /**
     * 最后一次心跳时间
     */
    private final AtomicLong lastHeartbeatTime = new AtomicLong();
    private final AtomicBoolean isMaster = new AtomicBoolean(false);
    private final AtomicReference<Node> voteMasterNode = new AtomicReference<>();
    private final AtomicLong lastVoteTime = new AtomicLong(0);
    private final AtomicReference<List<Node>> aliveNodes = new AtomicReference<List<Node>>();
    private Cluster cluster;
    private ClusterRegisterRunner clusterRegisterRunner;
    private HeartbeatRunner heartbeatRunner;
    private LossMasterChecker lossMasterChecker;
    private VotingTimeoutRunner votingTimeoutRunner;
    private VoteRunner voteRunner;

    /**
     * 创建工作节点
     *
     * @param cluster 集群
     */
    public WorkNode(Cluster cluster) {
        this.cluster = cluster;
        this.clusterRegisterRunner = new ClusterRegisterRunner(this);
        this.clusterRegisterRunner.start();
        this.heartbeatRunner = new HeartbeatRunner(this, cluster);
        this.lossMasterChecker = new LossMasterChecker(this, cluster);
        this.voteRunner = new VoteRunner(this, cluster);
        this.voteRunner.start();
        this.votingTimeoutRunner = new VotingTimeoutRunner(cluster, this);
        this.votingTimeoutRunner.start();
        switchSlave(null, -1L);
    }

    /**
     * 切换从节点
     */
    public void switchSlave(Node masterNode, Long lastVoteTime) {
        this.isMaster.set(false);
        this.lastVoteTime.set(lastVoteTime);
        this.voteMasterNode.set(masterNode);
        this.aliveNodes.set(Lists.newArrayList());
        this.heartbeatRunner.shutdown();
        this.votingTimeoutRunner.start();
        this.lossMasterChecker.start();
    }

    /**
     * 执行投票
     *
     * @param apply 申请
     * @return 是否投票
     */
    public boolean doVote(VoteApply apply) {
        Preconditions.checkNotNull(apply, "apply is null");
        Preconditions.checkNotNull(apply.getCurrentNode(), "node is null");
        Preconditions.checkNotNull(apply.getVoteTime(), "vote time is null");
        // step1. 集群在选举中只认老的
        if (cluster.isVoting() && apply.getVoteTime() > this.lastVoteTime.get()) {
            if (!apply.getCurrentNode().equals(voteMasterNode.get())) {
                log.error("not accept vote by request voteTime <= lastVoteTime");
                return false;
            }
        }

        // step2. 非选举状态voting必须更大
        if (!cluster.isVoting() && apply.getVoteTime() < this.lastVoteTime.get()) {
            return false;
        }

        // step3. 投票
        this.lastVoteTime.set(apply.getVoteTime());
        this.cluster.switchVoting();
        this.voteMasterNode.set(apply.getCurrentNode());
        return true;
    }

    /**
     * 切换Master节点
     *
     * @param apply 投票申请
     */
    public void switchMaster(VoteApply apply) {
        Preconditions.checkNotNull(apply, "apply is null");

        this.cluster.switchRunning(this);
        this.isMaster.set(true);
        this.lastVoteTime.set(apply.getVoteTime());
        this.voteMasterNode.set(apply.getCurrentNode());
        this.heartbeatRunner.start();
        this.lossMasterChecker.shutdown();
        this.votingTimeoutRunner.shutdown();
        log.info("current node switch master success, ip: {}, port: {}", getIp(), getPort());
    }

    /**
     * 执行选举
     */
    public void startVote() throws Exception {
        // 执行选举
        this.voteRunner.vote(cluster.getNodeRepository().queryAllNodes());
    }

    /**
     * 生成简单节点
     *
     * @return 结果
     */
    public Node toSimpleNode() {
        return new Node().setIp(getIp())
                .setPort(getPort());
    }
}
