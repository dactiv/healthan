package com.github.dactiv.healthan.canal.config;

import com.github.dactiv.healthan.commons.TimeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * canal 配置信息
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.canal")
public class CanalProperties {

    /**
     * 解析日志的线程数量，订阅消息也通过该值去开启线程
     */
    private int parallelBuildThreadSize = 8;

    /**
     * 定于获取链接出现异常时重试时间
     */
    private TimeProperties exceptionRetryTime = TimeProperties.ofMinutes(1);

    /**
     * 实例操作延迟时间
     */
    private TimeProperties instanceOptionDelayTime = TimeProperties.ofSeconds(15);

    /**
     * canal 连接配置
     */
    private List<CanalInstanceProperties> instances = new ArrayList<>();

    /**
     * 尝试获取 canal 的消息大小
     */
    private int batchSize = 5 * 1024;

    public CanalProperties() {

    }

    /**
     * 获取解析日志的线程数量，订阅消息也通过该值去开启线程
     *
     * @return 解析日志的线程数量，订阅消息也通过该值去开启线程
     */
    public int getParallelBuildThreadSize() {
        return parallelBuildThreadSize;
    }

    /**
     * 设置解析日志的线程数量，订阅消息也通过该值去开启线程
     *
     * @param parallelBuildThreadSize 解析日志的线程数量，订阅消息也通过该值去开启线程
     */
    public void setParallelBuildThreadSize(int parallelBuildThreadSize) {
        this.parallelBuildThreadSize = parallelBuildThreadSize;
    }

    /**
     * 获取定于获取链接出现异常时重试时间
     *
     * @return 定于获取链接出现异常时重试时间
     */
    public TimeProperties getExceptionRetryTime() {
        return exceptionRetryTime;
    }

    /**
     * 设置定于获取链接出现异常时重试时间
     *
     * @param exceptionRetryTime 定于获取链接出现异常时重试时间
     */
    public void setExceptionRetryTime(TimeProperties exceptionRetryTime) {
        this.exceptionRetryTime = exceptionRetryTime;
    }

    /**
     * 获取实例操作延迟时间
     *
     * @return 实例操作延迟时间
     */
    public TimeProperties getInstanceOptionDelayTime() {
        return instanceOptionDelayTime;
    }

    /**
     * 设置实例操作延迟时间
     *
     * @param instanceOptionDelayTime 实例操作延迟时间
     */
    public void setInstanceOptionDelayTime(TimeProperties instanceOptionDelayTime) {
        this.instanceOptionDelayTime = instanceOptionDelayTime;
    }

    /**
     * 获取尝试获取 canal 的消息大小
     *
     * @return 尝试获取 canal 的消息大小
     */
    public int getBatchSize() {
        return batchSize;
    }

    /**
     * 设置尝试获取 canal 的消息大小
     *
     * @param batchSize 尝试获取 canal 的消息大小
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * 获取 canal 连接配置
     *
     * @return canal 连接配置
     */
    public List<CanalInstanceProperties> getInstances() {
        return instances;
    }

    /**
     * 设置 canal 连接配置
     *
     * @param instances canal 连接配置
     */
    public void setInstances(List<CanalInstanceProperties> instances) {
        this.instances = instances;
    }
}
