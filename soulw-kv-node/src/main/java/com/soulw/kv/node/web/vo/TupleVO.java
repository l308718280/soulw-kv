package com.soulw.kv.node.web.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Created by SoulW on 2023/3/31.
 *
 * @author SoulW
 * @since 2023/3/31 10:37
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TupleVO implements Serializable {
    public static final String SPLIT_CODE = "_&&&_";
    private String key;
    private String value;

    public byte[] serialize() {
        return (key + SPLIT_CODE).getBytes(StandardCharsets.UTF_8);
    }

    public static TupleVO deserialize(byte[] bytes) {
        if (Objects.isNull(bytes) || bytes.length == 0) {
            return null;
        }
        String[] array = new String(bytes, StandardCharsets.UTF_8).split(SPLIT_CODE);
        return new TupleVO(array[0], array[1]);
    }
}
