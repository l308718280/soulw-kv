package com.soulw.kv.node.core.cluster.repository;

import com.soulw.kv.node.core.cluster.model.Node;

import java.util.List;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 13:50
 */
public interface NodeRepository {

    /**
     * 查询全量节点
     *
     * @return 全量节点
     */
    List<Node> queryAllNodes();

}
