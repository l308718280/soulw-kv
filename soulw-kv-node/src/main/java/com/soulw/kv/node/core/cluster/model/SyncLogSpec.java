package com.soulw.kv.node.core.cluster.model;

import com.google.common.base.Preconditions;
import com.soulw.kv.node.core.log.model.LogItem;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Objects;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 17:13
 */
@Value
public class SyncLogSpec {
    Integer clusterStatus;
    Integer minSlaveNodes;
    WorkNode workNode;

    /**
     * 是否满足同步日志规则
     *
     * @param logItem 日志
     * @return 结果
     */
    public void check(LogItem logItem) {
        Preconditions.checkState(Objects.equals(ClusterStatusEnum.RUNNING.getCode(), clusterStatus), "cluster status error");
        Preconditions.checkState(workNode.getIsMaster().get(), "only master can sync log");
        Preconditions.checkState(CollectionUtils.size(workNode.getAliveNodes().get()) >= minSlaveNodes, "slave node not enought");
    }
}
