package com.soulw.kv.node.core.cluster.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 */
@AllArgsConstructor
@Getter
public enum RoleEnum {
    ROLE_LEADER(1),
    ROLE_CANDIDATE(2),
    ROLE_SLAVE(3);

    private final Integer code;
}
