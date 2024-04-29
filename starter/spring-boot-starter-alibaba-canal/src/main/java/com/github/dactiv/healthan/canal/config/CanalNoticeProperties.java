package com.github.dactiv.healthan.canal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 通知配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.canal.notice")
public class CanalNoticeProperties {

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 需要扫描的包路径
     */
    private List<String> basePackages;

    public CanalNoticeProperties() {
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }
}
