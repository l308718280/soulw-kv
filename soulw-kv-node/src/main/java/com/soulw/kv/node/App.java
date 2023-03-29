package com.soulw.kv.node;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 10:56
 */
@SpringBootApplication(scanBasePackages = "com.soulw.kv")
public class App {

    /**
     * 应用启动
     *
     * @param args 请求参数
     */
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
