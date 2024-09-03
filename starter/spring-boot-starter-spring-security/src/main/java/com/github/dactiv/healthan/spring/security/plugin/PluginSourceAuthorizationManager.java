package com.github.dactiv.healthan.spring.security.plugin;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.plugin.Plugin;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * 用户类型表决器实现，用于判断当前 controller 方法里面是否带有 {@link Plugin} 注解的记录是否符合当前用户调用
 *
 * @author maurice
 */
public class PluginSourceAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    /**
     * 默认同意的来源类型值
     */
    public static final List<String> DEFAULT_GRANTED_SOURCES = Arrays.asList("SYSTEM","ALL");

    /**
     * 默认同意的来源类型
     */
    private List<String> grantedSources = DEFAULT_GRANTED_SOURCES;

    public PluginSourceAuthorizationManager() {
    }

    public PluginSourceAuthorizationManager(List<String> grantedSources) {
        this.grantedSources = grantedSources;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> supplier, MethodInvocation object) {

        Plugin plugin = AnnotationUtils.findAnnotation(object.getMethod(), Plugin.class);

        if (plugin == null) {
            return null;
        }

        Authentication authentication = supplier.get();

        if (!authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        if (!AuthenticationSuccessToken.class.isAssignableFrom(authentication.getClass())) {
            return new AuthorizationDecision(false);
        }

        List<String> resourceTypes = Arrays
                .stream(plugin.sources())
                .filter(s -> !grantedSources.contains(s))
                .toList();

        if (CollectionUtils.isEmpty(resourceTypes)) {
            return new AuthorizationDecision(true);
        }

        AuthenticationSuccessToken token = Casts.cast(authentication);

        if (!resourceTypes.contains(token.getPrincipalType())) {
            return new AuthorizationDecision(false);
        } else {
            return new AuthorizationDecision(true);
        }
    }

    /**
     * 获取默认同意的来源类型
     *
     * @return 默认同意的来源类型
     */
    public List<String> getGrantedSources() {
        return grantedSources;
    }

    /**
     * 设置默认同意的来源类型
     *
     * @param grantedSources 默认同意的来源类型
     */
    public void setGrantedSources(List<String> grantedSources) {
        this.grantedSources = grantedSources;
    }
}
