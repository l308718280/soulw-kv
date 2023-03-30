package com.soulw.kv.node.core.cluster.gateway;

import com.soulw.kv.node.core.cluster.model.Node;
import com.soulw.kv.node.core.cluster.model.VoteApply;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 */
public interface NodeRequestGateway {

    /**
     * 请求投票
     *
     * @param node         节点
     * @param applyRequest 请求
     * @return 结果
     */
    boolean requestVote(Node node, VoteApply applyRequest);

    /**
     * 执行心跳
     *
     * @param node         节点
     * @param applyRequest 请求
     * @return 是否成功
     */
    boolean heartbeat(Node node, VoteApply applyRequest);
}
