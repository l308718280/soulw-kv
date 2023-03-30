package com.soulw.kv.node.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HttpUtilsTest {

    @Test
    void post() {
        HttpUtils.ResponseVO resp = HttpUtils.post("https://www.baidu.com", "hello world...", null);
        assertNotNull(resp);

        System.out.println(resp.getBodyStr());
    }
}