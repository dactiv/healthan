package com.github.dactiv.healthan.spring.security.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.audit.Auditable;
import com.github.dactiv.healthan.security.plugin.Plugin;
import com.github.dactiv.healthan.spring.security.audit.config.ControllerAuditProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import com.github.dactiv.healthan.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 控制器审计方法拦截器
 *
 * @author maurice
 */
public class ControllerAuditHandlerInterceptor implements ApplicationEventPublisherAware, AsyncHandlerInterceptor {

    private final ControllerAuditProperties controllerAuditProperties;

    /**
     * spring 应用的事件推送器
     */
    private ApplicationEventPublisher applicationEventPublisher;

    public ControllerAuditHandlerInterceptor(ControllerAuditProperties controllerAuditProperties) {
        this.controllerAuditProperties = controllerAuditProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!HandlerMethod.class.isAssignableFrom(handler.getClass())) {
            return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
        }

        HandlerMethod handlerMethod = Casts.cast(handler);

        Auditable auditable = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Auditable.class);
        OperationDataTrace operationDataTrace = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), OperationDataTrace.class);
        Plugin plugin = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Plugin.class);

        // 判断是否存在操作数据最终
        String type = StringUtils.EMPTY;
        Object principal = null;
        if (Objects.nonNull(auditable) && auditable.operationDataTrace()) {
            type = auditable.type();
            principal = getPrincipal(auditable.principal(), request);
        } else if (Objects.nonNull(operationDataTrace)) {
            type = operationDataTrace.name();
            principal = getPrincipal(operationDataTrace.principal(), request);
        } else if (Objects.nonNull(plugin) && plugin.operationDataTrace()) {
            type = plugin.name();
            Plugin root = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Plugin.class);
            if (Objects.nonNull(root)) {
                type = root.name() + Casts.UNDERSCORE + type;
            }
        }

        if (Objects.isNull(principal)) {
            principal = getPrincipal(null, request);
        }

        if (StringUtils.isNotEmpty(type)) {
            Map<String, Object> data = getData(request, response, handler);
            type = controllerAuditProperties.getAuditPrefixName() + Casts.UNDERSCORE + type;
            request.setAttribute(SecurityPrincipalOperationDataTraceRepository.OPERATION_DATA_TRACE_ATT_NAME, true);
            AuditEvent auditEvent;
            if (AuthenticationSuccessToken.class.isAssignableFrom(principal.getClass())) {
                AuthenticationSuccessToken authenticationToken = Casts.cast(principal);
                data.put(AuthenticationSuccessToken.DETAILS_KEY, authenticationToken.getDetails());

                auditEvent = new AuditEvent(Instant.now(), authenticationToken.getName(), type, data);
            } else {
                auditEvent = new AuditEvent(Instant.now(), principal.toString(), type, data);
            }
            request.setAttribute(controllerAuditProperties.getAuditEventAttrName(), auditEvent);
        }

        return AsyncHandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        if (!HandlerMethod.class.isAssignableFrom(handler.getClass())) {
            return;
        }

        HandlerMethod handlerMethod = Casts.cast(handler);

        String type;
        Object principal;

        Auditable auditable = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Auditable.class);
        if (Objects.nonNull(auditable)) {
            principal = getPrincipal(auditable.principal(), request);
            type = auditable.type();
        } else {
            Plugin plugin = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Plugin.class);
            // 如果控制器方法带有 plugin 注解并且 audit 为 true 是，记录审计内容
            if (Objects.isNull(plugin) || !plugin.audit()) {
                return ;
            }

            principal = getPrincipal(null, request);
            type = plugin.name();
            Plugin root = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Plugin.class);
            if (root != null) {
                type = root.name() + Casts.UNDERSCORE + type;
            }
        }

        AuditEvent auditEvent = createAuditEvent(principal, type, request, response, handler, ex);

        // 推送审计事件
        applicationEventPublisher.publishEvent(new AuditApplicationEvent(auditEvent));

    }

    private AuditEvent createAuditEvent(Object principal,
                                        String type,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {

        Map<String, Object> data = getData(request, response, handler);

        if (Objects.isNull(ex) && HttpStatus.OK.value() == response.getStatus()) {
            type = type + Casts.UNDERSCORE + controllerAuditProperties.getSuccessSuffixName();
        } else {
            type = type + Casts.UNDERSCORE + controllerAuditProperties.getFailureSuffixName();

            if (Objects.nonNull(ex)) {
                data.put(controllerAuditProperties.getExceptionKeyName(), ex.getMessage());
            }
        }

        if (!StringUtils.startsWith(type, controllerAuditProperties.getAuditPrefixName() + Casts.UNDERSCORE)) {
            type = controllerAuditProperties.getAuditPrefixName() + Casts.UNDERSCORE + type;
        }

        if (AuthenticationSuccessToken.class.isAssignableFrom(principal.getClass())) {
            AuthenticationSuccessToken authenticationToken = Casts.cast(principal);
            data.put(AuthenticationSuccessToken.DETAILS_KEY, authenticationToken.getDetails());
            return new AuditEvent(Instant.now(), authenticationToken.getName(), type, data);
        } else {
            return new AuditEvent(Instant.now(), principal.toString(), type, data);
        }
    }

    private Map<String, Object> getData(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Map<String, Object> data = new LinkedHashMap<>();

        Map<String, Object> header = new LinkedHashMap<>();

        Enumeration<String> parameterNames = request.getHeaderNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            header.put(key, request.getHeader(key));
        }

        data.put(controllerAuditProperties.getHeaderKey(), header);

        Map<String, String[]> parameterMap = request.getParameterMap();

        if (!parameterMap.isEmpty()) {
            data.put(controllerAuditProperties.getParamKey(), parameterMap);
        }

        Object body = SpringMvcUtils.getRequestAttribute(RequestBodyAttributeAdviceAdapter.REQUEST_BODY_ATTRIBUTE_NAME);
        if (Objects.nonNull(body)) {
            data.put(controllerAuditProperties.getBodyKey(), body);
        }

        return data;
    }

    private Object getPrincipal(String key, HttpServletRequest request) {

        SecurityContext securityContext = SecurityContextHolder.getContext();

        if (Objects.isNull(securityContext.getAuthentication()) || !securityContext.getAuthentication().isAuthenticated()) {
            String principal = null;

            if (StringUtils.isNotBlank(key)) {
                principal = request.getParameter(key);

                if (StringUtils.isBlank(principal)) {
                    principal = request.getHeader(key);
                }
            }

            if (StringUtils.isBlank(principal)) {
                principal = request.getRemoteAddr();
            }

            return principal;

        } else {
            return securityContext.getAuthentication();
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

}
