package com.github.dactiv.healthan.spring.security.test;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.util.Collection;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SpringSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationProperties authenticationProperties;

    @Autowired
    private RememberMeProperties rememberMeProperties;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testSpringSecurityFunction() throws Exception {
        Cookie cookie = mockMvc
                .perform(
                        post(authenticationProperties.getLoginProcessingUrl())
                                .param(authenticationProperties.getUsernameParamName(),"test")
                                .param(authenticationProperties.getPasswordParamName(),"123456")
                                .header(authenticationProperties.getTypeHeaderName(), "test")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":200}"))
                .andExpect(content().json("{\"data\":{\"type\":\"test\", \"principal\":{\"id\":1,\"username\":\"test\"}}}"))
                .andReturn()
                .getResponse()
                .getCookie(rememberMeProperties.getCookieName());

        String authenticationCacheName = authenticationProperties.getAuthenticationCache().getName("test:test");
        SecurityPrincipal principal = cacheManager.getSecurityPrincipal(CacheProperties.of(authenticationCacheName));
        Assertions.assertNotNull(principal);

        String authorizationCacheName = authenticationProperties.getAuthorizationCache().getName("test:1:test");
        Collection<GrantedAuthority> authorities = cacheManager.getGrantedAuthorities(CacheProperties.of(authorizationCacheName));
        Assertions.assertEquals(1, authorities.size());

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\"}]}"));

        mockMvc
                .perform(
                        post(authenticationProperties.getLoginProcessingUrl())
                                .param(authenticationProperties.getUsernameParamName(),"test")
                                .param(authenticationProperties.getPasswordParamName(),"xxxxxx")
                                .header(authenticationProperties.getTypeHeaderName(), "test")
                )
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":500}"));

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\"},{\"principal\":\"test:test\",\"type\":\"AUTHENTICATION_FAILURE\"}]}"));

        mockMvc
                .perform(get("/operate/isAuthenticated").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"isAuthenticated\"}"));
    }

}
