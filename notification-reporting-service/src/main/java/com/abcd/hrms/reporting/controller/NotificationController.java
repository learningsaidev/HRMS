package com.abcd.hrms.reporting.controller;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.reporting.dto.NotificationDto;
import com.abcd.hrms.reporting.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationDto.Response>> sendNotification(
            @Valid @RequestBody NotificationDto.SendRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(notificationService.sendNotification(request)));
    }

    @PostMapping("/in-app")
    public ResponseEntity<ApiResponse<NotificationDto.InAppResponse>> createInApp(
            @Valid @RequestBody NotificationDto.CreateInAppRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(notificationService.createInAppNotification(request)));
    }

    @GetMapping("/my-notifications")
    public ResponseEntity<ApiResponse<List<NotificationDto.InAppResponse>>> getMyNotifications() {
        UUID userId = TenantContext.getUserId();
        if (userId == null) {
            userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUserNotifications(userId)));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationDto.InAppResponse>> markAsRead(@PathVariable UUID notificationId) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.markAsRead(notificationId)));
    }
}
