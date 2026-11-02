package com.abcd.hrms.common.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standard API error contract.
 * Never exposes Java stack traces to clients while providing correlation ID for diagnostics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String code;

    private String message;

    private String correlationId;

    private String path;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private List<FieldErrorDetail> fieldErrors;
}
