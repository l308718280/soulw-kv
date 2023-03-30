package com.soulw.kv.node.web.controller;

import com.soulw.kv.node.web.vo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 11:39
 */
@RestController
@RequestMapping("/master")
public class MasterController {

    /**
     * 回复消息
     *
     * @return 回复
     */
    @PostMapping("/ack")
    public Result<Void> ack() {
        return Result.success(null);
    }

}
