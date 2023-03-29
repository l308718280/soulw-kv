package com.soulw.kv.node.core.log.model;

import com.google.common.base.Preconditions;
import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.utils.LogFileUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
public class LogWriter {
    /**
     * 锁对象
     */
    private final ReentrantLock lock = new ReentrantLock();
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
    private List<BufferedLog> allLog;
    /**
     * 当前缓存日志
     */
    private transient BufferedLog currentLog;

    /**
     * 构建
     *
     * @param dir 文件路径
     */
    public LogWriter(String dir) {
        this.dir = dir;
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
        this.allLog = files.stream()
                .map(each -> new BufferedLog(dir + File.separator + each.getFileName(), each.getIndex()))
                .collect(Collectors.toList());

        // step3. 获得工作文件
        rolloverNext();
    }

    /**
     * 添加项
     *
     * @param item item
     */
    public boolean addItem(LogItem item) {
        // step1. 写入本地日志
        boolean addResult = this.currentLog.addLogItem(item);
        if (!addResult) {
            rolloverNext();
            addResult = this.currentLog.addLogItem(item);
        }
        if (!Objects.equals(Boolean.TRUE, addResult)) {
            throw new RuntimeException("unknow error: " + item);
        }

        return true;
    }

    private void rolloverNext() {
        try {
            lock.lock();
            BufferedLog nowLog = currentLog;
            if (Objects.nonNull(nowLog)) {
                nowLog.flush();
            }

            int fileIndex = allLog.size();
            BufferedLog newLog = new BufferedLog(dir + File.separator + LogFileUtils.generateFileName(fileIndex), fileIndex);
            newLog.init();
            this.allLog.add(newLog);
            this.currentLog = newLog;
        } catch (IOException e) {
            log.error("rollover next error", e);
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 删除
     *
     * @param item item
     */
    public boolean delItem(LogItem item) {
        if (Objects.isNull(item.getOffset()) || Objects.isNull(item.getFileIndex())) {
            return false;
        }

        BufferedLog mathFile = allLog.stream()
                .filter(each -> Objects.equals(each.getFileIndex(), item.getFileIndex()))
                .findFirst()
                .orElse(null);

        if (Objects.nonNull(mathFile)) {
            mathFile.setLogItemStatus(item.getOffset(), LogStatusEnum.STATUS_DEAD.getCode());
        }

        return false;
    }

}
