package com.github.dactiv.healthan.canal.domain;

import com.github.dactiv.healthan.commons.id.IdEntity;

import java.util.Date;

/**
 * canal 集群数据 dto
 *
 * @author maurice.chen
 */
public class CanalCluster extends IdEntity<Long> {

    private static final long serialVersionUID = 7325407486272402604L;

    /**
     * 集群名称
     */
    private String name;
    /**
     * zk hosts
     */
    private String zkHosts;
    /**
     * 修改时间
     */
    private Date modifiedTime;

    public CanalCluster() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZkHosts() {
        return zkHosts;
    }

    public void setZkHosts(String zkHosts) {
        this.zkHosts = zkHosts;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
