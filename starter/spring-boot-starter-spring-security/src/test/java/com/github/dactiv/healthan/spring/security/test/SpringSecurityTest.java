package com.github.dactiv.healthan.spring.security.test;

import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SpringSecurityTest {


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthenticationProperties authenticationProperties;

    @Autowired
    private RememberMeProperties rememberMeProperties;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void testLoginSuccess() throws Exception {

        String rememberMeCookie = mockMvc
                .perform(
                    post(authenticationProperties.getLoginProcessingUrl())
                        .param(authenticationProperties.getUsernameParamName(),"test")
                        .param(authenticationProperties.getPasswordParamName(),"123456")
                        .header(authenticationProperties.getTypeHeaderName(), "test")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":200}"))
                .andExpect(content().json("{\"data\":{\"username\":\"test\"}}"))
                .andReturn()
                .getResponse()
                .getCookie(rememberMeProperties.getCookie().getName())
                .getValue();

        Cookie cookie = new Cookie(rememberMeProperties.getCookie().getName(), rememberMeCookie);
        Assertions.assertTrue(StringUtils.isNotEmpty(cookie.getValue()));

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"id\":1,\"username\":\"test\",\"roleAuthorities\":[],\"status\":{\"name\":\"启用\",\"value\":1},\"type\":\"test\",\"resourceAuthorityStrings\":[]}}}]}"));

        mockMvc
                .perform(get("/operate/isAuthenticated").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"isAuthenticated\"}"));

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
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"id\":1,\"username\":\"test\",\"roleAuthorities\":[],\"status\":{\"name\":\"启用\",\"value\":1},\"type\":\"test\",\"resourceAuthorityStrings\":[]}}},{\"principal\":\"test:test\",\"type\":\"AUTHENTICATION_FAILURE\"}]}"));
    }

}
