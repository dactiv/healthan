package com.github.dactiv.healthan.spring.web.endpoint;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.enumerate.NameEnum;
import com.github.dactiv.healthan.commons.enumerate.NameEnumUtils;
import com.github.dactiv.healthan.commons.enumerate.NameValueEnum;
import com.github.dactiv.healthan.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.healthan.commons.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 获取枚举键值对终端
 *
 * @author maurice
 */
//FIXME 添加 enumerate 配置，不要通过 ConfigurationProperties 配置信息
@Endpoint(id = "enumerate")
public class EnumerateEndpoint {

    private final static Logger LOGGER = LoggerFactory.getLogger(EnumerateEndpoint.class);

    /**
     * 默认的枚举字段名称
     */
    public final static String DEFAULT_ENUM_KEY_NAME = "enum";

    /**
     * 信息奉献者集合
     */
    private final List<InfoContributor> infoContributors = new ArrayList<>();

    /**
     * 需要扫描的包路径
     */
    private List<String> basePackages = new ArrayList<>(16);

    /**
     * spring 资源解析器
     */
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    /**
     * spring 元数据读取工厂
     */
    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    /**
     * 缓存值
     */
    private final Map<String, Object> cache = new LinkedHashMap<>();

    /**
     * 并发锁
     */
    private final Lock lock = new ReentrantLock();

    /**
     * 创建一个获取枚举键值对终端
     *
     * @param infoContributors 信息贡献者实现
     */
    public EnumerateEndpoint(List<InfoContributor> infoContributors) {
        this.infoContributors.addAll(infoContributors);
    }

    @ReadOperation
    public Map<String, Object> enumerate() {
        // 如果缓存没有，就去扫描遍历
        lock.lock();

        try {

            if (cache.isEmpty()) {

                Info.Builder builder = new Info.Builder();

                for (InfoContributor contributor : this.infoContributors) {
                    contributor.contribute(builder);
                }

                Info build = builder.build();

                Map<String, Object> info = new LinkedHashMap<>();

                Map<String, Object> details = build.getDetails();

                if (MapUtils.isNotEmpty(details)) {
                    info.putAll(details);
                }

                Map<String, Map<String, Object>> enumMap = resolveEnumerate();

                info.put(DEFAULT_ENUM_KEY_NAME, enumMap);

                cache.putAll(info);
            }

            return cache;
        } finally {
            lock.unlock();
        }

    }

    private Map<String, Map<String, Object>> resolveEnumerate() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("开始解析 info.enum 信息");
        }

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        // 扫描所有集成 NameEnum 的类
        Set<Class<? extends Enum<? extends NameEnum>>> classes = resolvePlaceholders();
        // 如果找不到，什么都不做
        if (CollectionUtils.isEmpty(classes)) {
            return result;
        }

        for (Class<? extends Enum<? extends NameEnum>> c : classes) {

            Map<String, Object> map;

            if (NameValueEnum.class.isAssignableFrom(c)) {
                map = ValueEnumUtils.castMap(Casts.cast(c));
            } else {
                map = NameEnumUtils.castMap(c);
            }

            if (MapUtils.isNotEmpty(map)) {
                result.put(c.getSimpleName(), map);
            }
        }

        return result;
    }

    private Set<Class<? extends Enum<? extends NameEnum>>> resolvePlaceholders() {
        Set<Class<? extends Enum<? extends NameEnum>>> classes = new HashSet<>();

        for (String basePackage : basePackages) {
            String classPath = ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) + "/**/*.class";
            TypeFilter filter = new AssignableTypeFilter(NameEnum.class);

            try {
                Resource[] resources = this.resourcePatternResolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + classPath);

                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                        if (filter.match(metadataReader, metadataReaderFactory)) {
                            classes.add(Casts.cast(Class.forName(metadataReader.getClassMetadata().getClassName())));
                        }
                    }
                }
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }

        return classes;
    }

    /**
     * 设置要扫描的包路径
     *
     * @param basePackages 包路径
     */
    public void setBasePackages(List<String> basePackages) {
        this.basePackages = basePackages;
    }

    /**
     * 获取要扫描的包路径
     *
     * @return 要扫描的包路径集合
     */
    public List<String> getBasePackages() {
        return basePackages;
    }

}
