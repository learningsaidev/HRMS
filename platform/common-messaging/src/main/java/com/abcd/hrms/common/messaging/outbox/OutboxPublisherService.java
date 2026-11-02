package com.abcd.hrms.common.messaging.outbox;

import com.abcd.hrms.common.messaging.event.CanonicalEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service managing Transactional Outbox publishing.
 * Decouples database mutations from Kafka broker availability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Enqueues a canonical domain event into the local database outbox table.
     * MUST execute within the caller's active database transaction.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public <T> OutboxEvent enqueueEvent(String destinationTopic, String aggregateType, String aggregateId, CanonicalEvent<T> event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(event.getEventType())
                    .destinationTopic(destinationTopic)
                    .payload(jsonPayload)
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .build();

            return outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to serialize and enqueue outbox event for aggregate [{}]", aggregateId, e);
            throw new IllegalStateException("Failed to enqueue outbox event", e);
        }
    }

    /**
     * Poller task that flushes PENDING outbox events to Kafka.
     */
    @Scheduled(fixedDelayString = "${abcd.messaging.outbox.poll-interval-ms:2000}")
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc("PENDING", PageRequest.of(0, 50));
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            try {
                // Keyed by aggregateId to enforce strict chronological partitioning in Kafka
                kafkaTemplate.send(event.getDestinationTopic(), event.getAggregateId(), event.getPayload())
                        .addCallback(
                                result -> log.debug("Published outbox event [{}] to topic [{}]", event.getId(), event.getDestinationTopic()),
                                ex -> log.warn("Kafka dispatch failed for outbox event [{}]: {}", event.getId(), ex.getMessage())
                        );

                event.setStatus("PUBLISHED");
                event.setPublishedAt(Instant.now());
            } catch (Exception e) {
                log.error("Error publishing outbox event [{}]", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 490)) : "Unknown error");
                if (event.getRetryCount() >= 5) {
                    event.setStatus("FAILED");
                }
            }
        }
    }
}
