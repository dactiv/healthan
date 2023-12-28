package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.web.result.error.ErrorResultResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * rest 结果集认证入口点实现
 *
 * @author maurice.chen
 */
public class RestResultAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final Logger LOGGER = LoggerFactory.getLogger(RestResultAuthenticationEntryPoint.class);

    public static final String ERROR_INTERNAL_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

    private final List<ErrorResultResolver> resultResolvers;

    public RestResultAuthenticationEntryPoint(List<ErrorResultResolver> resultResolvers) {
        this.resultResolvers = resultResolvers;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {

        RestResult<Object> result;
        Throwable throwable = Casts.cast(request.getAttribute(ERROR_INTERNAL_ATTRIBUTE));

        if (Objects.nonNull(throwable)) {
            Optional<ErrorResultResolver> optional = resultResolvers
                    .stream()
                    .filter(r -> r.isSupport(throwable))
                    .findFirst();

            if (optional.isPresent()) {
                result = optional.get().resolve(throwable);
            } else {
                result = RestResult.ofException(throwable);
            }
            LOGGER.error("认证发生错误", e);
        } else if (InsufficientAuthenticationException.class.isAssignableFrom(e.getClass())) {
            LOGGER.error("认证发生错误", e);
            result = RestResult.of(HttpStatus.UNAUTHORIZED.getReasonPhrase(), HttpStatus.UNAUTHORIZED.value(), String.valueOf(HttpStatus.UNAUTHORIZED.value()));
        } else {
            LOGGER.error("认证发生错误", e);
            result = RestResult.ofException(e);
        }

        response.setStatus(result.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Casts.writeValueAsString(result));
    }
}
