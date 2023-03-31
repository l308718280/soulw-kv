package com.soulw.kv.node.infrastructure.gateway.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Preconditions;
import com.soulw.kv.node.core.cluster.gateway.NodeRequestGateway;
import com.soulw.kv.node.core.cluster.model.Node;
import com.soulw.kv.node.core.cluster.model.VoteApply;
import com.soulw.kv.node.core.log.model.LogItem;
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
    private static final String URL_PATTERN_SYNC_LOG = "http://%s:%s/slave/syncLog";

    @Override
    public boolean requestVote(Node node, VoteApply applyRequest) {
        try {
            HttpUtils.ResponseVO resp = HttpUtils.post(String.format(URL_PATTERN_REQUEST_VOTE, node.getIp(), node.getPort()), applyRequest, null);
            Preconditions.checkState(resp.isSuccess(), "response error");
            return resolveData(resp);
        } catch (Exception e) {
            log.error("request vote error", e);
            return false;
        }
    }

    @Override
    public boolean heartbeat(Node node, VoteApply applyRequest) {
        try {
            HttpUtils.ResponseVO resp = HttpUtils.post(String.format(URL_PATTERN_HEARTBEAT, node.getIp(), node.getPort()), applyRequest, null);
            Preconditions.checkState(resp.isSuccess(), "response error");
            return resolveData(resp);
        } catch (Exception e) {
            log.error("heartbeat error", e);
            return false;
        }
    }

    @Override
    public void syncLog(Node node, LogItem logItem) {
        HttpUtils.ResponseVO resp = HttpUtils.post(String.format(URL_PATTERN_SYNC_LOG, node.getIp(), node.getPort()), logItem, null);
        Preconditions.checkState(resp.isSuccess(), "response error");
        boolean flag = resolveData(resp);
        Preconditions.checkState(flag, "sync log error");
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
