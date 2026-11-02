package com.abcd.hrms.common.messaging.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InboxEventRepository extends JpaRepository<InboxEvent, UUID> {

    boolean existsByEventIdAndConsumerGroup(UUID eventId, String consumerGroup);
}
