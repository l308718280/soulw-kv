package com.soulw.kv.node.core.log.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 */
@AllArgsConstructor
@Getter
public enum LogStatusEnum {
    STATUS_ALIVE((short) 1),

    STATUS_DEAD((short) 2);

    private final Short code;
}
