package com.soulw.kv.node.core.cluster.model.runner;

import com.soulw.kv.node.core.cluster.model.WorkNode;
import lombok.Value;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 17:26
 */
@Value
public class ClusterRegisterRunner implements RunnerLifecycle {
    WorkNode workNode;

    @Override
    public void start() {
        // 不需要注册
    }

    @Override
    public void shutdown() {

    }
}
