package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ip 认证过滤器
 *
 * @author maurice.chen
 */
public class IpAuthenticationFilter extends OncePerRequestFilter {

    public static final Logger LOGGER = LoggerFactory.getLogger(IpAuthenticationFilter.class);

    public static final GrantedAuthority IP_WHITELIST_AUTHORITY = new SimpleGrantedAuthority("IP_WHITELIST");

    private final AuthenticationProperties authenticationProperties;

    public IpAuthenticationFilter(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        List<String> ips = authenticationProperties
                .getIpAuthentications()
                .stream()
                .filter(ip -> new AntPathRequestMatcher(ip.getUrl()).matches(request))
                .flatMap(ip -> ip.getIps().stream())
                .collect(Collectors.toList());

        String remoteIp = SpringMvcUtils.getIpAddress(request);

        if (CollectionUtils.isEmpty(ips)) {
            filterChain.doFilter(request, response);
            return ;
        }

        if (ips.contains(remoteIp)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (Objects.isNull(authentication)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(remoteIp, this.getClass().getName(), Collections.singletonList(IP_WHITELIST_AUTHORITY));
                WebAuthenticationDetails webAuthenticationDetails = new WebAuthenticationDetails(request);
                authenticationToken.setDetails(webAuthenticationDetails);

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
            return ;
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} 只允许: {} 的 ip 才能访问，当前 ip 为: {}", request.getRequestURI(), ips, remoteIp);
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Casts.writeValueAsString(RestResult.of(HttpStatus.UNAUTHORIZED.getReasonPhrase(), HttpStatus.UNAUTHORIZED.value(), String.valueOf(HttpStatus.UNAUTHORIZED.value()))));
    }
}
