package com.soulw.kv.node.core.cluster.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 11:39
 */
@Data
@Accessors(chain = true)
public class Node {
    private Integer role;
    private String ip;
    private Integer port;
    private Boolean isMaster;
}
