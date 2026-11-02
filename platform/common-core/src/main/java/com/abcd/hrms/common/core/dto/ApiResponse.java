package com.abcd.hrms.common.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standard enterprise API response envelope across all ABCD HRMS microservices.
 * Guarantees uniform structure for both successful responses and handled errors.
 *
 * @param <T> Payload data type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private T data;

    private ApiError error;

    private Map<String, Object> metadata;

    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ok(data);
    }

    public static <T> ApiResponse<T> ok(T data, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .timestamp(Instant.now())
                .build();
    }
}
