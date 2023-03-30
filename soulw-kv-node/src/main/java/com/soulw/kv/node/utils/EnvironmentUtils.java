package com.soulw.kv.node.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.env.Environment;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 21:06
 */
public class EnvironmentUtils {
    private static final String CODE_SERVER_PORT = "server.port";

    private static final Integer DEFAULT_PORT = 8080;

    /**
     * 获取端口
     *
     * @param environment 环境变量
     * @return 结果
     */
    public static Integer getPort(Environment environment) {
        return ObjectUtils.defaultIfNull(environment.getProperty(CODE_SERVER_PORT, Integer.class), DEFAULT_PORT);
    }
}
