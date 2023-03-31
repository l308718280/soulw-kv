package com.soulw.kv.node.web.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 11:36
 */
@Data
@Accessors(chain = true)
public class Result<T> implements Serializable {
    public static final String CODE_SUCCESS = "0";
    private Boolean success;
    private String code;
    private String msg;
    private T data;

    /**
     * 成功
     *
     * @param data 数据
     * @param <T>  类型
     * @return 结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>().setSuccess(true)
                .setCode(CODE_SUCCESS)
                .setData(data);
    }

    /**
     * 失败
     *
     * @param code 错误码
     * @param msg  消息
     * @param <T>  类型
     * @return 结果
     */
    public static <T> Result<T> fail(String code, String msg) {
        return new Result<T>().setSuccess(false)
                .setCode(code)
                .setMsg(msg);
    }
}
