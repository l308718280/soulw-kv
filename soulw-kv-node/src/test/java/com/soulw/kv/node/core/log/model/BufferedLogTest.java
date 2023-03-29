package com.soulw.kv.node.core.log.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.soulw.kv.node.BaseTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BufferedLogTest extends BaseTest {

    @Test
    void addLogItem() throws Exception {
        BufferedLog log = new BufferedLog(getFile("p.log"), 1);
        log.init();

        LogItem item = new LogItem();
        item.setStatus(LogStatusEnum.STATUS_ALIVE.getCode());
        item.setData("hello world".getBytes(StandardCharsets.UTF_8));
        item.setTimestamp(System.currentTimeMillis());

        for (int i = 0; i < 10; i++) {
            assertTrue(log.addLogItem(item));
        }
        log.flush();
    }

    @Test
    void setLogItemStatus() throws IOException {
        BufferedLog log = new BufferedLog(getFile("p.log"), 1);
        log.init();
        LogItem newItem = new LogItem(LogStatusEnum.STATUS_ALIVE.getCode(), System.currentTimeMillis(), null);
        log.addLogItem(newItem);
        log.flush();

        log.setLogItemStatus(newItem.getOffset(), LogStatusEnum.STATUS_DEAD.getCode());
        List<LogItem> resp = log.getItems(newItem.getOffset(), 1);
        System.out.println(JSON.toJSONString(resp, SerializerFeature.PrettyFormat));
        assertEquals(LogStatusEnum.STATUS_DEAD.getCode(), resp.get(0).getStatus());
    }

    @Test
    void getItems() throws IOException {
        BufferedLog log = new BufferedLog(getFile("p.log"), 1);
        log.init();
        List<LogItem> items = log.getItems(BufferedLog.PRESERVED_LEN, 5);
        System.out.println(JSON.toJSONString(items, SerializerFeature.PrettyFormat));
        assertEquals(5, items.size());
    }
}