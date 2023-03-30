package com.soulw.kv.node.core.cluster.model;

import com.google.common.base.Objects;
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
    private String ip;
    private Integer port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) {
            return false;
        }
        Node node = (Node) o;
        return Objects.equal(ip, node.ip) && Objects.equal(port, node.port);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ip, port);
    }
}
