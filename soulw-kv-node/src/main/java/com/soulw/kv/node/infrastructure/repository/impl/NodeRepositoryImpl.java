package com.soulw.kv.node.infrastructure.repository.impl;

import com.google.common.collect.Lists;
import com.soulw.kv.node.core.cluster.model.Node;
import com.soulw.kv.node.core.cluster.repository.NodeRepository;
import com.soulw.kv.node.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 20:44
 */
@Component
@Slf4j
public class NodeRepositoryImpl implements NodeRepository {

    @Override
    public List<Node> queryAllNodes() {
        String ip = NetworkUtils.loadIp();

        List<Node> result = Lists.newArrayList();
        for (int i = 0; i < 1; i++) {
            result.add(new Node().setIp(ip)
                    .setPort(8080 + i));
        }
        return result;
    }

}
