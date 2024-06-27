package com.github.dactiv.healthan.spring.security.test.service;

import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.security.entity.support.SimpleSecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.AbstractTypeSecurityPrincipalService;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MapTypeSecurityPrincipalService extends AbstractTypeSecurityPrincipalService implements InitializingBean {

    private static final Map<String, SimpleSecurityPrincipal> USER_DETAILS = Collections.synchronizedMap(new HashMap<>());

    private final PasswordEncoder passwordEncoder;

    public MapTypeSecurityPrincipalService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setAuthenticationProperties(AuthenticationProperties authenticationProperties) {
        super.setAuthenticationProperties(authenticationProperties);
    }

    @Override
    public void afterPropertiesSet()  {
        USER_DETAILS.put("test", new SimpleSecurityPrincipal(1, getPasswordEncoder().encode("123456"), "test"));
    }

    @Override
    public SecurityPrincipal getSecurityPrincipal(RequestAuthenticationToken token) throws AuthenticationException {
        return USER_DETAILS.get(token.getPrincipal().toString());
    }

    @Override
    public Collection<GrantedAuthority> getPrincipalAuthorities(RequestAuthenticationToken token, SecurityPrincipal principal) {
        Collection<GrantedAuthority> result = new LinkedHashSet<>();
        result.add(new SimpleGrantedAuthority("perms[operate]"));
        return result;
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList("test");
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
