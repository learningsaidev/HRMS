package com.abcd.hrms.onboarding.service;

import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.onboarding.dto.BgvCaseDto;
import com.abcd.hrms.onboarding.dto.BgvReviewRequest;
import com.abcd.hrms.onboarding.dto.CandidateConsentRequest;
import com.abcd.hrms.onboarding.dto.CreateCandidateCaseRequest;
import com.abcd.hrms.onboarding.dto.OnboardingCaseDto;
import com.abcd.hrms.onboarding.entity.BgvCase;
import com.abcd.hrms.onboarding.entity.BgvCheckItem;
import com.abcd.hrms.onboarding.entity.ConsentRecord;
import com.abcd.hrms.onboarding.entity.OnboardingCase;
import com.abcd.hrms.onboarding.repository.BgvCaseRepository;
import com.abcd.hrms.onboarding.repository.BgvCheckItemRepository;
import com.abcd.hrms.onboarding.repository.CandidateDocumentRepository;
import com.abcd.hrms.onboarding.repository.ConsentRecordRepository;
import com.abcd.hrms.onboarding.repository.OnboardingCaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private OnboardingCaseRepository onboardingCaseRepository;
    @Mock
    private CandidateDocumentRepository documentRepository;
    @Mock
    private ConsentRecordRepository consentRecordRepository;
    @Mock
    private BgvCaseRepository bgvCaseRepository;
    @Mock
    private BgvCheckItemRepository bgvCheckItemRepository;
    @Mock
    private OutboxPublisherService outboxPublisherService;

    @InjectMocks
    private OnboardingService onboardingService;

    private UUID caseId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        caseId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    void testCreateCandidateCase() {
        CreateCandidateCaseRequest req = new CreateCandidateCaseRequest();
        req.setCandidateName("Alice Candidate");
        req.setCandidateEmail("alice@test.com");
        req.setCompanyId(companyId);
        req.setProposedJoiningDate(LocalDate.now().plusDays(14));
        req.setProposedDesignation("Senior Engineer");

        when(onboardingCaseRepository.save(any(OnboardingCase.class))).thenAnswer(i -> {
            OnboardingCase c = i.getArgument(0);
            c.setId(caseId);
            return c;
        });

        OnboardingCaseDto dto = onboardingService.createCandidateCase(req);
        assertNotNull(dto);
        assertEquals("Alice Candidate", dto.getCandidateName());
        assertEquals("PREBOARDING", dto.getStatus());
        assertNotNull(dto.getSecureToken());
    }

    @Test
    void testConsentSubmission() {
        OnboardingCase mockCase = OnboardingCase.builder()
                .companyId(companyId)
                .candidateName("Alice Candidate")
                .candidateEmail("alice@test.com")
                .secureToken("token-123")
                .status("PREBOARDING")
                .build();
        mockCase.setId(caseId);

        when(onboardingCaseRepository.findBySecureToken("token-123")).thenReturn(Optional.of(mockCase));
        when(consentRecordRepository.save(any(ConsentRecord.class))).thenAnswer(i -> i.getArgument(0));
        when(onboardingCaseRepository.save(any(OnboardingCase.class))).thenAnswer(i -> i.getArgument(0));

        CandidateConsentRequest consentReq = new CandidateConsentRequest();
        consentReq.setSecureToken("token-123");
        consentReq.setConsentText("I agree to background verification");

        OnboardingCaseDto updated = onboardingService.submitConsent(consentReq, "127.0.0.1", "JUnit-Agent");
        assertEquals("CONSENT_GRANTED", updated.getStatus());
        verify(consentRecordRepository, times(1)).save(any(ConsentRecord.class));
    }

    @Test
    void testBgvInitiationRequiresConsent() {
        OnboardingCase mockCase = OnboardingCase.builder()
                .companyId(companyId)
                .status("PREBOARDING")
                .build();
        mockCase.setId(caseId);

        when(onboardingCaseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));

        assertThrows(BusinessValidationException.class, () -> onboardingService.initiateBgv(caseId));
    }

    @Test
    void testBgvApprovalPublishesEvent() {
        UUID bgvId = UUID.randomUUID();
        OnboardingCase mockCase = OnboardingCase.builder()
                .companyId(companyId)
                .targetLegalEntityId(UUID.randomUUID())
                .targetDepartmentId(UUID.randomUUID())
                .targetLocationId(UUID.randomUUID())
                .candidateName("Alice Candidate")
                .candidateEmail("alice@test.com")
                .proposedDesignation("Lead Dev")
                .proposedJoiningDate(LocalDate.now().plusDays(10))
                .status("BGV_INITIATED")
                .build();
        mockCase.setId(caseId);

        BgvCase bgvCase = BgvCase.builder()
                .onboardingCaseId(caseId)
                .status("IN_PROGRESS")
                .build();
        bgvCase.setId(bgvId);

        when(bgvCaseRepository.findById(bgvId)).thenReturn(Optional.of(bgvCase));
        when(onboardingCaseRepository.findById(caseId)).thenReturn(Optional.of(mockCase));
        when(bgvCaseRepository.save(any(BgvCase.class))).thenAnswer(i -> i.getArgument(0));
        when(bgvCheckItemRepository.findByBgvCaseId(bgvId)).thenReturn(new ArrayList<>());
        when(onboardingCaseRepository.save(any(OnboardingCase.class))).thenAnswer(i -> i.getArgument(0));

        BgvReviewRequest reviewReq = new BgvReviewRequest();
        reviewReq.setStatus("CLEAR");
        reviewReq.setReviewerNotes("All checks cleared");

        BgvCaseDto reviewed = onboardingService.reviewBgvCase(bgvId, reviewReq);
        assertEquals("CLEAR", reviewed.getStatus());
        assertEquals("ONBOARDING_COMPLETED", mockCase.getStatus());
        verify(outboxPublisherService, times(1)).enqueueEvent(any(), any(), any(), any());
    }
}
