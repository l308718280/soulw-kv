package com.soulw.kv.node.web.controller;

import com.google.common.base.Preconditions;
import com.soulw.kv.node.core.log.model.LogItem;
import com.soulw.kv.node.core.log.model.LogStatusEnum;
import com.soulw.kv.node.core.log.model.LogWriter;
import com.soulw.kv.node.web.enums.ErrorCode;
import com.soulw.kv.node.web.vo.Result;
import com.soulw.kv.node.web.vo.TupleVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 11:39
 */
@RestController
@RequestMapping("/master")
public class MasterController {

    @Resource
    private LogWriter logWriter;

    @PostMapping("/set")
    public Result<Boolean> set(@RequestBody Map<String, String> kvs) {
        for (Map.Entry<String, String> entry : kvs.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            Preconditions.checkState(StringUtils.isNotEmpty(k), "key is empty");
            Preconditions.checkState(StringUtils.isNotEmpty(v), "value is empty");
            if (!logWriter.addItem(new LogItem(LogStatusEnum.STATUS_ALIVE.getCode(),
                    System.currentTimeMillis(),
                    new TupleVO(k, v).serialize()))) {
                return Result.fail(ErrorCode.SYSTEM_ERROR.getCode(), "fail to write data");
            }
        }
        return Result.success(true);
    }

}
