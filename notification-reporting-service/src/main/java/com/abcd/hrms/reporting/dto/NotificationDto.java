package com.abcd.hrms.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public class NotificationDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID recipientId;
        private String recipientEmail;
        private String channel;
        private String templateName;
        private String subject;
        private String content;
        private String status;
        private Instant sentAt;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InAppResponse {
        private UUID id;
        private UUID userId;
        private String title;
        private String message;
        private String actionUrl;
        private Boolean isRead;
        private Instant readAt;
        private Instant createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendRequest {
        private UUID companyId;
        private UUID recipientId;
        @NotBlank(message = "Recipient email is mandatory")
        private String recipientEmail;
        private String channel = "EMAIL";
        @NotBlank(message = "Template name is mandatory")
        private String templateName;
        @NotBlank(message = "Subject is mandatory")
        private String subject;
        @NotBlank(message = "Content is mandatory")
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateInAppRequest {
        private UUID companyId;
        @NotNull(message = "User ID is mandatory")
        private UUID userId;
        @NotBlank(message = "Title is mandatory")
        private String title;
        @NotBlank(message = "Message is mandatory")
        private String message;
        private String actionUrl;
    }
}

