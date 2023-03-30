package com.soulw.kv.node.core.cluster.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 11:10
 */
@Setter
@Getter
@Accessors(chain = true)
public class WorkNode extends Node {
    /**
     * 最后一次心跳时间
     */
    private Long lastHeartbeatTime;
}
