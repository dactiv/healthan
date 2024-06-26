package com.github.dactiv.healthan.spring.security.authentication.service;

import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.security.entity.SimpleSecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.AbstractUserDetailsService;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 默认用户明细实现
 *
 * @author maurice.chen
 */
public class DefaultUserDetailsService extends AbstractUserDetailsService {

    public static final String DEFAULT_TYPES = "Default";

    private final PasswordEncoder passwordEncoder;

    public DefaultUserDetailsService(AuthenticationProperties properties,
                                     PasswordEncoder passwordEncoder) {
        setAuthenticationProperties(properties);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SecurityPrincipal getSecurityPrincipal(RequestAuthenticationToken token) throws AuthenticationException {
        return getSecurityPrincipal(token.getPrincipal().toString());
    }

    @Override
    public Collection<GrantedAuthority> getPrincipalAuthorities(SecurityPrincipal principal) {
        SecurityProperties.User user = getSpringSecurityUserConfig(principal.getUsername());
        return user.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(DEFAULT_TYPES);
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    private SecurityPrincipal getSecurityPrincipal(String username) {
        SecurityProperties.User user = getSpringSecurityUserConfig(username);
        return new SimpleSecurityPrincipal(
                DigestUtils.md5DigestAsHex(user.getName().getBytes(StandardCharsets.UTF_8)),
                user.getName(),
                user.getPassword(),
                DEFAULT_TYPES
        );
    }

    private SecurityProperties.User getSpringSecurityUserConfig(String username) {
        Optional<SecurityProperties.User> optional = getAuthenticationProperties()
                .getUsers()
                .stream()
                .filter(u -> u.getName().equals(username))
                .findFirst();

        if (!optional.isPresent()) {
            throw new BadCredentialsException("用户名密码错误");
        }

        return optional.get();
    }

}
