package com.soulw.kv.node.core.log.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.utils.LogFileUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 11:27
 */
@Data
@Slf4j
@Accessors(chain = true)
@ThreadSafe
public class LogWriter {
    /**
     * 锁对象
     */
    private final ReentrantLock addLock = new ReentrantLock();
    private final ReentrantLock updateLock = new ReentrantLock();
    /**
     * 集群对象
     */
    private Cluster cluster;
    /**
     * 文件路径
     */
    private String dir;
    /**
     * 全量缓存日志
     */
    private Map<Integer, BufferedLog> allLog;
    /**
     * 当前缓存日志
     */
    private transient BufferedLog currentLog;

    /**
     * 构建
     *
     * @param dir 文件路径
     */
    public LogWriter(String dir, Cluster cluster) {
        this.dir = dir;
        this.cluster = cluster;
    }

    /**
     * 初始化
     */
    public void init() {
        // step1. 初始化目录
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            Preconditions.checkState(dirFile.mkdirs(), "create directory error: " + dir);
        }
        Preconditions.checkState(dirFile.isDirectory(), "path " + dirFile + " is not directory");

        // step2. 获取当前文件
        List<FileNameVO> files = Stream.of(Objects.requireNonNull(dirFile.listFiles()))
                .map(each -> LogFileUtils.resolve(each.getName()))
                .collect(Collectors.toList());
        this.allLog = Maps.newConcurrentMap();
        files.stream()
                .map(each -> new BufferedLog(dir + File.separator + each.getFileName(), each.getIndex()))
                .forEach(each -> allLog.put(each.getFileIndex(), each));

        // step3. 获得工作文件
        rolloverNext();
    }

    /**
     * 添加项
     *
     * @param item item
     */
    public boolean addItem(LogItem item) {
        boolean addResult = false;
        try {
            addLock.lock();
            // step1. 写入本地日志
            addResult = this.currentLog.addLogItem(item);
            if (!addResult) {
                rolloverNext();
                addResult = this.currentLog.addLogItem(item);
            }
        } finally {
            addLock.unlock();
        }
        if (!Objects.equals(Boolean.TRUE, addResult)) {
            throw new RuntimeException("unknow error: " + item);
        }
        try {
            cluster.syncLog(item);
        } catch (Exception e) {
            delItem0(item);
            throw new RuntimeException("sync client error", e);
        }

        return true;
    }

    private void rolloverNext() {
        try {
            BufferedLog nowLog = currentLog;
            if (Objects.nonNull(nowLog)) {
                nowLog.flush();
            }

            int fileIndex = allLog.size();
            BufferedLog newLog = new BufferedLog(dir + File.separator + LogFileUtils.generateFileName(fileIndex), fileIndex);
            newLog.init();
            this.allLog.put(newLog.getFileIndex(), newLog);
            this.currentLog = newLog;
        } catch (IOException e) {
            log.error("rollover next error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除
     *
     * @param item item
     */
    public boolean delItem(LogItem item) {
        boolean resp = delItem0(item);

        if (resp) {
            try {
                cluster.syncLog(item);
            } catch (Exception e) {
                updateItemStatus(item);
                throw new RuntimeException("transfer log error", e);
            }
        }

        return resp;
    }

    private boolean updateItemStatus(LogItem item) {
        if (Objects.isNull(item.getOffset()) || Objects.isNull(item.getFileIndex())) {
            return false;
        }

        BufferedLog matchFile = allLog.get(item.getFileIndex());
        if (Objects.nonNull(matchFile)) {
            matchFile.setLogItemStatus(item.getOffset(), item.getStatus());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 线程安全
     *
     * @param item 数据
     * @return 结果
     */
    private boolean delItem0(LogItem item) {
        if (Objects.isNull(item.getOffset()) || Objects.isNull(item.getFileIndex())) {
            return false;
        }

        BufferedLog mathFile = allLog.get(item.getFileIndex());

        try {
            updateLock.lock();
            if (Objects.nonNull(mathFile) && mathFile.setLogItemStatus(item.getOffset(), LogStatusEnum.STATUS_DEAD.getCode())) {
                item.setStatus(LogStatusEnum.STATUS_DEAD.getCode());
                return true;
            } else {
                return false;
            }
        } finally {
            updateLock.unlock();
        }
    }

    /**
     * 获取数据
     *
     * @param fileIndex 文件索引
     * @param offset    位置
     */
    public LogItem getItem(Integer fileIndex, Integer offset) {
        if (Objects.isNull(fileIndex) || Objects.isNull(offset) || offset <= BufferedLog.PRESERVED_LEN) {
            return null;
        }

        BufferedLog file = allLog.get(fileIndex);
        if (Objects.isNull(file)) {
            return null;
        }

        if (offset >= file.getWrotePosition()) {
            return null;
        }

        return CollectionUtils.emptyIfNull(file.getItems(offset, 1))
                .stream().findFirst().orElse(null);
    }
}
