package com.soulw.kv.node.core.log.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Optional.ofNullable;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 11:28
 */
@Data
@Slf4j
@ThreadSafe
public class BufferedLog {
    /**
     * 缓存文件大小
     */
    private static final long FILE_SIZE = Long.parseLong(System.getProperty("buffered.log.fileSize", "1024"));
    protected static final AtomicIntegerFieldUpdater<BufferedLog> WROTE_POSITION;
    protected static final AtomicIntegerFieldUpdater<BufferedLog> COMMITED_POSITION;
    protected static final AtomicIntegerFieldUpdater<BufferedLog> FLUSH_POSITION;
    public static final int PRESERVED_LEN = 20;

    static {
        WROTE_POSITION = AtomicIntegerFieldUpdater.newUpdater(BufferedLog.class, "wrotePosition");
        COMMITED_POSITION = AtomicIntegerFieldUpdater.newUpdater(BufferedLog.class, "committedPosition");
        FLUSH_POSITION = AtomicIntegerFieldUpdater.newUpdater(BufferedLog.class, "flushPosition");
    }

    /**
     * 路径
     */
    private String path;
    private File file;
    private MappedByteBuffer mappedBuffer;
    private int fileSize;
    private FileChannel fileChannel;
    private final Integer fileIndex;
    private volatile int wrotePosition = PRESERVED_LEN;
    private volatile int committedPosition;
    private volatile int flushPosition;
    private long lastFlushTime;
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 构建
     *
     * @param path      路径
     * @param fileIndex 文件索引
     */
    public BufferedLog(String path, Integer fileIndex) {
        this.path = path;
        this.fileIndex = fileIndex;
    }

    /**
     * 初始化
     */
    public void init() throws IOException {
        if (StringUtils.isBlank(path)) {
            throw new RuntimeException("invalid path: " + path);
        }

        boolean isOk = true;
        try {
            file = new File(path);
            fileChannel = new RandomAccessFile(file, "rw").getChannel();
            mappedBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, FILE_SIZE);

            // 读头信息
            ByteBuffer readBuffer = mappedBuffer.slice();
            readBuffer.position(0);
            int currentPos = readBuffer.getInt();
            WROTE_POSITION.set(this, Math.max(PRESERVED_LEN, currentPos));
            log.info("load file, file: {}, wrotePos: {}", path, currentPos);
        } catch (IOException e) {
            log.error("load mapped buffer error", e);
            isOk = false;
            throw e;
        } finally {
            if (!isOk && Objects.nonNull(fileChannel)) {
                fileChannel.close();
            }

        }
    }

    /**
     * 设置日志文件
     *
     * @param offset    偏移量
     * @param newStatus 新状态
     * @return 结果
     */
    public boolean setLogItemStatus(long offset, Short newStatus) {
        try {
            lock.lock();
            ByteBuffer buffer = mappedBuffer.slice();
            buffer.position((int) (offset + LogItem.STATUS_OFFSET));
            buffer.putShort(newStatus);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 添加日志项
     *
     * @param logItem 日志
     */
    public boolean addLogItem(LogItem logItem) {
        try {
            lock.lock();

            Preconditions.checkNotNull(logItem.getSize(), "size is null");
            logItem.setOffset(WROTE_POSITION.get(this));
            logItem.setSize(logItem.computeSize());
            logItem.setContentSize(ofNullable(logItem.getData()).map(each -> each.length)
                    .orElse(0));
            logItem.setFileIndex(fileIndex);

            ByteBuffer buffer = ByteBuffer.allocate(logItem.computeSize());
            logItem.writeBuffer(buffer);

            return appendBuffer(buffer);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 查询数据
     *
     * @param offset 开始位置
     * @param len    获取数量
     * @return 结果
     */
    public List<LogItem> getItems(int offset, int len) {
        int currentPos = WROTE_POSITION.get(this);

        ByteBuffer buffer = mappedBuffer.slice();

        List<LogItem> result = Lists.newArrayList();
        for (int i = 0; i < len; i++) {
            if (offset >= currentPos) {
                break;
            }

            buffer.position(offset);
            LogItem logItem = new LogItem();
            logItem.readFromBuffer(buffer);
            result.add(logItem);
        }
        return result;
    }

    private boolean appendBuffer(ByteBuffer buffer) {
        int length = WROTE_POSITION.get(this);
        if (length + buffer.limit() > FILE_SIZE) {
            return false;
        }

        ByteBuffer tmpMappedBuffer = mappedBuffer.slice();
        tmpMappedBuffer.position(WROTE_POSITION.get(this));
        tmpMappedBuffer.put(buffer.array(), 0, buffer.limit());
        WROTE_POSITION.addAndGet(this, buffer.limit());
        return true;
    }

    /**
     * 刷新到硬盘
     */
    public void flush() {
        try {
            int currentPosition = WROTE_POSITION.get(this);
            ByteBuffer header = mappedBuffer.slice();
            header.position(0);
            header.putInt(currentPosition);

            this.mappedBuffer.force();
            this.lastFlushTime = System.currentTimeMillis();
            log.info("flush to desk...");

            FLUSH_POSITION.set(this, currentPosition);
        } catch (Exception e) {
            log.error("flush error", e);
        }

    }
}
