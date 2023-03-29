package com.soulw.kv.node.core.log.model;

import com.soulw.kv.node.BaseTest;
import com.soulw.kv.node.core.cluster.model.Cluster;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LogWriterTest extends BaseTest {

    @Test
    void addItem() {
        LogWriter writer = new LogWriter(getFile("log"), new Cluster());
        writer.init();

        int fileIndex = 0;
        int offset = 0;
        for (int i = 0; i < 100; i++) {
            LogItem item = new LogItem((short) 1, System.currentTimeMillis(), "hello".getBytes(StandardCharsets.UTF_8));
            boolean resp = writer.addItem(item);
            assertTrue(resp);

            if (i == 50) {
                fileIndex = item.getFileIndex();
                offset = item.getOffset();
            }
        }

        LogItem delItem = writer.getItem(fileIndex, offset);
        writer.delItem(delItem);

        LogItem newDelItem = writer.getItem(fileIndex, offset);
        assertEquals(LogStatusEnum.STATUS_DEAD.getCode(), newDelItem.getStatus());
    }

}