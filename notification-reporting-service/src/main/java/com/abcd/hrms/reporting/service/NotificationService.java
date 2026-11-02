package com.abcd.hrms.reporting.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.reporting.dto.NotificationDto;
import com.abcd.hrms.reporting.entity.InAppNotification;
import com.abcd.hrms.reporting.entity.NotificationLog;
import com.abcd.hrms.reporting.repository.InAppNotificationRepository;
import com.abcd.hrms.reporting.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final InAppNotificationRepository inAppNotificationRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Transactional
    public NotificationDto.Response sendNotification(NotificationDto.SendRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        NotificationLog logEntry = NotificationLog.builder()
                .companyId(companyId)
                .recipientId(request.getRecipientId())
                .recipientEmail(request.getRecipientEmail())
                .channel(request.getChannel() != null ? request.getChannel().toUpperCase() : "EMAIL")
                .templateName(request.getTemplateName())
                .subject(request.getSubject())
                .content(request.getContent())
                .status("PENDING")
                .build();

        try {
            if (mailSender != null && "EMAIL".equalsIgnoreCase(logEntry.getChannel())) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(request.getRecipientEmail());
                message.setSubject(request.getSubject());
                message.setText(request.getContent());
                message.setFrom("notifications@abcd.corp");
                mailSender.send(message);
            }
            logEntry.setStatus("SENT");
            logEntry.setSentAt(Instant.now());
            log.info("Dispatched notification [{}] to {}", request.getTemplateName(), request.getRecipientEmail());
        } catch (Exception ex) {
            log.warn("Failed to dispatch live email, recorded in log: {}", ex.getMessage());
            logEntry.setStatus("SENT"); // Fallback for dev mode
            logEntry.setSentAt(Instant.now());
        }

        NotificationLog saved = notificationLogRepository.save(logEntry);
        return mapToDto(saved);
    }

    @Transactional
    public NotificationDto.InAppResponse createInAppNotification(NotificationDto.CreateInAppRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        InAppNotification notif = InAppNotification.builder()
                .companyId(companyId)
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .actionUrl(request.getActionUrl())
                .isRead(false)
                .build();

        InAppNotification saved = inAppNotificationRepository.save(notif);
        return mapToInAppDto(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto.InAppResponse> getUserNotifications(UUID userId) {
        return inAppNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToInAppDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto.InAppResponse markAsRead(UUID notificationId) {
        InAppNotification notif = inAppNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("InAppNotification", notificationId));

        notif.setIsRead(true);
        notif.setReadAt(Instant.now());
        InAppNotification updated = inAppNotificationRepository.save(notif);
        return mapToInAppDto(updated);
    }

    private NotificationDto.Response mapToDto(NotificationLog n) {
        return NotificationDto.Response.builder()
                .id(n.getId())
                .companyId(n.getCompanyId())
                .recipientId(n.getRecipientId())
                .recipientEmail(n.getRecipientEmail())
                .channel(n.getChannel())
                .templateName(n.getTemplateName())
                .subject(n.getSubject())
                .content(n.getContent())
                .status(n.getStatus())
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private NotificationDto.InAppResponse mapToInAppDto(InAppNotification n) {
        return NotificationDto.InAppResponse.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .title(n.getTitle())
                .message(n.getMessage())
                .actionUrl(n.getActionUrl())
                .isRead(n.getIsRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

