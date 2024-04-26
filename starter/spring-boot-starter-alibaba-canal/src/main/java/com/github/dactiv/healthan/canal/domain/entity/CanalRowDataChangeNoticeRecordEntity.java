package com.github.dactiv.healthan.canal.domain.entity;

import com.github.dactiv.healthan.commons.domain.body.AckResponseBody;
import com.github.dactiv.healthan.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.healthan.commons.enumerate.support.Protocol;
import com.github.dactiv.healthan.commons.retry.Retryable;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class CanalRowDataChangeNoticeRecordEntity implements Serializable, Retryable, ExecuteStatus.Body {

    private static final long serialVersionUID = 7867580272505563609L;

    /**
     * 通知类型
     */
    private Protocol protocol;

    /**
     * 元数据信息
     */
    private Map<String, Object> protocolMeta;

    /**
     * 请求体
     */
    private Map<String, Object> requestBody;

    /**
     * 响应体
     */
    private AckResponseBody responseBody;

    /**
     * 响应时间
     */
    private Date successTime;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 最后发送时间
     */
    private Date lastSendTime;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 执行状态
     */
    private ExecuteStatus executeStatus = ExecuteStatus.Processing;

    public CanalRowDataChangeNoticeRecordEntity() {

    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Map<String, Object> getProtocolMeta() {
        return protocolMeta;
    }

    public void setProtocolMeta(Map<String, Object> protocolMeta) {
        this.protocolMeta = protocolMeta;
    }

    public Map<String, Object> getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Map<String, Object> requestBody) {
        this.requestBody = requestBody;
    }

    public AckResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(AckResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public Date getSuccessTime() {
        return successTime;
    }

    @Override
    public void setSuccessTime(Date successTime) {
        this.successTime = successTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public Date getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(Date lastSendTime) {
        this.lastSendTime = lastSendTime;
    }

    @Override
    public String getException() {
        return exception;
    }

    @Override
    public void setException(String exception) {
        this.exception = exception;
    }

    @Override
    public ExecuteStatus getExecuteStatus() {
        return executeStatus;
    }

    @Override
    public void setExecuteStatus(ExecuteStatus executeStatus) {
        this.executeStatus = executeStatus;
    }
}
