package com.github.dactiv.healthan.canal.resolver;

import com.github.dactiv.healthan.commons.domain.AckMessage;

import java.util.function.Consumer;

/**
 * canal 行数据变更通知解析器
 *
 * @author maurice.chen
 */
public interface CanalRowDataChangeNoticeResolver {

    /**
     * 是否支持通知实体
     *
     * @param entity canal 行数据变更通知记录实体
     *
     * @return true 是，否则 false
     */
    boolean isSupport(AckMessage entity);

    /**
     * 发送通知
     *
     * @param entity canal 行数据变更通知记录实体
     * @param consumer 当数据发送比昂更后回调次接口
     */
    void send(AckMessage entity, Consumer<AckMessage> consumer);
}
