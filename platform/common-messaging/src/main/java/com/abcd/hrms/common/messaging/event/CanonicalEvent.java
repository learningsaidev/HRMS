package com.abcd.hrms.common.messaging.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Standard enterprise domain event envelope (CanonicalEvent&lt;T&gt;) for Apache Kafka messaging.
 * Ensures uniform tracing, auditing, and multi-tenant partitioning across all microservices.
 *
 * @param <T> Domain event payload type
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanonicalEvent<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    private String eventType;

    @Builder.Default
    private String schemaVersion = "1.0.0";

    private String aggregateType;

    private String aggregateId;

    private UUID companyId;

    private UUID legalEntityId;

    @Builder.Default
    private Instant occurredAt = Instant.now();

    private String producer;

    private String correlationId;

    private String causationId;

    @Builder.Default
    private Map<String, String> traceContext = new HashMap<>();

    private T payload;

    public static <T> CanonicalEvent<T> of(String eventType, String aggregateType, String aggregateId,
                                           UUID companyId, UUID legalEntityId, String producer,
                                           String correlationId, T payload) {
        return CanonicalEvent.<T>builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .companyId(companyId)
                .legalEntityId(legalEntityId)
                .producer(producer)
                .correlationId(correlationId)
                .occurredAt(Instant.now())
                .payload(payload)
                .build();
    }
}
