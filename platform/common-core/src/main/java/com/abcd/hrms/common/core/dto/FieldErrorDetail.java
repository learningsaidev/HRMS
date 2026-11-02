package com.abcd.hrms.common.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Granular field validation error detail.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorDetail {

    private String field;

    private Object rejectedValue;

    private String message;
}
