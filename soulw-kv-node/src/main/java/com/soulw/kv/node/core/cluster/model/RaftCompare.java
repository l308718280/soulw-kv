package com.soulw.kv.node.core.cluster.model;

import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Created by SoulW on 2023/3/31.
 *
 * @author SoulW
 * @since 2023/3/31 11:11
 */
@Value
public class RaftCompare {
    Cluster cluster;
    List<Node> allNodes;

    public boolean isSatisfy(int nums) {
        if (cluster.getStandardRaft()) {
            return nums >= (CollectionUtils.size(allNodes) / 2) + 1;
        }

        return nums >= (CollectionUtils.size(allNodes) / 2);
    }
}
