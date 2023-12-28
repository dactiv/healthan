package com.github.dactiv.healthan.captcha;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 简单的消息类型实体实现
 *
 * @author maurice
 */
public class SimpleMessageType implements MessageType {

    /**
     * 消息类型
     */
    @NotNull(message = "消息类型不能为空")
    private String messageType;

    /**
     * 消息内容中的 spring el 值
     */
    private Map<String, Object> messageSpringElValue = new LinkedHashMap<>();

    public SimpleMessageType() {
    }

    @Override
    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Map<String, Object> getMessageSpringElValue() {
        return messageSpringElValue;
    }

    public void setMessageSpringElValue(Map<String, Object> messageSpringElValue) {
        this.messageSpringElValue = messageSpringElValue;
    }
}
