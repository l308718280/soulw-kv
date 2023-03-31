package com.soulw.kv.node.core.cluster.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 11:41
 */
@AllArgsConstructor
@Getter
public enum ClusterStatusEnum {
    VOTING(1),
    RUNNING(2);

    private final Integer code;
}
