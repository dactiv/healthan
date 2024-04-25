package com.github.dactiv.healthan.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConfigOverrides;
import com.github.dactiv.healthan.commons.annotation.IgnoreField;
import com.github.dactiv.healthan.commons.exception.SystemException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 转型工具类
 *
 * @author maurice.chen
 **/
public abstract class Casts {

    /**
     * 问号
     */
    public static final String QUESTION_MARK = "?";

    /**
     * 点符号
     */
    public static final String DOT = ".";

    /**
     *
     */
    public static final String UNDERSCORE = "_";

    /**
     * 负极符号
     */
    public final static String NEGATIVE = "-";

    /**
     * 分号
     */
    public final static String SEMICOLON = ";";

    /**
     * 逗号
     */
    public final static String COMMA = ",";

    /**
     * 默认等于符号
     */
    public static final String EQ = "=";

    /**
     * 默认 and 符号
     */
    public static final String HTTP_AND = "&";

    /**
     * 路径变量开始符号
     */
    public static final String HTTP_PATH_VARIABLE_START = "{";

    /**
     * 路径变量结束符号
     */
    public static final String HTTP_PATH_VARIABLE_END = "}";

    /**
     * 左括号
     */
    public static final String LEFT_BRACKET = "(";

    /**
     * 有括号
     */
    public static final String RIGHT_BRACKET = ")";

    /**
     *  map 类型范型引用
     */
    public static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};

    public static final ParameterizedTypeReference<Map<String, Object>> MAP_PARAMETERIZED_TYPE_REFERENCE = new ParameterizedTypeReference<Map<String, Object>>() {};
    /**
     * list map 类型范型引用
     */
    public static final TypeReference<List<Map<String, Object>>> LIST_MAP_TYPE_REFERENCE = new TypeReference<List<Map<String, Object>>>() {};

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static JsonInclude.Value jsonDefaultPropertyInclusion;

    static {
        setJsonDefaultPropertyInclusion(objectMapper);
    }

    /**
     * 设置 jackson objectMapper
     *
     * @param objectMapper objectMapper
     */
    public static void setObjectMapper(ObjectMapper objectMapper) {
        Casts.objectMapper = objectMapper;
        setJsonDefaultPropertyInclusion(objectMapper);
    }

    private static void setJsonDefaultPropertyInclusion(ObjectMapper objectMapper) {
        Field field = ReflectionUtils.findFiled(objectMapper, "_configOverrides");
        ConfigOverrides configOverrides = cast(ReflectionUtils.getFieldValue(objectMapper, field));
        jsonDefaultPropertyInclusion = configOverrides.getDefaultInclusion();
    }

    /**
     * 将值转换成指定类型的对象
     *
     * @param value 值
     * @param type  指定类型
     * @param <T>   对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T convertValue(Object value, Class<T> type) {
        return objectMapper.convertValue(value, type);
    }

    /**
     * 将值转换成指定类型的对象
     *
     * @param value       值
     * @param toValueType 指定类型
     * @param <T>         对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T convertValue(Object value, JavaType toValueType) {
        return objectMapper.convertValue(value, toValueType);
    }

    /**
     * 将值转换成指定类型的对象
     *
     * @param value 值
     * @param type  引用类型
     * @param <T>   对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T convertValue(Object value, TypeReference<T> type) {
        return objectMapper.convertValue(value, type);
    }

    /**
     * 获取 object mapper
     *
     * @return object mapper
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 将值转换为 json 字符串
     *
     * @param value 值
     * @return json 字符串
     */
    public static String writeValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new SystemException(e);
        }
    }

    public static String writeValueAsString(Object value, JsonInclude.Include include) {
        try {
            String result = objectMapper.setDefaultPropertyInclusion(include).writeValueAsString(value);
            objectMapper.setDefaultPropertyInclusion(jsonDefaultPropertyInclusion);
            return result;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 json 字符串转换为指定类型的对象
     *
     * @param json json 字符串
     * @param type 指定类型的对象 class
     * @param <T>  对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(String json, Class<T> type) {

        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    /**
     * 将 json 字符串转换为指定类型的对象
     *
     * @param json json 字符串
     * @param type 引用类型
     * @param <T>  对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(String json, TypeReference<T> type) {

        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }

    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param stream input 流
     * @param type   用于包含信息和作为反序列化器键的类型标记类的基类
     * @param <T>    对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(InputStream stream, JavaType type) {
        try {
            return objectMapper.readValue(stream, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param stream input 流
     * @param type   指定类型的对象 class
     * @param <T>    对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(InputStream stream, Class<T> type) {
        try {
            return objectMapper.readValue(stream, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 json 字符串转换为指定类型的对象
     *
     * @param stream input 流
     * @param type   引用类型
     * @param <T>    对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(InputStream stream, TypeReference<T> type) {

        try {
            return objectMapper.readValue(stream, type);
        } catch (Exception e) {
            throw new SystemException(e);
        }

    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param json json 字符串
     * @param type 用于包含信息和作为反序列化器键的类型标记类的基类
     * @param <T>  对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(String json, JavaType type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param bytes bytes 内容
     * @param type  指定类型的对象 class
     * @param <T>   对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(byte[] bytes, Class<T> type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param bytes bytes 内容
     * @param type  用于包含信息和作为反序列化器键的类型标记类的基类
     * @param <T>   对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(byte[] bytes, JavaType type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将 bytes 内容转换为指定类型的对象
     *
     * @param bytes bytes 内容
     * @param type  用于包含信息和作为反序列化器键的类型标记类的基类
     * @param <T>   对象范型实体值
     * @return 指定类型的对象实例
     */
    public static <T> T readValue(byte[] bytes, TypeReference<T> type) {
        try {
            return objectMapper.readValue(bytes, type);
        } catch (IOException e) {
            throw new SystemException(e);
        }
    }

    /**
     * 将格式为 http query string 的字符串转型为成 MultiValueMap
     *
     * @param body 数据题
     * @return 转换后的 MultiValueMap 对象
     */
    public static MultiValueMap<String, String> castRequestBodyMap(String body) {
        MultiValueMap<String, String> result = new LinkedMultiValueMap<>();

        Arrays.stream(StringUtils.split(body, HTTP_AND)).forEach(b -> {
            String key = StringUtils.substringBefore(b, EQ);
            String value = StringUtils.substringAfter(b, EQ);
            result.add(key, value);
        });

        return result;
    }

    public static String toSnakeCase(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append(UNDERSCORE).append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 将 MultiValueMap 对象转换为 name=value&amp;name2=value2&amp;name3=value3 格式字符串
     *
     * @param newRequestBody MultiValueMap 对象
     * @return 转换后的字符串
     */
    public static <K,V> String castRequestBodyMapToString(MultiValueMap<K, V> newRequestBody) {
        return castRequestBodyMapToString(newRequestBody, Object::toString);
    }

    /**
     * 将 MultiValueMap 对象转换为 name=value&amp;name2=value2&amp;name3=value3 格式字符串
     *
     * @param newRequestBody MultiValueMap 对象
     * @param function       处理字符串的功能
     * @return 转换后的字符串
     */
    public static <K,V> String castRequestBodyMapToString(MultiValueMap<K, V> newRequestBody, Function<V, String> function) {
        StringBuilder result = new StringBuilder();

        newRequestBody
                .forEach((key, value) -> value
                        .forEach(
                                v -> result
                                        .append(key)
                                        .append(EQ)
                                        .append(value.size() > 1 ? value.stream().map(function).collect(Collectors.toList()) : function.apply(value.get(0)))
                                        .append(HTTP_AND)
                        )
                );

        if (result.length() > 1) {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    /**
     * 将数 map 数据转换成 普通的 map 对象，如果值为 map 参数的值为1个以上的数组值，将该 key 对应的值转换成 list 对象
     *
     * @param map 要转换的 map 对象
     *
     * @return 新的 map 对象
     */
    public static <K,V> Map<K, Object> castArrayValueMapToObjectValueMap(Map<K, V[]> map) {
        return castArrayValueMapToObjectValueMap(map, s -> s);
    }

    /**
     * 将数组值的 map 数据转换成 MultiValueMap 对象
     *
     * @param map map 对象
     *
     * @return MultiValueMap
     *
     */
    public static <K,V> MultiValueMap<K, V> castMapToMultiValueMap(Map<K,V[]> map) {
        MultiValueMap<K,V> result = new LinkedMultiValueMap<>();

        map.forEach((key, value) -> result.put(key, Arrays.asList(value)));

        return result;
    }

    /**
     * 将 key 为 String， value 为 String 数组的 map 数据转换成 key 为 String，value 为 object 的 map 对象
     *
     * @param map      key 为 String， value 为 String 数组的 map
     * @param function 处理字符串的功能
     * @return key 为 String，value 为 object 的 map 对象
     */
    public static <K,V> Map<K, Object> castArrayValueMapToObjectValueMap(Map<K, V[]> map, Function<V, Object> function) {
        Map<K, Object> result = new LinkedHashMap<>();

        map.forEach((k, v) -> {
            if (v.length > 1) {
                result.put(k, Arrays.stream(v).map(function).collect(Collectors.toList()));
            } else {
                result.put(k, function.apply(v[0]));
            }
        });

        return result;
    }

    /**
     * 集合转换器的实现
     *
     * @author maurice.chen
     */
    @SuppressWarnings("rawtypes")
    private static class CollectionConverter implements Converter {

        @Override
        public <T> T convert(Class<T> type, Object value) {
            Class<?> typeInstance;

            if (type.isInterface() && Set.class.isAssignableFrom(type)) {
                typeInstance = LinkedHashSet.class;
            } else if (type.isInterface() && (List.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type))) {
                typeInstance = LinkedList.class;
            } else if (!type.isInterface()) {
                typeInstance = type;
            } else {
                typeInstance = value.getClass();
            }

            Object obj = ClassUtils.newInstance(typeInstance);
            Collection<?> collection = null;

            if (Collection.class.isAssignableFrom(obj.getClass())) {
                collection = (Collection<?>) obj;
            }

            if (collection == null) {
                return type.cast(value);
            }

            if (Collection.class.isAssignableFrom(value.getClass())) {
                Collection values = (Collection) value;
                collection.addAll(values);
            }

            return type.cast(obj);
        }

    }

    static {
        registerDateConverter("yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss");
        registerCollectionConverter();
    }

    /**
     * 注册集合类型的转换器
     */
    private static void registerCollectionConverter() {
        ConvertUtils.register(new CollectionConverter(), Collection.class);
        ConvertUtils.register(new CollectionConverter(), List.class);
        ConvertUtils.register(new CollectionConverter(), ArrayList.class);
        ConvertUtils.register(new CollectionConverter(), LinkedList.class);
        ConvertUtils.register(new CollectionConverter(), Set.class);
        ConvertUtils.register(new CollectionConverter(), HashSet.class);
        ConvertUtils.register(new CollectionConverter(), LinkedHashSet.class);
    }

    /**
     * 注册一个时间类型的转换器,当前默认的格式为：yyyy-MM-dd
     *
     * @param patterns 日期格式
     */
    private static void registerDateConverter(String... patterns) {
        DateConverter dc = new DateConverter();
        dc.setUseLocaleFormat(true);
        dc.setPatterns(patterns);
        ConvertUtils.register(dc, Date.class);
    }

    /**
     * 通过路径获取 map 实体
     *
     * @param source map 数据源
     * @param path   路径，多个以点(".")分割
     * @return map 实体
     */
    public static Map<String, Object> getPathMap(Map<String, Object> source, String path) {

        Map<String, Object> result = new LinkedHashMap<>(source);

        String[] strings = StringUtils.split(path, DOT);

        for (String s : strings) {
            result = Casts.cast(result.get(s));
        }

        return result;

    }

    /**
     * 将 value 转型为返回值类型
     *
     * @param value 值
     * @param <T>   值类型
     * @return 转型后的值
     */
    public static <T> T cast(Object value) {
        if (value == null) {
            return null;
        }
        return (T) cast(value, value.getClass());
    }

    /**
     * 如果 value 不为 null 值，将 value 转型为返回值类型
     *
     * @param value 值
     * @param <T>   值类型
     * @return 转型后的值
     */
    public static <T> T castIfNotNull(Object value) {
        if (value == null) {
            return null;
        }

        return cast(value);
    }

    /**
     * 将 value 转型为返回值类型
     *
     * @param value 值
     * @param type  值类型 class
     * @param <T>   值类型
     * @return 转型后的值
     */
    public static <T> T cast(Object value, Class<T> type) {
        return (T) (value == null ? null : ConvertUtils.convert(value, type));
    }

    /**
     * 讲值转型为 Optional 类型
     *
     * @param value 值
     * @param <T>   值类型
     * @return Optional
     */
    public static <T> Optional<T> castOptional(Object value) {
        return Optional.ofNullable((T) value);
    }

    /**
     * 如果 value 不为 null 值，将 value 转型为返回值类型
     *
     * @param value 值
     * @param type  值类型 class
     * @param <T>   值类型
     * @return 转型后的值
     */
    public static <T> T castIfNotNull(Object value, Class<T> type) {

        if (value == null) {
            return null;
        }

        return cast(value, type);
    }

    /**
     * 设置 url 路径变量值
     *
     * @param url           url 路径
     * @param variableValue url 路径的变量对应值 map
     * @return 新的 url 路径
     */
    public static String setUrlPathVariableValue(String url, Map<String, String> variableValue) {

        String[] vars = StringUtils.substringsBetween(url, HTTP_PATH_VARIABLE_START, HTTP_PATH_VARIABLE_END);

        List<String> varList = Arrays.asList(vars);

        List<String> existList = varList
                .stream()
                .map(StringUtils::trimToEmpty)
                .filter(variableValue::containsKey)
                .collect(Collectors.toList());

        String temp = url;

        for (String s : existList) {
            String searchString = HTTP_PATH_VARIABLE_START + s + HTTP_PATH_VARIABLE_END;
            temp = StringUtils.replace(temp, searchString, variableValue.get(s));
        }

        return temp;
    }

    /**
     * 创建一个新的对象，并将 source 属性内容拷贝到创建的对象中
     *
     * @param source           原数据
     * @param targetClass      新的对象类型
     * @param ignoreProperties 要忽略的属性名称
     * @return 新的对象内容
     */
    public static <T> T of(Object source, Class<T> targetClass, String... ignoreProperties) {

        if (Objects.isNull(source)) {
            return null;
        }

        T result = ClassUtils.newInstance(targetClass);

        BeanUtils.copyProperties(source, result, ignoreProperties);

        return result;
    }

    public static <T> T ofMap(Map<String,Object> source, Class<T> targetClass, String... ignoreProperties) {
        T result = ClassUtils.newInstance(targetClass);

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            if (ArrayUtils.contains(ignoreProperties, entry.getKey())) {
                continue;
            }
            Field field = ReflectionUtils.findFiled(result, entry.getKey());
            if (Objects.isNull(field)) {
                continue;
            }

            ReflectionUtils.setFieldValue(result, field.getName(), cast(entry.getValue(), field.getType()));
        }

        return result;
    }


    public static List<Field> getIgnoreField(Class<?> targetClass) {
        List<Field> fields = new LinkedList<>();
        for (Field o : targetClass.getDeclaredFields()) {
            IgnoreField ignoreField = o.getAnnotation(IgnoreField.class);
            if (Objects.isNull(ignoreField)) {
                continue;
            }
            fields.add(o);
        }
        return fields;
    }

    public static boolean isPrimitive(Object value) {
        return (value instanceof Boolean ||
                value instanceof Byte ||
                value instanceof Character ||
                value instanceof Short ||
                value instanceof Integer ||
                value instanceof Long ||
                value instanceof Float ||
                value instanceof Double);
    }

    /**
     * 将数据库列名称转驼峰
     *
     * @param columnName 列名称
     *
     * @return 驼峰名称
     */
    public static String castDataBaseColumnNameToCamelCase(String columnName) {
        // 首先将字段名按下划线分割成单词
        String[] words = columnName.split(UNDERSCORE);

        // 如果只有一个单词，无需转换，直接返回
        if (words.length == 1) {
            return words[0];
        }

        // 从第二个单词开始，将首字母大写
        StringBuilder result = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            String word = words[i];
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }

        return result.toString();
    }

}
