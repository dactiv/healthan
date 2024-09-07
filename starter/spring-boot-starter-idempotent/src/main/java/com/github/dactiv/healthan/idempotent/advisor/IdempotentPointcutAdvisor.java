package com.github.dactiv.healthan.idempotent.advisor;

import com.github.dactiv.healthan.idempotent.annotation.Idempotent;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import java.io.Serial;

/**
 * aop 的幂等性切面处理实现
 *
 * @author maurice
 */
public class IdempotentPointcutAdvisor extends AbstractPointcutAdvisor {

    @Serial
    private static final long serialVersionUID = -2973618152809395856L;

    private final IdempotentInterceptor idempotentInterceptor;

    public IdempotentPointcutAdvisor(IdempotentInterceptor idempotentInterceptor) {
        this.idempotentInterceptor = idempotentInterceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return new AnnotationMatchingPointcut(null, Idempotent.class, true);
    }

    @Override
    public Advice getAdvice() {
        return idempotentInterceptor;
    }
}
