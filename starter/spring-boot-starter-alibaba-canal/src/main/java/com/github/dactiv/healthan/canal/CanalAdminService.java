package com.github.dactiv.healthan.canal;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.healthan.canal.config.CanalAdminProperties;
import com.github.dactiv.healthan.canal.config.CanalInstanceProperties;
import com.github.dactiv.healthan.canal.domain.CanalInstance;
import com.github.dactiv.healthan.canal.domain.CanalNodeServer;
import com.github.dactiv.healthan.canal.domain.CanalNodeServerConfig;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.commons.enumerate.support.YesOrNo;
import com.github.dactiv.healthan.commons.exception.ErrorCodeException;
import com.github.dactiv.healthan.commons.exception.SystemException;
import com.github.dactiv.healthan.commons.page.PageRequest;
import com.github.dactiv.healthan.commons.page.TotalPage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.CookieGenerator;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * canal admin api 服务
 *
 * @author maurice.chen
 */
public class CanalAdminService {

    public static final Logger LOGGER = LoggerFactory.getLogger(CanalAdminService.class);

    public static final String PAGE_ITEMS_FIELD = "items";

    private CanalAdminProperties canalAdminProperties;

    private RestTemplate restTemplate;

    private RedissonClient redissonClient;

    private CanalInstanceManager canalInstanceManager;

    public CanalAdminService() {
    }

    public CanalAdminService(CanalAdminProperties canalAdminProperties,
                             RestTemplate restTemplate,
                             RedissonClient redissonClient,
                             CanalInstanceManager canalInstanceManager) {
        this.canalAdminProperties = canalAdminProperties;
        this.restTemplate = restTemplate;
        this.redissonClient = redissonClient;
        this.canalInstanceManager = canalInstanceManager;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CanalAdminProperties getCanalAdminProperties() {
        return canalAdminProperties;
    }

    public void setCanalAdminProperties(CanalAdminProperties canalAdminProperties) {
        this.canalAdminProperties = canalAdminProperties;
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public CanalInstanceManager getCanalInstanceManager() {
        return canalInstanceManager;
    }

    public void setCanalInstanceManager(CanalInstanceManager canalInstanceManager) {
        this.canalInstanceManager = canalInstanceManager;
    }

    /**
     * 获取 api 实际值
     *
     * @param api api 值
     * @return 域名 + 接口名称值
     */
    public String getApiValue(String api) {
        String apiValue = StringUtils.prependIfMissing(api, CookieGenerator.DEFAULT_COOKIE_PATH);
        String url = StringUtils.removeEnd(canalAdminProperties.getUri(), CookieGenerator.DEFAULT_COOKIE_PATH);

        return url + apiValue;
    }

    /**
     * 查询 canal 节点服务
     *
     * @param param 查询参数
     * @return canal 节点服务集合
     */
    public List<CanalNodeServer> findNodeServer(MultiValueMap<String, String> param) {
        HttpHeaders headers = createCanalApiHttpHeaders();

        List<CanalNodeServer> result = new LinkedList<>();

        try {
            param.add(PageRequest.PAGE_FIELD_NAME, BigDecimal.ZERO.toPlainString());
            List<CanalNodeServer> data;

            do {
                int page = NumberUtils.toInt(param.getFirst(PageRequest.PAGE_FIELD_NAME));
                page++;

                param.put(PageRequest.PAGE_FIELD_NAME, Collections.singletonList(String.valueOf(page)));
                HttpEntity<?> entity = new HttpEntity<>(headers);

                String url = getApiValue(CanalConstants.FIND_NODE_SERVERS_API) + Casts.QUESTION_MARK + Casts.castRequestBodyMapToString(param);
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, Casts.MAP_PARAMETERIZED_TYPE_REFERENCE);
                Map<String, Object> pageResult = postResponseEntity(response, url, Casts.MAP_TYPE_REFERENCE);
                data = Casts.convertValue(pageResult.get(PAGE_ITEMS_FIELD), new TypeReference<List<CanalNodeServer>>() {});
                if (CollectionUtils.isNotEmpty(data)) {
                    result.addAll(data);
                }
            } while (CollectionUtils.isNotEmpty(data));
        } catch (Exception e) {
            LOGGER.error("执行 findNodeServer 出现错误", e);
        }

        return result;
    }

    /**
     * 获取节点服务配置信息
     *
     * @param clusterId 集群 id 默认为 0
     * @param id        节点服务 id
     * @return 配置信息
     */
    public CanalNodeServerConfig getNodeServerConfig(Long clusterId, Long id) {
        HttpHeaders headers = createCanalApiHttpHeaders();
        clusterId = Objects.isNull(clusterId) ? 0 : clusterId;

        String url = getApiValue(MessageFormat.format(CanalConstants.NODE_SERVER_CONFIG_API, clusterId, id));
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, Casts.MAP_PARAMETERIZED_TYPE_REFERENCE);
        return postResponseEntity(response, url, new TypeReference<CanalNodeServerConfig>() {});

    }

    /**
     * 处理响应实体，并返回结果
     *
     * @param response  响应实体
     * @param url       api url
     * @param dataClass 响应值类型
     * @param <T>       响应值类型
     * @return 响应值
     */
    private <T> T postResponseEntity(ResponseEntity<Map<String, Object>> response, String url, TypeReference<T> dataClass) {

        Map<String, Object> body = assertResponseEntityAndGetBody(response, url);
        return Casts.convertValue(body.get(RestResult.DEFAULT_DATA_NAME), dataClass);
    }

    /**
     * 断言 api 执行响应实体并获取 api 里的 data 内容
     *
     * @param response api 执行响应实
     * @param url      api 接口地址
     * @return api data 内容
     */
    private Map<String, Object> assertResponseEntityAndGetBody(ResponseEntity<Map<String, Object>> response, String url) {
        if (HttpStatus.OK != response.getStatusCode()) {
            throw new SystemException("canal 执行 " + url + " 接口响应状态码为: " + response.getStatusCode());
        }

        Map<String, Object> body = Objects.requireNonNull(response.getBody(), "canal 执行 " + url + " 响应的 body 为空");
        String codeValue = body.get(CanalConstants.RESULT_CODE).toString();
        if (!StringUtils.equals(codeValue, CanalConstants.API_SUCCESS_CODE)) {
            throw new ErrorCodeException(body.get(RestResult.DEFAULT_MESSAGE_NAME).toString(), codeValue);
        }

        if (!body.containsKey(RestResult.DEFAULT_DATA_NAME)) {
            throw new SystemException("canal 执行 " + url + " 的 json 结果中，没有 " + RestResult.DEFAULT_DATA_NAME + " 内容");
        }

        return body;
    }

    /**
     * 获取已启动的 canal 实例
     *
     * @param serverId 服务 id
     * @return canal 实例集合
     */
    public List<CanalInstance> findActiveInstances(Long serverId) {
        HttpHeaders headers = createCanalApiHttpHeaders();
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);


        String url = getApiValue(MessageFormat.format(CanalConstants.FIND_ACTIVE_INSTANCES_API, serverId));
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, Casts.MAP_PARAMETERIZED_TYPE_REFERENCE);
        List<CanalInstance> result = postResponseEntity(response, url, new TypeReference<List<CanalInstance>>() {});
        if (CollectionUtils.isEmpty(result)) {
            return new LinkedList<>();
        }

        return result
                .stream()
                .peek(this::setInstanceSubscribeStatus)
                .collect(Collectors.toList());
    }

    /**
     * 订阅服务的实例
     *
     * @param instance canal 实例
     */
    public void subscribe(CanalInstance instance, CanalNodeServer canalNodeServer) {

        if (YesOrNo.No.equals(instance.getRunningStatus()) || YesOrNo.Yes.equals(instance.getSubscribeStatus())) {
            return;
        }

        CanalNodeServerConfig canalNodeServerConfig;
        if (canalNodeServer.getClass().isAssignableFrom(CanalNodeServerConfig.class)) {
            canalNodeServerConfig = Casts.cast(canalNodeServer);
        } else {
            canalNodeServerConfig = getNodeServerConfig(
                    canalNodeServer.getClusterId(),
                    canalNodeServer.getId()
            );
        }

        CanalInstanceProperties instanceProperties = new CanalInstanceProperties();

        instanceProperties.setHost(canalNodeServer.getIp());
        instanceProperties.setTcpPort(canalNodeServer.getTcpPort());
        instanceProperties.setId(instance.getId());
        instanceProperties.setName(instance.getName());

        String host = canalNodeServerConfig.properties().getProperty(CanalConstants.CANAL_TCP_HOST);
        if (StringUtils.isEmpty(host) && MapUtils.isNotEmpty(instance.properties())) {
            host = instance.properties().getProperty(CanalConstants.CANAL_TCP_HOST);
        }

        if (StringUtils.isEmpty(host)) {
            instanceProperties.setHost(host);
        }

        instanceProperties.setUsername(canalNodeServerConfig.properties().getOrDefault(CanalConstants.CANAL_USERNAME, StringUtils.EMPTY).toString());
        instanceProperties.setPassword(canalNodeServerConfig.properties().getOrDefault(CanalConstants.CANAL_PASSWORD, StringUtils.EMPTY).toString());

        canalInstanceManager.subscribe(instanceProperties);
    }


    /**
     * 停止订阅 canal 实例
     *
     * @param id canal 实例 id
     */
    public void unsubscribe(Long id) {
        CanalInstance instance = getInstance(id);

        if (Objects.isNull(instance)) {
            LOGGER.warn("stopSubscribeInstance 找不到 ID 为 [{}] 的 canal 实例", id);
            return;
        }

        if (YesOrNo.No.equals(instance.getSubscribeStatus())) {
            return;
        }
        canalInstanceManager.unsubscribe(id);
    }

    /**
     * 获取 canal 实例
     *
     * @param id 实例 id
     * @return 实例 id
     */
    public CanalInstance getInstance(Long id) {

        HttpHeaders headers = createCanalApiHttpHeaders();
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

        String url = getApiValue(MessageFormat.format(CanalConstants.GET_AND_DELETE_INSTANCE_API, id));

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, Casts.MAP_PARAMETERIZED_TYPE_REFERENCE);
        CanalInstance result = postResponseEntity(response, url, new TypeReference<CanalInstance>() {});

        setInstanceSubscribeStatus(result);

        return result;
    }

    /**
     * 设置实例是否被订阅状态
     *
     * @param instance canal 实例 dto
     */
    private void setInstanceSubscribeStatus(CanalInstance instance) {
        if (Objects.isNull(instance)) {
            return;
        }
        CanalSubscribeRunner canalSubscribeRunner = canalInstanceManager.getInstance(instance.getId());
        instance.setSubscribeStatus(Objects.nonNull(canalSubscribeRunner) ? YesOrNo.Yes : YesOrNo.No);
    }

    /**
     * 查找 canal 实例分页
     *
     * @param name        canal 实例名称
     * @param pageRequest 分页请求
     * @return 带总记录数的分页实体
     */
    public TotalPage<CanalInstance> findInstances(String name, PageRequest pageRequest) {
        HttpHeaders headers = createCanalApiHttpHeaders();
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

        String url = getApiValue(CanalConstants.FIND_INSTANCES_API);
        url = MessageFormat.format(url, name, pageRequest.getNumber(), pageRequest.getSize());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Casts.MAP_PARAMETERIZED_TYPE_REFERENCE
        );
        Map<String, Object> result = postResponseEntity(response, url, Casts.MAP_TYPE_REFERENCE);

        Long totalCount = Casts.cast(result.get(TotalPage.COUNT_FIELD), Long.class);
        List<CanalInstance> elements = Casts.convertValue(
                result.get(PAGE_ITEMS_FIELD),
                new TypeReference<List<CanalInstance>>() {}
        );
        elements.forEach(this::setInstanceSubscribeStatus);
        return new TotalPage<>(pageRequest, elements, totalCount);
    }

    /**
     * 创建 canal 执行 api 的头信息
     *
     * @return canal 执行 api 的头信息
     */
    private HttpHeaders createCanalApiHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CanalConstants.API_TOKEN_HEADER_NAME, getCanalAdminLoginToken());
        return headers;
    }

    public String getCanalAdminLoginToken() {

        RBucket<String> bucket = redissonClient.getBucket(canalAdminProperties.getLoginTokenCache().getName());

        if (bucket.isExists()) {
            return bucket.get();
        }

        Map<String, Object> loginParam = new LinkedHashMap<>();

        loginParam.put(CanalConstants.CANAL_ADMIN_USERNAME, canalAdminProperties.getUsername());
        loginParam.put(CanalConstants.CANAL_ADMIN_PASSWORD, canalAdminProperties.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(loginParam, headers);

        String url = getApiValue(CanalConstants.LOGIN_API);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, Casts.MAP_PARAMETERIZED_TYPE_REFERENCE);

            Map<String, Object> data = postResponseEntity(response, url, Casts.MAP_TYPE_REFERENCE);
            String loginToken = data.getOrDefault(canalAdminProperties.getTokenParamName(), StringUtils.EMPTY).toString();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("获取 canal admin 登陆 token 成功，值为:{}", loginToken);
            }

            bucket.set(loginToken);
            TimeProperties expiresTime = canalAdminProperties.getLoginTokenCache().getExpiresTime();
            if (Objects.nonNull(expiresTime)) {
                bucket.expireAsync(expiresTime.toDuration());
            }

            return loginToken;
        } catch (Exception e) {
            LOGGER.error("获取 canal admin 登陆 token 失败", e);
            return null;
        }
    }

    /**
     * 获取集群信息
     *
     * @return 集群信息
     */
    public List<Map<String, Object>> getClusters() {
        String url = getApiValue(CanalConstants.GET_CLUSTERS_API);

        HttpHeaders headers = createCanalApiHttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, Casts.MAP_PARAMETERIZED_TYPE_REFERENCE);
        return postResponseEntity(response, url, new TypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * 获取实例日志
     *
     * @param instanceId 实例 id
     * @param serverId   服务节点 id
     * @return 日志信息
     */
    public Map<String, Object> getInstanceLog(Long instanceId, Long serverId) {
        HttpHeaders headers = createCanalApiHttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        String url = getApiValue(MessageFormat.format(CanalConstants.GET_INSTANCE_LOG, instanceId, serverId));
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, Casts.MAP_PARAMETERIZED_TYPE_REFERENCE);
        return postResponseEntity(response, url, new TypeReference<Map<String, Object>>() {});
    }
}
