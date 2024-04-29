package com.github.dactiv.healthan.commons.domain.body;

import com.github.dactiv.healthan.commons.enumerate.support.AckStatus;

import java.io.Serializable;
import java.util.Map;

/**
 * ack 确认响应题
 *
 * @author maurice.chen
 */
public class AckResponseBody implements Serializable {

    private static final long serialVersionUID = -504725054089049434L;

    /**
     * 确认信息
     */
    private AckStatus ack;

    /**
     *
     */
    private Map<String, Object> message;

    public AckResponseBody() {

    }

    public AckResponseBody(AckStatus ack) {
        this.ack = ack;
    }

    public AckResponseBody(AckStatus ack, Map<String, Object> message) {
        this.ack = ack;
        this.message = message;
    }

    public AckStatus getAck() {
        return ack;
    }

    public void setAck(AckStatus ack) {
        this.ack = ack;
    }

    public Map<String, Object> getMessage() {
        return message;
    }

    public void setMessage(Map<String, Object> message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AckResponseBody{" +
                "ack=" + ack +
                ", message=" + message +
                '}';
    }
}
