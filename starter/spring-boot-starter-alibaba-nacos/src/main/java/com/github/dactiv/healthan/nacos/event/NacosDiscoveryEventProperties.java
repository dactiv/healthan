package com.github.dactiv.healthan.nacos.event;

import com.github.dactiv.healthan.commons.TimeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * nacoa 事件配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("spring.cloud.nacos.discovery.event")
public class NacosDiscoveryEventProperties {

    public static final String DEFAULT_SUBSCRIBE_SERVICE_CRON = "0 0/1 * * * ?";

    public static final String DEFAULT_UNSUBSCRIBE_SERVICE_CRON = "30 0/1 * * * ?";

    /**
     * 扫描并订阅服务的 cron 表达式
     */
    private String subscribeServiceCron = DEFAULT_SUBSCRIBE_SERVICE_CRON;

    /**
     * 取消订阅扫描的 cron 表达式
     */
    private String unsubscribeScanCron = DEFAULT_UNSUBSCRIBE_SERVICE_CRON;

    /**
     * 过期取消订阅时间
     */
    private TimeProperties expireUnsubscribeTime = new TimeProperties(1, TimeUnit.HOURS);

    public NacosDiscoveryEventProperties() {
    }

    /**
     * 获取扫描并订阅服务的 cron 表达式
     *
     * @return 扫描并订阅服务的 cron 表达式
     */
    public String getSubscribeServiceCron() {
        return subscribeServiceCron;
    }

    /**
     * 设置扫描并订阅服务的 cron 表达式
     *
     * @param subscribeServiceCron 扫描并订阅服务的 cron 表达式
     */
    public void setSubscribeServiceCron(String subscribeServiceCron) {
        this.subscribeServiceCron = subscribeServiceCron;
    }

    /**
     * 获取取消订阅扫描的 cron 表达式
     *
     * @return 取消订阅扫描的 cron 表达式
     */
    public String getUnsubscribeScanCron() {
        return unsubscribeScanCron;
    }

    /**
     * 设置取消订阅扫描的 cron 表达式
     *
     * @param unsubscribeScanCron 取消订阅扫描的 cron 表达式
     */
    public void setUnsubscribeScanCron(String unsubscribeScanCron) {
        this.unsubscribeScanCron = unsubscribeScanCron;
    }

    /**
     * 获取超时取消订阅时间
     *
     * @return 超时取消订阅时间
     */
    public TimeProperties getExpireUnsubscribeTime() {
        return expireUnsubscribeTime;
    }

    /**
     * 设置超时取消订阅时间
     *
     * @param expireUnsubscribeTime 超时取消订阅时间
     */
    public void setExpireUnsubscribeTime(TimeProperties expireUnsubscribeTime) {
        this.expireUnsubscribeTime = expireUnsubscribeTime;
    }
}
