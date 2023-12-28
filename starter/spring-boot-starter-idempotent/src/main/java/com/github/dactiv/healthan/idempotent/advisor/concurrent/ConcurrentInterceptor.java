package com.github.dactiv.healthan.idempotent.advisor.concurrent;

import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.commons.exception.SystemException;
import com.github.dactiv.healthan.idempotent.ConcurrentConfig;
import com.github.dactiv.healthan.idempotent.LockType;
import com.github.dactiv.healthan.idempotent.annotation.Concurrent;
import com.github.dactiv.healthan.idempotent.annotation.ConcurrentElements;
import com.github.dactiv.healthan.idempotent.exception.ConcurrentException;
import com.github.dactiv.healthan.idempotent.generator.ValueGenerator;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 并发拦截器
 *
 * @author maurice
 */
public class ConcurrentInterceptor implements MethodInterceptor {

    public static final String DEFAULT_EXCEPTION = "请不要重复操作";

    /**
     * redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * key 生成器
     */
    private final ValueGenerator valueGenerator;

    public ConcurrentInterceptor(RedissonClient redissonClient, ValueGenerator valueGenerator) {
        this.redissonClient = redissonClient;
        this.valueGenerator = valueGenerator;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        List<Concurrent> concurrentList = new LinkedList<>();
        ConcurrentElements concurrentElements = AnnotationUtils.findAnnotation(invocation.getMethod(), ConcurrentElements.class);
        if (Objects.nonNull(concurrentElements)) {
            concurrentList.addAll(Arrays.asList(concurrentElements.value()));
        } else {
            Concurrent concurrent = AnnotationUtils.findAnnotation(invocation.getMethod(), Concurrent.class);
            concurrentList.add(concurrent);
        }

        if (CollectionUtils.isEmpty(concurrentList)) {
            return invocation.proceed();
        }

        List<ConcurrentConfig> concurrentConfigs = concurrentList
                .stream()
                .filter(c -> StringUtils.isEmpty(c.condition()) || valueGenerator.assertCondition(c.condition(), invocation.getMethod(), invocation.getArguments()))
                .map(ConcurrentConfig::ofConcurrent)
                .collect(Collectors.toList());

        return invoke(concurrentConfigs, () -> this.invocationProceed(invocation)) ;
    }

    private <R> R invoke(List<ConcurrentConfig> concurrentConfigs, Supplier<R> supplier) {
        List<RLock> locks = new LinkedList<>();
        try {
            for (ConcurrentConfig config : concurrentConfigs) {
                RLock lock = getLock(config.getKey(), config.getLockType());

                boolean tryLock = tryLock(lock, config.getWaitTime(), config.getLeaseTime());

                if (!tryLock) {
                    throw new ConcurrentException(config.getException());
                }
                locks.add(lock);
            }
            return supplier.get();
        } finally {
            locks.stream().filter(RLock::isLocked).forEach(Lock::unlock);
        }
    }

    /**
     * 执行 aop 内容
     *
     * @param invocation 方法执行器
     *
     * @return 执行后的返回内容
     */
    private Object invocationProceed(MethodInvocation invocation)  {
        try {
            return invocation.proceed();
        } catch (Throwable e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param concurrent 并发注解
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     *
     */
    private <R> R invoke(String key,
                         Concurrent concurrent,
                         Supplier<R> supplier) {
        TimeProperties waitTime = TimeProperties.of(concurrent.waitTime());
        TimeProperties leaseTime = TimeProperties.of(concurrent.leaseTime());

        return invoke(key, concurrent.type(), waitTime, leaseTime, concurrent.exception(), supplier);
    }

    /**
     * 执行并发处理过程
     *
     * @param properties 并发配置
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     * @return 返回值
     */
    public <R> R invoke(ConcurrentConfig properties,
                        Supplier<R> supplier) {

        return invoke(
                properties.getKey(),
                properties.getLockType(),
                properties.getWaitTime(),
                properties.getLeaseTime(),
                properties.getException(),
                supplier
        );
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     */
    public <R> R invoke(String key,
                        Supplier<R> supplier) {
        return invoke(key, LockType.Lock, supplier);
    }

    /***
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     */
    public <R> R invoke(String key,
                        LockType type,
                        Supplier<R> supplier) {
        return invoke(key, type, null, supplier);
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     *
     */
    public <R> R invoke(String key,
                        LockType type,
                        TimeProperties waitTime,
                        Supplier<R> supplier) {
        return invoke(key, type, waitTime, null, supplier);
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param leaseTime 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 返回值
     */
    public <R> R invoke(String key,
                        LockType type,
                        TimeProperties waitTime,
                        TimeProperties leaseTime,
                        Supplier<R> supplier) {
        return invoke(key, type, waitTime, leaseTime, DEFAULT_EXCEPTION, supplier);
    }

    /**
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param leaseTime 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     * @param exception 异常信息
     * @param supplier 执行过程供应者
     * @param <R> 返回值类型
     *
     * @return 执行过程供应者返回值
     */
    public <R> R invoke(String key,
                        LockType type,
                        TimeProperties waitTime,
                        TimeProperties leaseTime,
                        String exception,
                        Supplier<R> supplier) {

        RLock lock = getLock(key, type);

        boolean tryLock = tryLock(lock, waitTime, leaseTime);

        if (!tryLock) {
            throw new ConcurrentException(exception);
        }

        try {
            return supplier.get();
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param concurrent 并发注解
     * @param runnable 执行过程供应者
     *
     */
    private void invoke(String key,
                        Concurrent concurrent,
                        Runnable runnable) {
        TimeProperties waitTime = TimeProperties.of(concurrent.waitTime());
        TimeProperties leaseTime = TimeProperties.of(concurrent.leaseTime());

        invoke(key, concurrent.type(), waitTime, leaseTime, concurrent.exception(), runnable);
    }

    /**
     * 执行并发处理过程
     *
     * @param properties 并发配置
     * @param runnable 执行过程供应者
     */
    public void invoke(ConcurrentConfig properties,
                       Runnable runnable) {

        invoke(
                properties.getKey(),
                properties.getLockType(),
                properties.getWaitTime(),
                properties.getLeaseTime(),
                properties.getException(),
                runnable
        );
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param runnable 执行过程供应者
     *
     */
    public void invoke(String key,
                       Runnable runnable) {
        invoke(key, LockType.Lock, runnable);
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param runnable 执行过程供应者
     *
     */
    public void invoke(String key,
                       LockType type,
                       Runnable runnable) {
        invoke(key, type, null, runnable);
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param runnable 执行过程供应者
     *
     */
    public void invoke(String key,
                       LockType type,
                       TimeProperties waitTime,
                       Runnable runnable) {
        invoke(key, type, waitTime, null, runnable);
    }

    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param leaseTime 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     * @param runnable 执行过程供应者
     *
     */
    public void invoke(String key,
                       LockType type,
                       TimeProperties waitTime,
                       TimeProperties leaseTime,
                       Runnable runnable) {
        invoke(key, type, waitTime, leaseTime, DEFAULT_EXCEPTION, runnable);
    }
    /**
     * 执行并发处理过程
     *
     * @param key 键值
     * @param type 锁类型
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param leaseTime 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     * @param exception 异常信息
     * @param runnable 执行过程供应者
     *
     */
    public void invoke(String key,
                       LockType type,
                       TimeProperties waitTime,
                       TimeProperties leaseTime,
                       String exception,
                       Runnable runnable) {

        RLock lock = getLock(key, type);

        boolean tryLock = tryLock(lock, waitTime, leaseTime);

        if (!tryLock) {
            throw new ConcurrentException(exception);
        }

        try {
            runnable.run();
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    /**
     * 获取锁
     *
     * @param key 建值
     * @param type 锁类型
     *
     * @return redis 锁实现
     */
    private RLock getLock(String key, LockType type) {
        if (LockType.FairLock.equals(type)) {
            return redissonClient.getFairLock(key);
        } else if (LockType.Lock.equals(type)) {
            return redissonClient.getLock(key);
        }

        throw new SystemException("找不到对 [" + type + "] 的所类型支持");
    }

    /**
     * 尝试加锁
     *
     * @param lock 锁
     * @param waitTime 等待锁时间（获取锁时如果在该时间内获取不到，抛出异常）
     * @param leaseTime 释放锁时间 (当获取到锁时候，在该时间不管执行过程供应者执行完成或不完成，都将当前锁释放)
     *
     * @return true 加锁成功，否则 false
     */
    private boolean tryLock(RLock lock, TimeProperties waitTime, TimeProperties leaseTime) {

        try {
            if (Objects.nonNull(waitTime) && Objects.nonNull(leaseTime)) {
                long waitTimeValue = waitTime.getUnit().toMillis(waitTime.getValue());
                return lock.tryLock(waitTimeValue, leaseTime.getValue(), leaseTime.getUnit());
            } else if (Objects.nonNull(waitTime)) {
                return lock.tryLock(waitTime.getValue(), waitTime.getUnit());
            }
        } catch (Exception e) {
            throw new SystemException("并发获取锁时出现异常", e);
        }

        return lock.tryLock();
    }

}
