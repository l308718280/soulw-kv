package com.soulw.kv.node.web.controller;

import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.core.log.model.LogItem;
import com.soulw.kv.node.core.log.model.LogWriter;
import com.soulw.kv.node.web.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 11:35
 */
@RestController
@RequestMapping("/slave")
public class SlaveController {

    /**
     * 同步日志
     *
     * @param logItem 日志项
     * @return 结果
     */
    public Result<Boolean> syncLog(LogItem logItem) {

        return null;
    }


}
