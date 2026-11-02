package com.abcd.hrms.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation conflicts with existing entity state (e.g. duplicate key, concurrent modification).
 */
public class ConflictException extends BaseBusinessException {

    public ConflictException(String message) {
        super(message, "ERR_CONFLICT", HttpStatus.CONFLICT);
    }

    public ConflictException(String entityName, String conflictDetail) {
        super(String.format("Conflict in %s: %s", entityName, conflictDetail), "ERR_CONFLICT", HttpStatus.CONFLICT);
    }
}
