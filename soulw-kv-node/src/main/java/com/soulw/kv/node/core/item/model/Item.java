package com.soulw.kv.node.core.item.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 11:25
 */
@Data
@Accessors(chain = true)
public class Item implements Serializable {
    private String key;
    private String value;
}
