package com.soulw.kv.node.core.log.model;

import com.soulw.kv.node.BaseTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class LogWriterTest extends BaseTest {

    @Test
    void addItem() {
        LogWriter writer = new LogWriter(getFile("log"));
        writer.init();

        for (int i = 0; i < 100; i++) {
            boolean resp = writer.addItem(new LogItem((short) 1, System.currentTimeMillis(), "hello world1".getBytes(StandardCharsets.UTF_8)));
            assertTrue(resp);
        }
    }

}