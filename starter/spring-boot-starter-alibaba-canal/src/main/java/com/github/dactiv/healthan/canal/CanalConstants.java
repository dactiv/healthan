package com.github.dactiv.healthan.canal;

import com.github.dactiv.healthan.commons.Casts;

/**
 * canal 常量内容
 *
 * @author maurice.chen
 */
public interface CanalConstants {

    String API_TOKEN_HEADER_NAME = "X-TOKEN";

    String RESULT_CODE = "code";

    String API_SUCCESS_CODE = "20000";

    String LOGIN_API = "/api/v1/user/login";

    String FIND_NODE_SERVERS_API = "/api/v1/nodeServers";

    String NODE_SERVER_CONFIG_API = "/api/v1/canal/config/{0}/{1}";

    String FIND_ACTIVE_INSTANCES_API = "api/v1/canal/active/instances/{0}";

    String FIND_INSTANCES_API = "api/v1/canal/instances?name={0}&page={1}&size={2}";

    String GET_AND_DELETE_INSTANCE_API = "api/v1/canal/instance/{0}";

    String GET_CLUSTERS_API = "api/v1/canal/clustersAndServers";

    String GET_INSTANCE_LOG = "api/v1/canal/instance/log/{0}/{1}";

    String ROOT = "canal";

    String CANAL_PORT = ROOT + Casts.DOT + "port";

    String CANAL_USERNAME = ROOT + Casts.DOT + "user";

    String CANAL_PASSWORD = ROOT + Casts.DOT + "passwd";

    /**
     * canal tcp 模式链接的 host 地址，该配置主要是应用在本地 docker ip 变来变去的问题
     */
    String CANAL_TCP_HOST = ROOT + Casts.DOT + "tcp.host";

    String CANAL_ADMIN_USERNAME = "username";

    String CANAL_ADMIN_PASSWORD = "password";

}
