package com.soulw.kv.node.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkUtilsTest {

    @Test
    void loadIp() {
        String ip = NetworkUtils.loadIp();
        System.out.println("current ip: " + ip);
        assertTrue(StringUtils.isNotBlank(ip));
    }
}