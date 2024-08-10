package com.github.dactiv.healthan.spring.security.authentication.service;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.mybatis.plus.service.DataOwnerService;
import com.github.dactiv.healthan.spring.security.authentication.config.SecurityPrincipalDataOwnerProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;

/**
 * 安全用户数据拥有者服务实现，用于将当前用户数据隔离，形成租户数据
 *
 * @author maurice.chen
 */
public class SecurityPrincipalDataOwnerService implements DataOwnerService {

    private final SecurityPrincipalDataOwnerProperties properties;

    public SecurityPrincipalDataOwnerService(SecurityPrincipalDataOwnerProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getOwner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (AuthenticationSuccessToken.class.isAssignableFrom(authentication.getClass())) {
            AuthenticationSuccessToken token = Casts.cast(authentication);
            if (properties.getIgnorePrincipalTypes().contains(token.getPrincipalType())) {
                return null;
            }
            if (Principal.class.isAssignableFrom(token.getPrincipal().getClass())) {
                Principal principal = Casts.cast(token.getPrincipal());
                if (properties.getIgnorePrincipalNames().contains(principal.getName())) {
                    return null;
                }
            } else if (properties.getIgnorePrincipalNames().contains(token.getPrincipal().toString())){
                return null;
            }
        }
        return authentication.getName();
    }
}
