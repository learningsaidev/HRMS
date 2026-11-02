package com.abcd.hrms.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an entity requested by ID or business key does not exist.
 */
public class EntityNotFoundException extends BaseBusinessException {

    public EntityNotFoundException(String entityName, Object identifier) {
        super(String.format("%s with identifier [%s] was not found", entityName, identifier),
                "ERR_ENTITY_NOT_FOUND",
                HttpStatus.NOT_FOUND);
    }

    public EntityNotFoundException(String message) {
        super(message, "ERR_ENTITY_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
