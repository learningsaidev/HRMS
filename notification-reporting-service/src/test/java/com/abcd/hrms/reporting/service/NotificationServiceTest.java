package com.abcd.hrms.reporting.service;

import com.abcd.hrms.reporting.dto.NotificationDto;
import com.abcd.hrms.reporting.entity.InAppNotification;
import com.abcd.hrms.reporting.entity.NotificationLog;
import com.abcd.hrms.reporting.repository.InAppNotificationRepository;
import com.abcd.hrms.reporting.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Dispatch & In-App Notification Tests")
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;
    @Mock
    private InAppNotificationRepository inAppNotificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    void testSendEmailNotification() {
        NotificationDto.SendRequest request = new NotificationDto.SendRequest();
        request.setCompanyId(companyId);
        request.setRecipientEmail("employee@abcd.corp");
        request.setTemplateName("PAYSLIP_GENERATED");
        request.setSubject("Your November 2026 Payslip is Ready");
        request.setContent("Dear Employee, your payslip for November 2026 has been generated.");

        when(notificationLogRepository.save(any(NotificationLog.class))).thenAnswer(i -> {
            NotificationLog n = i.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        NotificationDto.Response response = notificationService.sendNotification(request);
        assertNotNull(response);
        assertEquals("SENT", response.getStatus());
        assertEquals("employee@abcd.corp", response.getRecipientEmail());
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    void testCreateAndMarkInAppNotificationRead() {
        UUID notifId = UUID.randomUUID();
        NotificationDto.CreateInAppRequest request = new NotificationDto.CreateInAppRequest();
        request.setCompanyId(companyId);
        request.setUserId(userId);
        request.setTitle("Leave Approved");
        request.setMessage("Your casual leave request for Dec 1-2 has been approved.");
        request.setActionUrl("/leaves/history");

        when(inAppNotificationRepository.save(any(InAppNotification.class))).thenAnswer(i -> {
            InAppNotification n = i.getArgument(0);
            if (n.getId() == null) n.setId(notifId);
            return n;
        });

        NotificationDto.InAppResponse created = notificationService.createInAppNotification(request);
        assertNotNull(created);
        assertFalse(created.getIsRead());

        InAppNotification mockNotif = InAppNotification.builder()
                .companyId(companyId)
                .userId(userId)
                .title(request.getTitle())
                .message(request.getMessage())
                .isRead(false)
                .build();
        mockNotif.setId(notifId);

        when(inAppNotificationRepository.findById(notifId)).thenReturn(Optional.of(mockNotif));

        NotificationDto.InAppResponse readNotif = notificationService.markAsRead(notifId);
        assertTrue(readNotif.getIsRead());
        assertNotNull(readNotif.getReadAt());
    }
}

