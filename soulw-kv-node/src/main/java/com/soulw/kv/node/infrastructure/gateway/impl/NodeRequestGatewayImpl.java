package com.soulw.kv.node.infrastructure.gateway.impl;

import com.soulw.kv.node.core.cluster.gateway.NodeRequestGateway;
import com.soulw.kv.node.core.cluster.model.Node;
import com.soulw.kv.node.core.cluster.model.VoteApply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 20:47
 */
@Component
@Slf4j
public class NodeRequestGatewayImpl implements NodeRequestGateway {

    @Override
    public boolean requestVote(Node node, VoteApply applyRequest) {
        return false;
    }

    @Override
    public boolean heartbeat(Node node, VoteApply applyRequest) {
        return false;
    }
}
