package com.github.dactiv.healthan.commons;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.healthan.commons.exception.ErrorCodeException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * rest 结果集实体类，用于统一返回指定格式数据使用
 *
 * @author maurice.chen
 **/
public class RestResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 8809220528389402403L;

    public static final String SUCCESS_EXECUTE_CODE = "200";

    public static final String UNKNOWN_EXECUTE_CODE = "404";

    public static final String PROCESSING_EXECUTE_CODE = "102";

    public static final String DEFAULT_PROCESSING_MESSAGE = "Processing";

    public static final String DEFAULT_SUCCESS_MESSAGE = "ok";

    public static final String DEFAULT_MESSAGE_NAME = "message";

    public static final String DEFAULT_DATA_NAME = "data";

    public static final String DEFAULT_STATUS_NAME = "status";

    public static final String DEFAULT_EXECUTE_CODE_NAME = "executeCode";

    public static final String DEFAULT_TIMESTAMP_NAME = "timestamp";

    public static final String DEFAULT_META_NAME = "meta";

    /**
     * 信息
     */
    private String message;

    /**
     * 执行代码
     */
    private int status;

    /**
     * 执行代码
     */
    private String executeCode;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 创建时间
     */
    private Date timestamp = new Date();

    /**
     * 元数据信息
     */
    private Map<String, Object> meta = new LinkedHashMap<>();

    /**
     * 创建一个抽象的 rest 结果集实体类
     */
    public RestResult() {
    }

    /**
     * 创建一个抽象的 rest 结果集实体类
     *
     * @param message 响应信息
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> of(String message) {
        return of(message, Integer.parseInt(SUCCESS_EXECUTE_CODE));
    }

    /**
     * 创建一个抽象的 rest 结果集实体类
     *
     * @param message 响应信息
     * @param status  执行状态
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> of(String message, int status) {
        return of(message, status, SUCCESS_EXECUTE_CODE);
    }

    /**
     * 创建一个抽象的 rest 结果集实体类
     *
     * @param message     响应信息
     * @param status      执行状态
     * @param executeCode 执行代码
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> of(String message, int status, String executeCode) {
        return new RestResult<>(message, status, executeCode, null);
    }

    /**
     * 创建一个抽象的 rest 结果集实体类
     *
     * @param message     响应信息
     * @param status      执行状态
     * @param executeCode 执行代码
     * @param data        响应数据
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> of(String message, int status, String executeCode, T data) {
        return new RestResult<>(message, status, executeCode, data);
    }

    /**
     * 创建一个成功的抽象 rest 结果集实体类
     *
     * @param data 响应内容
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofSuccess(T data) {
        return ofSuccess(DEFAULT_SUCCESS_MESSAGE, data);
    }

    /**
     * 创建一个成功的抽象 rest 结果集实体类
     *
     * @param message 响应信息
     * @param data    响应内容
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofSuccess(String message, T data) {
        return of(message, Integer.parseInt(SUCCESS_EXECUTE_CODE), SUCCESS_EXECUTE_CODE, data);
    }

    /**
     * 创建一个成功的抽象 rest 结果集实体类
     *
     * @param message     响应信息
     * @param executeCode 执行代码
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofSuccess(String message, String executeCode) {
        return of(message, Integer.parseInt(SUCCESS_EXECUTE_CODE), executeCode, null);
    }

    /**
     * 创建一个执行中的抽象 rest 结果集实体类
     *
     * @param data 响应内容
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofProcessing(T data) {
        return ofSuccess(PROCESSING_EXECUTE_CODE, data);
    }

    /**
     * 创建一个执行中的抽象 rest 结果集实体类
     *
     * @param message 响应信息
     * @param data    响应内容
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofProcessing(String message, T data) {
        return of(message, Integer.parseInt(PROCESSING_EXECUTE_CODE), PROCESSING_EXECUTE_CODE, data);
    }

    /**
     * 创建一个执行中的抽象 rest 结果集实体类
     *
     * @param message     响应信息
     * @param executeCode 执行代码
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofProcessing(String message, String executeCode) {
        return of(message, Integer.parseInt(PROCESSING_EXECUTE_CODE), executeCode, null);
    }

    /**
     * 创建一个异常的抽象 rest 结果集实体类
     *
     * @param throwable 异常信息
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofException(Throwable throwable) {
        return of(throwable.getMessage(), Integer.parseInt(ErrorCodeException.DEFAULT_EXCEPTION_CODE), ErrorCodeException.DEFAULT_EXCEPTION_CODE, null);
    }

    /**
     * 创建一个异常的抽象 rest 结果集实体类
     *
     * @param executeCode 执行代码
     * @param throwable   异常信息
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofException(String executeCode, Throwable throwable) {
        return of(throwable.getMessage(), Integer.parseInt(ErrorCodeException.DEFAULT_EXCEPTION_CODE), executeCode, null);
    }

    /**
     * 创建一个异常的抽象 rest 结果集实体类
     *
     * @param executeCode 执行代码
     * @param throwable   异常信息
     * @param data        数据内容
     *
     * @return rest 结果集
     */
    public static <T> RestResult<T> ofException(String executeCode, Throwable throwable, T data) {
        return of(throwable.getMessage(), Integer.parseInt(ErrorCodeException.DEFAULT_EXCEPTION_CODE), executeCode, data);
    }

    /**
     * 抽象的 rest 结果集实体类
     *
     * @param message     响应信息
     * @param status      执行状态
     * @param executeCode 执行代码
     * @param data        响应数据
     */
    public RestResult(String message, int status, String executeCode, T data) {
        this.message = message;
        this.status = status;
        this.executeCode = executeCode;
        this.data = data;
    }

    /**
     * 获取信息
     *
     * @return 信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置信息
     *
     * @param message 信息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置状态
     *
     * @param status 状态
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 获取执行代码
     *
     * @return 执行嗲吗
     */
    public String getExecuteCode() {
        return executeCode;
    }

    /**
     * 设置执行代码
     *
     * @param executeCode 执行代码
     */
    public void setExecuteCode(String executeCode) {
        this.executeCode = executeCode;
    }

    /**
     * 获取响应数据
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * 获取元数据信息
     *
     * @return 元数据信息
     */
    public Map<String, Object> getMeta() {
        return meta;
    }

    /**
     * 设置元数据信息
     *
     * @param meta 元数据信息
     */
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestResult<?> that = (RestResult<?>) o;
        return status == that.status && message.equals(that.message) && executeCode.equals(that.executeCode) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, status, executeCode, data);
    }

    /**
     * 设置创建时间
     *
     * @param timestamp 创建时间
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 是否成功
     *
     * @return true 是，否则 false
     */
    @JsonIgnore
    public boolean isSuccess() {
        return SUCCESS_EXECUTE_CODE.equals(String.valueOf(getStatus()));
    }

    /**
     * 是否处理中
     *
     * @return true 是，否则 false
     */
    @JsonIgnore
    public boolean isProcessing() {
        return PROCESSING_EXECUTE_CODE.equals(String.valueOf(getStatus()));
    }

    /**
     * 是否未知状态
     * @return true 是，否则 false
     */
    @JsonIgnore
    public boolean isUnknown() {
        return UNKNOWN_EXECUTE_CODE.equals(String.valueOf(getStatus()));
    }
}
