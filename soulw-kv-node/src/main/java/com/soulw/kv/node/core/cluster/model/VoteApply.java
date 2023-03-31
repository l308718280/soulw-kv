package com.soulw.kv.node.core.cluster.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 17:20
 */
@Data
@Accessors(chain = true)
public class VoteApply implements Serializable {
    private Node currentNode;
    private Long voteTime;
}
