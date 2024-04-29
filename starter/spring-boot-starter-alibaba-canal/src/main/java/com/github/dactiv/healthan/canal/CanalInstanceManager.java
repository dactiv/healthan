package com.github.dactiv.healthan.canal;

import com.github.dactiv.healthan.canal.config.CanalInstanceProperties;
import com.github.dactiv.healthan.canal.config.CanalProperties;
import com.github.dactiv.healthan.canal.domain.CanalMessage;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * canal 实例管理
 *
 * @author maurice.chen
 */
public class CanalInstanceManager implements InitializingBean {

    public static final Logger LOGGER = LoggerFactory.getLogger(CanalInstanceManager.class);

    private static final List<CanalSubscribeRunner> RUNNERS = new LinkedList<>();

    private List<CanalRowDataChangeResolver> canalRowDataChangeResolvers;

    private ThreadPoolExecutor buildExecutor;

    private CanalProperties canalProperties;

    public CanalInstanceManager() {

    }

    public CanalInstanceManager(List<CanalRowDataChangeResolver> canalRowDataChangeResolvers,
                                CanalProperties canalProperties) {
        this.canalRowDataChangeResolvers = canalRowDataChangeResolvers;

        int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;

        this.buildExecutor = new ThreadPoolExecutor(
                corePoolSize,
                corePoolSize * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.canalProperties = canalProperties;
    }

    public CanalInstanceManager(List<CanalRowDataChangeResolver> canalRowDataChangeResolvers,
                                ThreadPoolExecutor buildExecutor,
                                CanalProperties canalProperties) {
        this.canalRowDataChangeResolvers = canalRowDataChangeResolvers;
        this.buildExecutor = buildExecutor;
        this.canalProperties = canalProperties;
    }

    public List<CanalRowDataChangeResolver> getCanalRowDataChangeResolvers() {
        return canalRowDataChangeResolvers;
    }

    public void setCanalRowDataChangeResolvers(List<CanalRowDataChangeResolver> canalRowDataChangeResolvers) {
        this.canalRowDataChangeResolvers = canalRowDataChangeResolvers;
    }

    public ThreadPoolExecutor getBuildExecutor() {
        return buildExecutor;
    }

    public void setBuildExecutor(ThreadPoolExecutor buildExecutor) {
        this.buildExecutor = buildExecutor;
    }

    public CanalProperties getCanalProperties() {
        return canalProperties;
    }

    public void setCanalProperties(CanalProperties canalProperties) {
        this.canalProperties = canalProperties;
    }

    /**
     * 订阅实例
     *
     * @param instanceProperties 实例配置
     */
    public void subscribe(CanalInstanceProperties instanceProperties) {

        if (RUNNERS.stream().anyMatch(s -> s.getInstance().getId().equals(instanceProperties.getId()))) {
            return;
        }

        try {
            CanalSubscribeRunner runner = new CanalSubscribeRunner(
                    instanceProperties,
                    canalProperties,
                    buildExecutor,
                    this::canalMessageConsumer
            );

            Future<?> future = buildExecutor.submit(runner);
            runner.setFuture(future);
            RUNNERS.add(runner);

        } catch (Exception e) {
            LOGGER.error("构造 canal 订阅运行线程失败", e);
        }
    }

    private void canalMessageConsumer(CanalMessage canalMessage) {

        for (CanalRowDataChangeResolver resolver : this.canalRowDataChangeResolvers) {
            try {
                resolver.change(canalMessage);
            } catch (Exception e) {
                LOGGER.error("执行 [{}] 解析器解析 canal 消息出错", resolver.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 取消订阅实例
     *
     * @param id 实例 id
     */
    public void unsubscribe(Long id) {

        Assert.notNull(id, "参数 id 不能为空");
        CanalSubscribeRunner canalSubscribeRunner = getInstance(id);

        if (Objects.isNull(canalSubscribeRunner)) {
            return ;
        }
        canalSubscribeRunner.stop();

        RUNNERS.removeIf(r -> canalSubscribeRunner.getInstance().getId().equals(r.getInstance().getId()));
    }

    /**
     * 获取所有运行的实例
     *
     * @return canal 订阅消息运行者集合
     */
    public List<CanalSubscribeRunner> getAllInstance() {
        return RUNNERS;
    }

    /**
     * 获取实例
     *
     * @param id 示例 id
     *
     * @return canal 订阅消息运行者
     */
    public CanalSubscribeRunner getInstance(Long id) {
        return getAllInstance().stream().filter(i -> i.getInstance().getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(canalProperties.getInstances())) {
            return ;
        }

        canalProperties.getInstances().forEach(this::subscribe);
    }
}
