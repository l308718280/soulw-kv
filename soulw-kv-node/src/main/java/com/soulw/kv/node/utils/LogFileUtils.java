package com.soulw.kv.node.utils;

import com.soulw.kv.node.core.log.model.FileNameVO;

/**
 * Created by SoulW on 2023/3/29.
 *
 * @author SoulW
 * @since 2023/3/29 15:50
 */
public class LogFileUtils {

    /**
     * 前缀
     */
    private static final String PREFIX = "soulwka-";
    private static final String SUFFIX = ".log";

    /**
     * 生成文件名称
     *
     * @param index 索引
     * @return 结果
     */
    public static String generateFileName(Integer index) {
        return PREFIX + index + SUFFIX;
    }

    /**
     * 解析
     *
     * @param fileName 文件名称
     * @return 结果
     */
    public static FileNameVO resolve(String fileName) {
        return new FileNameVO(fileName, Integer.valueOf(fileName.replace(PREFIX, "")
                .replace(SUFFIX, "")));
    }
}
