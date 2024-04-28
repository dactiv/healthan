package com.github.dactiv.healthan.commons.domain.meta;

import com.github.dactiv.healthan.commons.enumerate.support.Protocol;

import java.io.Serializable;
import java.util.Map;

/**
 * 协议元数据信息
 *
 * @author maurice.chen
 */
public interface ProtocolMeta extends Serializable {

    /**
     * 获取消息协议信息
     *
     * @return 协议类型枚举
     */
    Protocol getProtocol();

    /**
     * 获取协议内容元数据信息
     *
     * @return 协议内容元数据信息
     */
    Map<String, Object> getProtocolMeta();
}
