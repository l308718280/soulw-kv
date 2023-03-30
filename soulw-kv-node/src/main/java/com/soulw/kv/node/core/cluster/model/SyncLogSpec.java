package com.soulw.kv.node.core.cluster.model;

import com.soulw.kv.node.core.log.model.LogItem;
import lombok.Value;

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
    public boolean isSatisfy(LogItem logItem) {
        return false;
    }
}
