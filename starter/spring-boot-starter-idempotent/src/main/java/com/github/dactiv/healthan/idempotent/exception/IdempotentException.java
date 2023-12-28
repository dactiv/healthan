package com.github.dactiv.healthan.idempotent.exception;

import com.github.dactiv.healthan.commons.exception.SystemException;



/**
 * 幂等性异常
 *
 * @author maurice.chen
 */
public class IdempotentException extends SystemException {

    
    private static final long serialVersionUID = -8218863087525865969L;

    public IdempotentException() {
    }

    public IdempotentException(String message) {
        super(message);
    }

    public IdempotentException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdempotentException(Throwable cause) {
        super(cause);
    }
}
