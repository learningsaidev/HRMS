package com.abcd.hrms.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when domain business validation fails (e.g. negative net salary, overlapping leave dates).
 */
public class BusinessValidationException extends BaseBusinessException {

    public BusinessValidationException(String message) {
        super(message, "ERR_BUSINESS_VALIDATION", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public BusinessValidationException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
