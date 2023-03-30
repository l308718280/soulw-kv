package com.soulw.kv.node.web.controller;

import com.soulw.kv.node.core.cluster.model.Cluster;
import com.soulw.kv.node.core.cluster.model.VoteApply;
import com.soulw.kv.node.core.log.model.LogItem;
import com.soulw.kv.node.web.enums.ErrorCode;
import com.soulw.kv.node.web.vo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 11:35
 */
@RestController
@RequestMapping("/slave")
public class SlaveController {

    @Resource
    private Cluster cluster;

    /**
     * 同步日志
     *
     * @param logItem 日志项
     * @return 结果
     */
    public Result<Boolean> syncLog(LogItem logItem) {

        return null;
    }

    @PostMapping("/heartbeat")
    public Result<Boolean> heartbeat(@RequestBody VoteApply apply) {
        if (Objects.isNull(apply) || Objects.isNull(apply.getCurrentNode())) {
            return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "missing request params");
        }

        cluster.receiveHeartbeat(apply);
        return Result.success(true);
    }

    @PostMapping("/request/vote")
    public Result<Boolean> requestVote(@RequestBody VoteApply apply) {
        if (Objects.isNull(apply) || Objects.isNull(apply.getCurrentNode())) {
            return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "missing request params");
        }

        if (!cluster.getCurrentNode().doVote(apply)) {
            return Result.fail(ErrorCode.VOTE_ERROR.getCode(), "not accept vote");
        }
        return Result.success(true);
    }

}
