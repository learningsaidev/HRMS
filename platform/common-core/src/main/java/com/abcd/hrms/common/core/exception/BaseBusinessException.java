package com.abcd.hrms.common.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base abstract runtime exception for all ABCD HRMS domain business exceptions.
 */
@Getter
public abstract class BaseBusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    protected BaseBusinessException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected BaseBusinessException(String message, Throwable cause, String errorCode, HttpStatus httpStatus) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
