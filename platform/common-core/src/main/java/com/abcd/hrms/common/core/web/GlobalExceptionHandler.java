package com.abcd.hrms.common.core.web;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.dto.ApiError;
import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.common.core.dto.FieldErrorDetail;
import com.abcd.hrms.common.core.exception.BaseBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Centralized exception handler ensuring zero raw stack trace leakage to clients
 * while preserving complete diagnostic traceability via correlation IDs.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BaseBusinessException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        log.warn("Business exception [{}] occurred at path [{}]: {}. Correlation ID: {}",
                ex.getErrorCode(), request.getRequestURI(), ex.getMessage(), correlationId);

        ApiError error = ApiError.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .correlationId(correlationId)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(FieldErrorDetail.builder()
                    .field(fe.getField())
                    .rejectedValue(fe.getRejectedValue())
                    .message(fe.getDefaultMessage())
                    .build());
        }

        log.warn("Payload validation failed at path [{}]: {} field errors. Correlation ID: {}",
                request.getRequestURI(), fieldErrors.size(), correlationId);

        ApiError error = ApiError.builder()
                .code("ERR_ARGUMENT_VALIDATION")
                .message("Request payload validation failed. Check fieldErrors for details.")
                .correlationId(correlationId)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        List<FieldErrorDetail> fieldErrors = new ArrayList<>();
        ex.getConstraintViolations().forEach(cv -> fieldErrors.add(FieldErrorDetail.builder()
                .field(cv.getPropertyPath().toString())
                .rejectedValue(cv.getInvalidValue())
                .message(cv.getMessage())
                .build()));

        ApiError error = ApiError.builder()
                .code("ERR_CONSTRAINT_VIOLATION")
                .message("Constraint violation in request parameters")
                .correlationId(correlationId)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        String correlationId = resolveCorrelationId(request);
        log.error("Unhandled unexpected exception at path [{}]. Correlation ID: {}",
                request.getRequestURI(), correlationId, ex);

        ApiError error = ApiError.builder()
                .code("ERR_INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please provide the correlation ID to technical support.")
                .correlationId(correlationId)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(error));
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = TenantContext.getCorrelationId();
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = request.getHeader("X-Correlation-Id");
        }
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
}
