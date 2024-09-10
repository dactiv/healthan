package com.github.dactiv.healthan.spring.security.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.OAuth2Properties;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SpringSecurityOauth2Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationProperties authenticationProperties;

    @Autowired
    private OAuth2Properties oAuth2Properties;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test() throws Exception {
        String authenticationCacheName = authenticationProperties.getAuthenticationCache().getName("test:test");
        String authorizationCacheName = authenticationProperties.getAuthorizationCache().getName("test:1:test");

        redissonClient.getBucket(authenticationCacheName).delete();
        redissonClient.getBucket(authorizationCacheName).delete();

        MockHttpSession session = new MockHttpSession();

        mockMvc
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
                .andExpect(content().json("{\"data\":{\"type\":\"test\", \"principal\":{\"id\":1,\"username\":\"test\"}}}"));

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}}]}"));

        String codeJson = mockMvc
                .perform(
                        get(oAuth2Properties.getAuthorizeEndpoint())
                                .queryParam(OAuth2ParameterNames.CLIENT_ID,"test")
                                .queryParam(OAuth2ParameterNames.RESPONSE_TYPE,OAuth2ParameterNames.CODE)
                                .queryParam(OAuth2ParameterNames.SCOPE, "openid profile")
                                .queryParam(OAuth2ParameterNames.REDIRECT_URI, "http://www.domain.com")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":200}"))
                .andExpect(content().json("{\"data\":{\"authenticated\": true, \"clientId\": \"test\",\"scopes\": [\"openid\", \"profile\"]}}"))
                .andExpect(jsonPath("$.data.authorizationCode.tokenValue").value(not(emptyOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RestResult<Map<String, Object>> codeResultJson = Casts.readValue(codeJson, new TypeReference<>() {});
        Map<String, Object> authorizationCodeMap = Casts.cast(codeResultJson.getData().get("authorizationCode"));
        String tokenValue = authorizationCodeMap.get("tokenValue").toString();

        String accessTokenJson = mockMvc
                .perform(
                        post(oAuth2Properties.getTokenEndpoint())
                                .param(OAuth2ParameterNames.CLIENT_ID,"test")
                                .param(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.AUTHORIZATION_CODE.getValue())
                                .param(OAuth2ParameterNames.CODE,tokenValue)
                                .param(OAuth2ParameterNames.CLIENT_SECRET,"123456")
                                .param(OAuth2ParameterNames.SCOPE, "openid profile")
                                .param(OAuth2ParameterNames.REDIRECT_URI, "http://www.domain.com")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":200}"))
                .andExpect(jsonPath("$.data.accessToken.tokenValue").value(not(emptyOrNullString())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        RestResult<Map<String, Object>> accessTokenResultJson = Casts.readValue(accessTokenJson, new TypeReference<>() {});
        Map<String, Object> accessTokenMap = Casts.cast(accessTokenResultJson.getData().get("accessToken"));
        String accessTokenValue = accessTokenMap.get("tokenValue").toString();

        mockMvc
                .perform(
                        get(oAuth2Properties.getOidcUserInfoEndpoint())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenValue)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":200}"))
                .andExpect(content().json("{\"data\":{\"type\":\"test\",\"principal\":{\"id\":\"1\",\"username\":\"test\",\"name\":\"1:test\"}}}"));

        mockMvc
                .perform(get("/actuator/auditevents"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"events\":[{\"principal\":\"test:1:test\",\"type\":\"AUTHENTICATION_SUCCESS\",\"data\":{\"details\":{\"remember\":false}}},{\"principal\": \"test:1:test\",\"type\": \"AUTHENTICATION_SUCCESS\",\"data\": {\"details\": {\"requestDetails\": {\"jwt\": {\"subject\": \"test:1:test\",\"audience\": [\"test\"]},\"authorities\": [{\"authority\": \"SCOPE_openid\"}, {\"authority\": \"SCOPE_profile\"}]},\"metadata\": {},\"remember\": false}}}]}"));
        ;
    }
}
