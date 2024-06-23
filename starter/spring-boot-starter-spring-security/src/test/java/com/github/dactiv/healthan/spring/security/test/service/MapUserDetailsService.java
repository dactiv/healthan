package com.github.dactiv.healthan.spring.security.test.service;

import com.github.dactiv.healthan.spring.security.authentication.AbstractUserDetailsService;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.healthan.spring.security.entity.SecurityUserDetails;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MapUserDetailsService extends AbstractUserDetailsService implements InitializingBean {

    private static final Map<String, SecurityUserDetails> USER_DETAILS = Collections.synchronizedMap(new HashMap<>());

    private final PasswordEncoder passwordEncoder;

    public MapUserDetailsService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setAuthenticationProperties(AuthenticationProperties authenticationProperties) {
        super.setAuthenticationProperties(authenticationProperties);
    }

    @Autowired
    public void setRememberMeProperties(RememberMeProperties rememberMeProperties) {
        super.setRememberMeProperties(rememberMeProperties);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        USER_DETAILS.put("test", new SecurityUserDetails(1, "test", getPasswordEncoder().encode("123456")));
    }

    @Override
    public SecurityUserDetails getAuthenticationUserDetails(RequestAuthenticationToken token) throws AuthenticationException {
        return USER_DETAILS.get(token.getPrincipal().toString());
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
