package com.github.dactiv.healthan.canal.config;

import com.github.dactiv.healthan.commons.id.IdEntity;

import java.io.Serial;

/**
 * canal 实例配置
 *
 * @author maurice.chen
 */
public class CanalInstanceProperties extends IdEntity<Long>  {

    @Serial
    private static final long serialVersionUID = 9075497464601557570L;

    /**
     * 实例名称
     */
    private String name;

    /**
     * 连接账户
     */
    private String username;

    /**
     * 连接密码
     */
    private String password;

    /**
     * 连接端口
     */
    private int tcpPort = 11111;

    /**
     * 连接地址
     */
    private String host;

    public CanalInstanceProperties() {
    }

    /**
     * 获取实例名称
     *
     * @return 实例名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置实例名称
     *
     * @param name 实例名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取连接账户
     *
     * @return 连接账户
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置连接账户
     *
     * @param username 连接账户
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取连接密码
     *
     * @return 连接密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置连接密码
     *
     * @param password 连接密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
