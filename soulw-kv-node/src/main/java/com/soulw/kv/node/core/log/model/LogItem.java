package com.soulw.kv.node.core.log.model;

import lombok.Data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 11:28
 */
@Data
public class LogItem implements Serializable {
    public static final int HEADER_SIZE = 26;
    public static final int STATUS_OFFSET = 8;
    private Integer offset = -1;
    private Integer size = -1;
    private Short status = -1;
    private Long timestamp = -1L;
    private Integer contentSize = 0;
    private Integer fileIndex;
    private byte[] data = new byte[0];

    /**
     * 默认构建
     */
    public LogItem() {
    }

    /**
     * 创建
     *
     * @param status    状态
     * @param timestamp 写入时间戳
     * @param data      数据
     */
    public LogItem(Short status, Long timestamp, byte[] data) {
        this.status = status;
        this.timestamp = timestamp;
        this.data = data;
        this.size = computeSize();
        this.contentSize = Objects.isNull(data) ? 0 : data.length;
    }

    /**
     * 数据
     *
     * @return 结果
     */
    public String getDataStr() {
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 计算大小
     *
     * @return 结果
     */
    public Integer computeSize() {
        if (Objects.isNull(data)) {
            return HEADER_SIZE;
        }

        return HEADER_SIZE + data.length;
    }

    /**
     * 写入到buffer里
     *
     * @param buffer buffer
     */
    public void writeBuffer(ByteBuffer buffer) {
        buffer.putInt(offset);
        buffer.putInt(size);
        buffer.putShort(status);
        buffer.putLong(timestamp);
        buffer.putInt(contentSize);
        buffer.putInt(fileIndex);
        if (Objects.nonNull(data)) {
            buffer.put(data, 0, data.length);
        }
    }

    /**
     * 从buffer中读出来
     *
     * @param buffer buffer
     */
    public void readFromBuffer(ByteBuffer buffer) {
        this.offset = buffer.getInt();
        this.size = buffer.getInt();
        this.status = buffer.getShort();
        this.timestamp = buffer.getLong();
        this.contentSize = buffer.getInt();
        this.fileIndex = buffer.getInt();
        if (contentSize > 0) {
            byte[] bytes = new byte[contentSize];
            buffer.get(bytes, 0, contentSize);
            this.data = bytes;
        }
    }
}
