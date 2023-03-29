package com.soulw.kv.node;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 16:18
 */
@Slf4j
public abstract class BaseTest {

    /**
     * 临时路径
     */
    public static final String TMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "soulw" + File.separator;

    /**
     * 获取一个文件
     *
     * @param fileName 文件名称
     * @return 路径
     */
    public String getFile(String fileName) {
        String resp = TMP_DIR + fileName;
        log.info("BaseTest.getFile() fileName: {}", fileName);
        return resp;
    }
}
