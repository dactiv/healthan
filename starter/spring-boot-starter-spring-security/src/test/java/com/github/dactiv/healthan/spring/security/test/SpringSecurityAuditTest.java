package com.github.dactiv.healthan.spring.security.test;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.audit.config.ControllerAuditProperties;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringSecurityAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationProperties authenticationProperties;

    @Autowired
    private RememberMeProperties rememberMeProperties;

    @Autowired
    private ControllerAuditProperties controllerAuditProperties;

    @Autowired
    private CacheManager cacheManager;

    @Test
    public void testAuditFunction() throws Exception {

        MockHttpSession session = new MockHttpSession();

        Cookie cookie = mockMvc
                .perform(
                        post(authenticationProperties.getLoginProcessingUrl())
                                .param(authenticationProperties.getUsernameParamName(),"test")
                                .param(authenticationProperties.getPasswordParamName(),"123456")
                                .header(authenticationProperties.getTypeHeaderName(), "test")
                                .session(session)
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
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}}]}"));

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
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}}]}"));

        mockMvc
                .perform(get("/operate/isAuthenticated"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{\"message\":\"Unauthorized\"}"));

        Cookie newCookie = mockMvc
                .perform(get("/operate/isAuthenticated").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"isAuthenticated\"}"))
                .andReturn()
                .getResponse()
                .getCookie(rememberMeProperties.getCookieName());

        if (Objects.nonNull(newCookie)) {
            cookie = newCookie;
        }

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}},{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":true}}}]}"));

        newCookie = mockMvc
                .perform(get("/operate/permsOperate").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"permsOperate\"}"))
                .andReturn()
                .getResponse()
                .getCookie(rememberMeProperties.getCookieName());

        if (Objects.nonNull(newCookie)) {
            cookie = newCookie;
        }

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}},{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":true}}}]}"));

        newCookie = mockMvc
                .perform(get("/operate/isFullyAuthenticated").cookie(cookie))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{\"message\":\"Access is denied\"}"))
                .andReturn()
                .getResponse()
                .getCookie(rememberMeProperties.getCookieName());

        if (Objects.nonNull(newCookie)) {
            cookie = newCookie;
        }

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}},{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":true}}}]}"));

        mockMvc
                .perform(get("/operate/pluginTestPermsOperate").cookie(cookie))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{\"message\":\"Access is denied\"}"))
                .andReturn()
                .getResponse()
                .getCookie(rememberMeProperties.getCookieName());

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}},{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":true}}}]}"));

        mockMvc
                .perform(get("/operate/isFullyAuthenticated").session(session))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"isFullyAuthenticated\"}"));

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}},{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":true}}}]}"));

        mockMvc
                .perform(get("/operate/pluginTestPermsOperate").session(session))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"pluginTestPermsOperate\"}"));

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}},{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":true}}}]}"));

        mockMvc
                .perform(
                        post("/operate/pluginTestPermsPostAuditOperate")
                                .param(authenticationProperties.getUsernameParamName(),"test")
                                .param(authenticationProperties.getPasswordParamName(),"test")
                                .header(authenticationProperties.getTypeHeaderName(), "audit")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"pluginTestPermsGetAuditOperate\"}"));

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}},{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":true}}},{\"principal\":\"test:1:test\",\"type\":\"" + controllerAuditProperties.getAuditPrefixName() + "_OperateController_pluginTestPermsGetAuditOperate_SUCCESS\",\"data\":{\"header\":{\"X-AUTHENTICATION-TYPE\":\"audit\"},\"parameter\":{\"username\":[\"test\"],\"password\":[\"test\"]}}}]}"));

        mockMvc
                .perform(get("/operate/pluginAnyPermsOperate").session(session))
                .andExpect(status().is4xxClientError())
                .andExpect(content().json("{\"message\":\"Access is denied\"}"));
    }

}
