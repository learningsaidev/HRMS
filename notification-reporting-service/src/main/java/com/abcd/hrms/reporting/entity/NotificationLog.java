package com.abcd.hrms.reporting.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_logs")
public class NotificationLog extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "channel", nullable = false, length = 30)
    @Builder.Default
    private String channel = "EMAIL"; // EMAIL, IN_APP, SMS, SLACK

    @Column(name = "template_name", nullable = false, length = 100)
    private String templateName;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SENT, FAILED

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "sent_at")
    private Instant sentAt;
}

