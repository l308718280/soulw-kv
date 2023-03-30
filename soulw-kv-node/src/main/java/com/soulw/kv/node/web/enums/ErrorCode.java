package com.soulw.kv.node.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 */
@AllArgsConstructor
@Getter
public enum ErrorCode {
    SYSTEM_ERROR("-1"),
    /**
     * 不接受投票
     */
    VOTE_ERROR("10001");

    private final String code;

}
