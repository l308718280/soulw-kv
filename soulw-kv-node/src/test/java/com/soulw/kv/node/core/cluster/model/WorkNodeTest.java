package com.soulw.kv.node.core.cluster.model;

import com.soulw.kv.node.core.cluster.gateway.NodeRequestGateway;
import com.soulw.kv.node.core.cluster.repository.NodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class WorkNodeTest {

    private WorkNode workNode;

    @BeforeEach
    void setUp() {
        NodeRepository nodeRepository = Mockito.mock(NodeRepository.class);
        NodeRequestGateway nodeRequestGateway = Mockito.mock(NodeRequestGateway.class);
        Environment env = Mockito.mock(Environment.class);
        Cluster cluster = new Cluster(nodeRepository, nodeRequestGateway, env);
        cluster.setHeartbeatTimeout(10_000L);
        workNode = new WorkNode(cluster);
    }

    @Test
    void switchSlave() {
        workNode.switchSlave(null, -1L);
        assertFalse(workNode.getIsMaster().get());
    }

    @Test
    void switchMaster() {
        VoteApply apply = new VoteApply();
        apply.setVoteTime(1L);
        workNode.switchMaster(apply);
        assertTrue(workNode.getIsMaster().get());
    }

    @Test
    void doVote() {
        VoteApply apply = new VoteApply();
        apply.setCurrentNode(new Node());
        apply.setVoteTime(System.currentTimeMillis());
        assertTrue(workNode.doVote(apply));
        assertEquals(workNode.getLastVoteTime().get(), apply.getVoteTime());
    }

    @Test
    void startVoting() throws Exception {
        workNode.startVote();
        assertTrue(true);
    }
}