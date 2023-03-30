package com.soulw.kv.node.core.log.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.soulw.kv.node.BaseTest;
import com.soulw.kv.node.core.cluster.model.Cluster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LogWriterTest extends BaseTest {

    private LogWriter writer;

    @BeforeEach
    void setUp() {
        writer = new LogWriter(getFile("log"), Mockito.mock(Cluster.class));
        writer.init();
    }

    @Test
    void addItem() {

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

    @Test
    void overrideLog() {
        LogItem item = new LogItem((short) 1, System.currentTimeMillis(), "hello".getBytes(StandardCharsets.UTF_8));
        item.setFileIndex(9);
        item.setOffset(999);
        boolean resp = writer.overrideLog(item);
        assertTrue(resp);

        LogItem overrideLog = writer.getItem(item.getFileIndex(), item.getOffset());
        System.out.println(JSON.toJSONString(overrideLog, SerializerFeature.PrettyFormat));
        assertNotNull(overrideLog);
    }
}