package com.soulw.kv.node.core.cluster.model.runner;

import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.core.cluster.model.Node;
import com.soulw.kv.node.core.cluster.model.VoteApply;
import com.soulw.kv.node.core.cluster.model.WorkNode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 17:23
 */
@Value
@Slf4j
public class VoteRunner implements RunnerLifecycle {
    /**
     * 当前工作节点
     */
    WorkNode workNode;
    Cluster cluster;

    /**
     * 执行选举
     *
     * @param allNodes 全量节点
     */
    public void vote(List<Node> allNodes) {
        try {
            // step1. 判断是否选举中
            if (cluster.isVoting()) {
                return;
            }

            // step2. 切换集群为选举中
            cluster.switchVoting();

            // step3. 进行选举
            VoteApply applyRequest = new VoteApply();
            applyRequest.setVoteTime(System.currentTimeMillis());
            applyRequest.setCurrentNode(this.workNode.toSimpleNode());
            this.workNode.getLastVoteTime().set(applyRequest.getVoteTime());
            int voteNums = 0;
            boolean isOk = false;
            for (Node node : allNodes) {
                // 判断是否中断
                if (!cluster.isVoting()) {
                    break;
                }
                // 判断是否工作节点
                if (node.equals(workNode)) {
                    voteNums++;
                    continue;
                }
                // 请求选票
                if (requestVote(node, applyRequest)) {
                    voteNums++;
                }
                // 判断是否满足条件
                if (cluster.isSatisfyRaft(voteNums)) {
                    // 选举通过
                    workNode.switchMaster(applyRequest);
                    isOk = true;
                    break;
                }
            }

            // step4. 判断边界场景
            if (!workNode.getIsMaster().get() && cluster.isSatisfyRaft(voteNums)) {
                workNode.switchMaster(applyRequest);
                isOk = true;
            }

            // step5. 选举失败
            if (!isOk) {
                cluster.revertVotingStatus();
            }
        } catch (Exception e) {
            log.error("start vote error", e);
            // 选举失败
            cluster.revertVotingStatus();
        }
    }

    private boolean requestVote(Node node, VoteApply applyRequest) {
        return cluster.getNodeRequestGateway().requestVote(node, applyRequest);
    }

    @Override
    public void start() {
        // 无需启动
    }

    @Override
    public void shutdown() {

    }
}
