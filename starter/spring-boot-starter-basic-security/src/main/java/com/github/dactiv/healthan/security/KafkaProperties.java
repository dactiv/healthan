package com.github.dactiv.healthan.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * kafka 配置信息
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.security.audit.index.kafka")
public class KafkaProperties implements Serializable {

    /**
     * 接收的主题
     */
    private String topic;

    /**
     * 是否启用 kafka 发送审计数据
     */
    private boolean enabled = false;

    public KafkaProperties() {
    }

    /**
     * 是否启用 kafka 发送审计数据
     *
     * @return true 是，否则 false
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 kafka 发送审计数据
     *
     * @param enabled true 是，否则 false
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取接收的主题
     *
     * @return 接收的主题
     */
    public String getTopic() {
        return topic;
    }

    /**
     * 设置接收的主题
     *
     * @param topic 接收的主题
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }
}
