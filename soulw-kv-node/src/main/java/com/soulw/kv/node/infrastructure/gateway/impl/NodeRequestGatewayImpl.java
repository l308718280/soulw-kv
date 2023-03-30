package com.soulw.kv.node.infrastructure.gateway.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.soulw.kv.node.core.cluster.gateway.NodeRequestGateway;
import com.soulw.kv.node.core.cluster.model.Node;
import com.soulw.kv.node.core.cluster.model.VoteApply;
import com.soulw.kv.node.utils.HttpUtils;
import com.soulw.kv.node.web.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Created by SoulW on 2023/3/30.
 *
 * @author SoulW
 * @since 2023/3/30 20:47
 */
@Component
@Slf4j
public class NodeRequestGatewayImpl implements NodeRequestGateway {

    private static final String URL_PATTERN_REQUEST_VOTE = "http://%s:%s/slave/request/vote";
    private static final String URL_PATTERN_HEARTBEAT = "http://%s:%s/slave/heartbeat";

    @Override
    public boolean requestVote(Node node, VoteApply applyRequest) {
        try {
            HttpUtils.ResponseVO resp = HttpUtils.post(String.format(URL_PATTERN_REQUEST_VOTE, node.getIp(), node.getPort()), applyRequest, null);
            if (resp.isSuccess()) {
                return resolveData(resp);
            }
        } catch (Exception e) {
            log.error("request vote error", e);
        }
        return false;
    }

    @Override
    public boolean heartbeat(Node node, VoteApply applyRequest) {
        try {
            HttpUtils.ResponseVO resp = HttpUtils.post(String.format(URL_PATTERN_HEARTBEAT, node.getIp(), node.getPort()), applyRequest, null);
            if (resp.isSuccess()) {
                return resolveData(resp);
            }
        } catch (Exception e) {
            log.error("request heartbeat error", e);
        }
        return false;
    }

    private boolean resolveData(HttpUtils.ResponseVO resp) {
        Result<Boolean> result = JSON.parseObject(resp.getBodyStr(), new TypeReference<Result<Boolean>>() {
        });
        if (Objects.isNull(result) || !result.getSuccess()) {
            return false;
        }
        return Boolean.TRUE.equals(result.getData());
    }
}
