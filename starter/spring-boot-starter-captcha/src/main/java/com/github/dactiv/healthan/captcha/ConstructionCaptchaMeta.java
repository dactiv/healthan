package com.github.dactiv.healthan.captcha;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 构造验证码元数据
 *
 * @author maurice.chen
 */
public class ConstructionCaptchaMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = -2135082237130733550L;

    private String type;

    private Map<String, Object> args;

    public ConstructionCaptchaMeta() {
    }

    public ConstructionCaptchaMeta(String type, Map<String, Object> args) {
        this.type = type;
        this.args = args;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }
}
