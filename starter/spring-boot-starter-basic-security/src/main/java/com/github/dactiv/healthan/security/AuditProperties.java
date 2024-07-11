package com.github.dactiv.healthan.security;

import com.github.dactiv.healthan.security.audit.AuditType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 审计配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.authentication.audit")
public class AuditProperties {

    /**
     * 审计类型
     */
    private AuditType type = AuditType.Memory;

    /**
     * 要忽略的审计类型
     */
    private List<String> ignoreTypes;

    /**
     * 要忽略的用户名称
     */
    List<String> ignorePrincipals;

    public AuditProperties() {
    }

    /**
     * 获取审计类型
     *
     * @return 审计类型
     */
    public AuditType getType() {
        return type;
    }

    /**
     * 设置审计类型
     *
     * @param type 审计类型
     */
    public void setType(AuditType type) {
        this.type = type;
    }

    /**
     * 获取要忽略的审计类型
     *
     * @return 要忽略的审计类型集合
     */
    public List<String> getIgnoreTypes() {
        return ignoreTypes;
    }

    /**
     * 设置要忽略的审计类型
     *
     * @param ignoreTypes 要忽略的审计类型集合
     */
    public void setIgnoreTypes(List<String> ignoreTypes) {
        this.ignoreTypes = ignoreTypes;
    }

    /**
     * 获取要忽略的用户名称
     *
     * @return 要忽略的用户名称集合
     */
    public List<String> getIgnorePrincipals() {
        return ignorePrincipals;
    }

    /**
     * 设置要忽略的用户名称
     *
     * @param ignorePrincipals 要忽略的用户名称
     */
    public void setIgnorePrincipals(List<String> ignorePrincipals) {
        this.ignorePrincipals = ignorePrincipals;
    }
}
