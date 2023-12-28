package com.github.dactiv.healthan.captcha;

import javax.servlet.http.HttpServletRequest;

/**
 * 验证码解析器
 *
 * @author maurice.chen
 */
public interface CaptchaResolver {

    String POST_ARGS_KEY = "post";

    String GENERATE_ARGS_KEY = "generate";

    /**
     * 创建构造验证码元数据
     *
     * @param request http servlet request
     *
     * @return 构造验证码元数据
     */
    ConstructionCaptchaMeta createConstructionCaptchaMeta(HttpServletRequest request);

    /**
     * 所属类型
     *
     * @return 类型
     */
    String getType();
}
