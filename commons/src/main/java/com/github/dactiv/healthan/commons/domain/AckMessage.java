package com.github.dactiv.healthan.commons.domain;

import com.github.dactiv.healthan.commons.domain.body.AckResponseBody;
import com.github.dactiv.healthan.commons.domain.meta.ProtocolMeta;
import com.github.dactiv.healthan.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.healthan.commons.retry.Retryable;

import java.io.Serializable;
import java.util.Map;

/**
 * 确认消息接口，用于发送指定协议的消息信息并根据响应结果来确定是否需要重新发送消息使用
 *
 * @author maurice.chen
 */
public interface AckMessage extends Serializable, Retryable, ExecuteStatus.Body, ProtocolMeta {

    /**
     * 获取消息请求体
     *
     * @return 消息请求体
     */
    Map<String, Object> getRequestBody();

    /**
     * 获取 ack 响应体
     *
     * @return ack 响应体
     */
    AckResponseBody getResponseBody();

    /**
     * 设置 ack 响应体
     *
     * @param ackResponseBody ack 响应体
     */
    void setResponseBody(AckResponseBody ackResponseBody);
}
