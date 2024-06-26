package com.github.dactiv.healthan.spring.security.test;

import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.provider.RequestAuthenticationProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
    private RedissonClient redissonClient;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void testLoginSuccess() throws Exception {

        mockMvc
                .perform(
                    post(authenticationProperties.getLoginProcessingUrl())
                        .param(authenticationProperties.getUsernameParamName(),"test")
                        .param(authenticationProperties.getPasswordParamName(),"123456")
                        .header(authenticationProperties.getTypeHeaderName(), "test")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":200}"))
                .andExpect(content().json("{\"data\":{\"username\":\"test\"}}"));

        Assertions.assertTrue(redissonClient.getBucket(RequestAuthenticationProvider.DEFAULT_AUTHENTICATION_KEY_NAME + "test:test").isExists());
        Assertions.assertFalse(redissonClient.getSet(RequestAuthenticationProvider.DEFAULT_AUTHORIZATION_KEY_NAME + "test:1:test").isEmpty());

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\"}]}"));

        mockMvc
                .perform(get("/operate/isAuthenticated"))
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
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\"},{\"principal\":\"test:test\",\"type\":\"AUTHENTICATION_FAILURE\"}]}"));
    }

}
