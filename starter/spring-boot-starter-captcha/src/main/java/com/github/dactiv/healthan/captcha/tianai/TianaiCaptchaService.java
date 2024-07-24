package com.github.dactiv.healthan.captcha.tianai;

import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.common.util.CollectionUtils;
import cloud.tianai.captcha.common.util.ObjectUtils;
import cloud.tianai.captcha.generator.ImageCaptchaGenerator;
import cloud.tianai.captcha.generator.ImageTransform;
import cloud.tianai.captcha.generator.common.constant.SliderCaptchaConstant;
import cloud.tianai.captcha.generator.common.model.dto.ImageCaptchaInfo;
import cloud.tianai.captcha.generator.impl.MultiImageCaptchaGenerator;
import cloud.tianai.captcha.generator.impl.transform.Base64ImageTransform;
import cloud.tianai.captcha.resource.ImageCaptchaResourceManager;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.common.model.dto.ResourceMap;
import cloud.tianai.captcha.resource.impl.DefaultImageCaptchaResourceManager;
import cloud.tianai.captcha.validator.ImageCaptchaValidator;
import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import cloud.tianai.captcha.validator.impl.BasicCaptchaTrackValidator;
import com.github.dactiv.healthan.captcha.AbstractCaptchaService;
import com.github.dactiv.healthan.captcha.CaptchaProperties;
import com.github.dactiv.healthan.captcha.GenerateCaptchaResult;
import com.github.dactiv.healthan.captcha.SimpleCaptcha;
import com.github.dactiv.healthan.captcha.controller.CaptchaController;
import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.captcha.storage.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.tianai.body.TianaiRequestBody;
import com.github.dactiv.healthan.captcha.tianai.config.ResourceProperties;
import com.github.dactiv.healthan.captcha.tianai.config.TemplateProperties;
import com.github.dactiv.healthan.captcha.tianai.config.TianaiCaptchaProperties;
import com.github.dactiv.healthan.captcha.token.InterceptToken;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.commons.exception.ErrorCodeException;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.RandomValuePropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.validation.Validator;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * tianai 验证码服务实现
 *
 * @author maurice.chen
 */
public class TianaiCaptchaService extends AbstractCaptchaService<TianaiRequestBody> implements ResourceStore {

    public static final Logger LOGGER = LoggerFactory.getLogger(TianaiCaptchaService.class);

    public static final String DEFAULT_TYPE = "tianai";

    private static final String TYPE_TAG_SPLIT_FLAG = "|";

    private final ImageCaptchaGenerator imageCaptchaGenerator;


    private final TianaiCaptchaProperties tianaiCaptchaProperties;

    // 负责计算一些数据存到缓存中，用于校验使用
    // ImageCaptchaValidator负责校验用户滑动滑块是否正确和生成滑块的一些校验数据; 比如滑块到凹槽的百分比值
    private final ImageCaptchaValidator imageCaptchaValidator = new BasicCaptchaTrackValidator();
    private final Map<String, List<ResourceMap>> templateResourceMap = new HashMap<>(2);
    private final Map<String, List<Resource>> resourceMap = new HashMap<>(2);
    private final Map<String, List<ResourceMap>> templateResourceTagMap = new HashMap<>(2);
    private final Map<String, List<Resource>> resourceTagMap = new HashMap<>(2);

    public TianaiCaptchaService(CaptchaProperties captchaProperties,
                                Validator validator,
                                Interceptor interceptor,
                                CaptchaStorageManager captchaStorageManager,
                                TianaiCaptchaProperties tianaiCaptchaProperties) {

        setCaptchaProperties(captchaProperties);
        setInterceptor(interceptor);
        setValidator(validator);
        setCaptchaStorageManager(captchaStorageManager);

        tianaiCaptchaProperties
                .getTemplateMap()
                .forEach((key, value) -> value.forEach(r -> this.addTemplate(key, r)));

        tianaiCaptchaProperties
                .getResourceMap()
                .forEach((key, value) -> value.forEach(r -> addResource(key, new Resource(r.getType(), r.getData(), r.getTag()))));

        this.tianaiCaptchaProperties = tianaiCaptchaProperties;
        ImageCaptchaResourceManager imageCaptchaResourceManager = new DefaultImageCaptchaResourceManager(this);
        ImageTransform imageTransform = new Base64ImageTransform();

        this.imageCaptchaGenerator = new MultiImageCaptchaGenerator(imageCaptchaResourceManager,imageTransform).init(false);
    }

    public TianaiCaptchaProperties getTianaiCaptchaProperties() {
        return tianaiCaptchaProperties;
    }

    private void addTemplate(String key, TemplateProperties r) {
        ResourceMap resource = new ResourceMap(ResourceProperties.DEFAULT_TAG_NAME,4);
        if (Objects.nonNull(r.getActiveImage())) {
            Resource image = new Resource(r.getActiveImage().getType(), r.getActiveImage().getData(), r.getActiveImage().getTag());
            resource.put(SliderCaptchaConstant.TEMPLATE_ACTIVE_IMAGE_NAME, image);
        }

        if (Objects.nonNull(r.getFixedImage())) {
            Resource image = new Resource(r.getFixedImage().getType(), r.getFixedImage().getData(), r.getFixedImage().getTag());
            resource.put(SliderCaptchaConstant.TEMPLATE_FIXED_IMAGE_NAME, image);
        }

        if (Objects.nonNull(r.getMaskImage())) {
            Resource image = new Resource(r.getMaskImage().getType(), r.getMaskImage().getData(), r.getMaskImage().getTag());
            resource.put(SliderCaptchaConstant.TEMPLATE_MASK_IMAGE_NAME, image);
        }

        this.addTemplate(key, resource);
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {
        Map<String, Object> result = new LinkedHashMap<>();
        String url = tianaiCaptchaProperties.getApiBaseUrl()
                + AntPathMatcher.DEFAULT_PATH_SEPARATOR
                + CaptchaController.CONTROLLER_NAME
                + AntPathMatcher.DEFAULT_PATH_SEPARATOR
                + TianaiCaptchaProperties.JS_CONTROLLER;

        result.put(TianaiCaptchaProperties.JS_URL_KEY, url);

        return result;
    }

    @Override
    protected boolean matchesCaptcha(HttpServletRequest request, SimpleCaptcha captcha) {
        String captchaValue = request.getParameter(getCaptchaParamName());
        String md5 = DigestUtils.md5DigestAsHex(captcha.getValue().getBytes());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[tianal 验证码] 匹配验证码信息:请求值为:{},对比值为:{}", captchaValue, md5);
        }
        if (!StringUtils.equals(md5, captchaValue)) {
            return false;
        }

        ImageCaptchaTrack track = Casts.readValue(captcha.getValue(), ImageCaptchaTrack.class);
        Duration duration = Duration.between(
                LocalDateTime.ofInstant(track.getStartSlidingTime().toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(track.getEndSlidingTime().toInstant(), ZoneId.systemDefault())
        );
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "[tianal 验证码] 调用滑动时间比对，当前滑动时间值为:{} 秒, 服务验证码超时值为:{} 秒",
                    duration.getSeconds(),
                    tianaiCaptchaProperties.getServerVerifyTimeout().toSeconds()
            );
        }
        return duration.getSeconds() < tianaiCaptchaProperties.getServerVerifyTimeout().toSeconds();
    }

    public RestResult<Object> clientVerify(ImageCaptchaTrack imageCaptchaTrack, String token) {
        InterceptToken interceptToken = createInterceptToken(token);
        SimpleCaptcha captcha = getCaptchaStorageManager().getCaptcha(interceptToken);
        getCaptchaStorageManager().deleteCaptcha(interceptToken);

        try {
            Assert.notNull(captcha, "验证内容已过期");

            Map<String, Object> map = Casts.readValue(captcha.getValue(), Casts.MAP_TYPE_REFERENCE);
            ApiResponse<?> response = imageCaptchaValidator.valid(imageCaptchaTrack, map);
            if (response.isSuccess()) {

                String value = Casts.writeValueAsString(imageCaptchaTrack);
                captcha.setValue(value);
                getCaptchaStorageManager().saveCaptcha(captcha, interceptToken);

                long useTime = imageCaptchaTrack.getEndSlidingTime().getTime() - imageCaptchaTrack.getStartSlidingTime().getTime();
                return RestResult.ofSuccess("校验成功, 本次使用 " + useTime / 1000 + " 秒", DigestUtils.md5DigestAsHex(captcha.getValue().getBytes()));
            }
            return RestResult.of(response.getMsg(), HttpStatus.OK.value(), ErrorCodeException.DEFAULT_EXCEPTION_CODE);
        } catch (Exception e) {
            LOGGER.error("校验 tianai 数据错误", e);
            RestResult<Object> result = RestResult.ofException(e);
            result.setStatus(HttpStatus.OK.value());
            return result;
        }
    }

    @Override
    protected GenerateCaptchaResult doGenerateCaptcha(InterceptToken buildToken,
                                                      TianaiRequestBody requestBody,
                                                      HttpServletRequest request) {
        String type = requestBody.getGenerateImageType();
        if (RandomValuePropertySource.RANDOM_PROPERTY_SOURCE_NAME.equals(requestBody.getGenerateImageType())) {
            type = tianaiCaptchaProperties.getRandomCaptchaType().get(RandomUtils.nextInt(0, tianaiCaptchaProperties.getRandomCaptchaType().size()));
        }
        // 生成滑块验证码图片, 可选项
        // SLIDER (滑块验证码)
        // ROTATE (旋转验证码)
        // CONCAT (滑动还原验证码)
        // WORD_IMAGE_CLICK (文字点选验证码)
        //
        // 更多验证码支持 详见 cloud.tianai.captcha.common.constant.CaptchaTypeConstant
        ImageCaptchaInfo imageCaptchaInfo = imageCaptchaGenerator.generateCaptchaImage(type);
        imageCaptchaInfo.setTolerant(tianaiCaptchaProperties.getTolerantMap().getOrDefault(type, 0.00F));
        // 这个map数据应该存到缓存中，校验的时候需要用到该数据
        Map<String, Object> map = imageCaptchaValidator.generateImageCaptchaValidData(imageCaptchaInfo);

        imageCaptchaInfo.setType(CaseUtils.toCamelCase(imageCaptchaInfo.getType(), false, Casts.UNDERSCORE.toCharArray()));
        Map<String, Object> body = Casts.convertValue(imageCaptchaInfo, Casts.MAP_TYPE_REFERENCE);

        return GenerateCaptchaResult.of(body, Casts.writeValueAsString(map));
    }

    @Override
    protected TimeProperties getCaptchaExpireTime() {
        return tianaiCaptchaProperties.getCaptchaExpireTime();
    }

    @Override
    public String getType() {
        return DEFAULT_TYPE;
    }

    @Override
    public String getCaptchaParamName() {
        return tianaiCaptchaProperties.getCaptchaParamName();
    }

    @Override
    public void addResource(String type, Resource resource) {
        this.resourceMap.computeIfAbsent(type, (k) -> new ArrayList<>(20)).add(resource);
        if (!ObjectUtils.isEmpty(resource.getTag())) {
            this.resourceTagMap.computeIfAbsent(this.mergeTypeAndTag(type, resource.getTag()), (k) -> new ArrayList<>(20)).add(resource);
        }

    }

    @Override
    public void addTemplate(String type, ResourceMap template) {
        this.templateResourceMap.computeIfAbsent(type, (k) -> new ArrayList<>(2)).add(template);
        if (!ObjectUtils.isEmpty(template.getTag())) {
            this.templateResourceTagMap.computeIfAbsent(this.mergeTypeAndTag(type, template.getTag()), (k) -> new ArrayList<>(2)).add(template);
        }

    }

    @Override
    public Resource randomGetResourceByTypeAndTag(String type, String tag) {
        List<Resource> resources;
        if (ObjectUtils.isEmpty(tag)) {
            resources = this.resourceMap.get(type);
        } else {
            resources = this.resourceTagMap.get(this.mergeTypeAndTag(type, tag));
        }

        if (CollectionUtils.isEmpty(resources)) {
            throw new IllegalStateException("随机获取资源错误，store中资源为空, type:" + type);
        } else if (resources.size() == 1) {
            return resources.iterator().next();
        } else {
            int randomIndex = ThreadLocalRandom.current().nextInt(resources.size());
            return resources.get(randomIndex);
        }
    }

    @Override
    public ResourceMap randomGetTemplateByTypeAndTag(String type, String tag) {
        List<ResourceMap> templateList;
        if (ObjectUtils.isEmpty(tag)) {
            templateList = this.templateResourceMap.get(type);
        } else {
            templateList = this.templateResourceTagMap.get(this.mergeTypeAndTag(type, tag));
        }

        if (CollectionUtils.isEmpty(templateList)) {
            throw new IllegalStateException("随机获取模板错误，store中模板为空, type:" + type);
        } else if (templateList.size() == 1) {
            return templateList.iterator().next();
        } else {
            int randomIndex = ThreadLocalRandom.current().nextInt(templateList.size());
            return templateList.get(randomIndex);
        }
    }

    public String mergeTypeAndTag(String type, String tag) {
        return type + TYPE_TAG_SPLIT_FLAG + tag;
    }

    public void clearResources(String type) {
        this.resourceMap.remove(type);
    }

    public void clearAllResources() {
        this.resourceMap.clear();
    }

    public Map<String, List<Resource>> listAllResources() {
        return this.resourceMap;
    }

    public List<Resource> listResourcesByType(String type) {
        return this.resourceMap.getOrDefault(type, Collections.emptyList());
    }

    public int getAllResourceCount() {
        return this.resourceMap.values().stream().mapToInt(List::size).sum();
    }

    public int getResourceCount(String type) {
        return this.resourceMap.getOrDefault(type, Collections.emptyList()).size();
    }

    public void clearAllTemplates() {
        this.templateResourceMap.clear();
    }

    public void clearTemplates(String type) {
        this.templateResourceMap.remove(type);
    }

    public List<ResourceMap> listTemplatesByType(String type) {
        return this.templateResourceMap.getOrDefault(type, Collections.emptyList());
    }

    public Map<String, List<ResourceMap>> listAllTemplates() {
        return this.templateResourceMap;
    }
}
