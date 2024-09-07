package com.github.dactiv.healthan.idempotent.advisor.concurrent;

import com.github.dactiv.healthan.idempotent.annotation.Concurrent;
import com.github.dactiv.healthan.idempotent.annotation.ConcurrentElements;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.Pointcuts;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

import java.io.Serial;

/**
 * 并发处理的切面实现
 *
 * @author maurice
 */
public class ConcurrentPointcutAdvisor extends AbstractPointcutAdvisor {

    @Serial
    private static final long serialVersionUID = -2797648387592489604L;

    private final ConcurrentInterceptor concurrentInterceptor;

    public ConcurrentPointcutAdvisor(ConcurrentInterceptor concurrentInterceptor) {
        this.concurrentInterceptor = concurrentInterceptor;
    }

    @Override
    public Pointcut getPointcut() {
        return Pointcuts.union(
                new AnnotationMatchingPointcut(null, Concurrent.class, true),
                new AnnotationMatchingPointcut(null, ConcurrentElements.class, true)
        );
    }

    @Override
    public Advice getAdvice() {
        return concurrentInterceptor;
    }
}
