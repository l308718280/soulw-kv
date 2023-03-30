package com.soulw.kv.node.core.cluster.model;

import com.soulw.kv.node.core.log.model.LogItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 11:27
 */
@Data
@Accessors(chain = true)
public class Cluster {
    private Integer status;
    private Long minSlaveNode;
    private Long heartbeatTimeout;
    private List<Node> nodes;
    private Node currentNode;

    /**
     * 集群初始化
     */
    public void init() {

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
}
