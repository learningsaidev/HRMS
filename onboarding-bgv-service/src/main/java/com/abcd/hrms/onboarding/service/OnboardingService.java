package com.abcd.hrms.onboarding.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.BusinessValidationException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.messaging.KafkaTopicConstants;
import com.abcd.hrms.common.messaging.event.CanonicalEvent;
import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.onboarding.dto.BgvCaseDto;
import com.abcd.hrms.onboarding.dto.BgvReviewRequest;
import com.abcd.hrms.onboarding.dto.CandidateConsentRequest;
import com.abcd.hrms.onboarding.dto.CreateCandidateCaseRequest;
import com.abcd.hrms.onboarding.dto.DocumentUploadRequest;
import com.abcd.hrms.onboarding.dto.OnboardingCaseDto;
import com.abcd.hrms.onboarding.entity.BgvCase;
import com.abcd.hrms.onboarding.entity.BgvCheckItem;
import com.abcd.hrms.onboarding.entity.CandidateDocument;
import com.abcd.hrms.onboarding.entity.ConsentRecord;
import com.abcd.hrms.onboarding.entity.OnboardingCase;
import com.abcd.hrms.onboarding.repository.BgvCaseRepository;
import com.abcd.hrms.onboarding.repository.BgvCheckItemRepository;
import com.abcd.hrms.onboarding.repository.CandidateDocumentRepository;
import com.abcd.hrms.onboarding.repository.ConsentRecordRepository;
import com.abcd.hrms.onboarding.repository.OnboardingCaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingCaseRepository onboardingCaseRepository;
    private final CandidateDocumentRepository documentRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final BgvCaseRepository bgvCaseRepository;
    private final BgvCheckItemRepository bgvCheckItemRepository;
    private final OutboxPublisherService outboxPublisherService;

    @Transactional
    public OnboardingCaseDto createCandidateCase(CreateCandidateCaseRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        String secureToken = "cand-" + UUID.randomUUID().toString().replace("-", "");

        OnboardingCase onboardingCase = OnboardingCase.builder()
                .companyId(companyId)
                .candidateName(request.getCandidateName())
                .candidateEmail(request.getCandidateEmail())
                .phoneNumber(request.getPhoneNumber())
                .targetLegalEntityId(request.getTargetLegalEntityId())
                .targetDepartmentId(request.getTargetDepartmentId())
                .targetLocationId(request.getTargetLocationId())
                .proposedDesignation(request.getProposedDesignation())
                .proposedJoiningDate(request.getProposedJoiningDate())
                .secureToken(secureToken)
                .status("PREBOARDING")
                .build();

        OnboardingCase saved = onboardingCaseRepository.save(onboardingCase);
        return mapToDto(saved);
    }

    @Transactional
    public OnboardingCaseDto submitConsent(CandidateConsentRequest request, String ipAddress, String userAgent) {
        OnboardingCase onboardingCase = onboardingCaseRepository.findBySecureToken(request.getSecureToken())
                .orElseThrow(() -> new EntityNotFoundException("OnboardingCase by token", request.getSecureToken()));

        String hash = sha256(request.getConsentText() + ipAddress + Instant.now().toString());

        ConsentRecord record = ConsentRecord.builder()
                .onboardingCaseId(onboardingCase.getId())
                .consentText(request.getConsentText())
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .userAgent(userAgent)
                .consentedAt(Instant.now())
                .consentHash(hash)
                .build();
        consentRecordRepository.save(record);

        onboardingCase.setStatus("CONSENT_GRANTED");
        OnboardingCase updated = onboardingCaseRepository.save(onboardingCase);
        return mapToDto(updated);
    }

    @Transactional
    public CandidateDocument uploadDocument(DocumentUploadRequest request) {
        OnboardingCase onboardingCase = onboardingCaseRepository.findById(request.getOnboardingCaseId())
                .orElseThrow(() -> new EntityNotFoundException("OnboardingCase", request.getOnboardingCaseId()));

        CandidateDocument document = CandidateDocument.builder()
                .onboardingCaseId(onboardingCase.getId())
                .documentType(request.getDocumentType())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .storagePath(request.getStoragePath())
                .mimeType(request.getMimeType())
                .verificationStatus("PENDING")
                .build();

        return documentRepository.save(document);
    }

    @Transactional
    public BgvCaseDto initiateBgv(UUID onboardingCaseId) {
        OnboardingCase onboardingCase = onboardingCaseRepository.findById(onboardingCaseId)
                .orElseThrow(() -> new EntityNotFoundException("OnboardingCase", onboardingCaseId));

        if (!"CONSENT_GRANTED".equalsIgnoreCase(onboardingCase.getStatus()) && !"DETAILS_SUBMITTED".equalsIgnoreCase(onboardingCase.getStatus())) {
            throw new BusinessValidationException("Candidate consent must be granted prior to initiating BGV", "ERR_BGV_CONSENT_REQUIRED");
        }

        BgvCase bgvCase = BgvCase.builder()
                .onboardingCaseId(onboardingCase.getId())
                .vendorName("ABCD_INTERNAL_REVIEW")
                .vendorReferenceId("BGV-" + System.currentTimeMillis())
                .status("IN_PROGRESS")
                .build();
        BgvCase savedCase = bgvCaseRepository.save(bgvCase);

        List<String> checkTypes = Arrays.asList("IDENTITY", "ADDRESS", "EDUCATION", "PREVIOUS_EMPLOYMENT", "CRIMINAL_RECORD");
        List<BgvCheckItem> checkItems = new ArrayList<>();
        for (String type : checkTypes) {
            BgvCheckItem item = BgvCheckItem.builder()
                    .bgvCaseId(savedCase.getId())
                    .checkType(type)
                    .status("PENDING")
                    .build();
            checkItems.add(bgvCheckItemRepository.save(item));
        }

        onboardingCase.setStatus("BGV_INITIATED");
        onboardingCaseRepository.save(onboardingCase);

        return mapToBgvDto(savedCase, checkItems);
    }

    @Transactional
    public BgvCaseDto reviewBgvCase(UUID bgvCaseId, BgvReviewRequest request) {
        BgvCase bgvCase = bgvCaseRepository.findById(bgvCaseId)
                .orElseThrow(() -> new EntityNotFoundException("BgvCase", bgvCaseId));

        OnboardingCase onboardingCase = onboardingCaseRepository.findById(bgvCase.getOnboardingCaseId())
                .orElseThrow(() -> new EntityNotFoundException("OnboardingCase", bgvCase.getOnboardingCaseId()));

        bgvCase.setStatus(request.getStatus().toUpperCase());
        bgvCase.setManualReviewerNotes(request.getReviewerNotes());
        bgvCase.setDecidedBy(request.getReviewerId() != null ? request.getReviewerId() : TenantContext.getUserId());
        bgvCase.setDecidedAt(Instant.now());
        BgvCase updatedBgv = bgvCaseRepository.save(bgvCase);

        List<BgvCheckItem> items = bgvCheckItemRepository.findByBgvCaseId(bgvCaseId);
        for (BgvCheckItem item : items) {
            item.setStatus(request.getStatus().toUpperCase());
            bgvCheckItemRepository.save(item);
        }

        if ("CLEAR".equalsIgnoreCase(request.getStatus())) {
            onboardingCase.setStatus("ONBOARDING_COMPLETED");
            onboardingCaseRepository.save(onboardingCase);

            // Publish OnboardingCompleted.v1 to Kafka via Transactional Outbox
            Map<String, Object> payload = new HashMap<>();
            payload.put("onboardingCaseId", onboardingCase.getId());
            payload.put("candidateName", onboardingCase.getCandidateName());
            payload.put("candidateEmail", onboardingCase.getCandidateEmail());
            payload.put("proposedJoiningDate", onboardingCase.getProposedJoiningDate().toString());
            payload.put("targetLegalEntityId", onboardingCase.getTargetLegalEntityId());
            payload.put("targetDepartmentId", onboardingCase.getTargetDepartmentId());
            payload.put("targetLocationId", onboardingCase.getTargetLocationId());
            payload.put("proposedDesignation", onboardingCase.getProposedDesignation());

            CanonicalEvent<Map<String, Object>> event = CanonicalEvent.of(
                    "OnboardingCompleted.v1",
                    "ONBOARDING_CASE",
                    onboardingCase.getId().toString(),
                    onboardingCase.getCompanyId(),
                    onboardingCase.getTargetLegalEntityId(),
                    "onboarding-bgv-service",
                    TenantContext.getCorrelationId(),
                    payload
            );

            outboxPublisherService.enqueueEvent(
                    KafkaTopicConstants.TOPIC_ONBOARDING_EVENTS,
                    "ONBOARDING_CASE",
                    onboardingCase.getId().toString(),
                    event
            );
        } else {
            onboardingCase.setStatus("BGV_" + request.getStatus().toUpperCase());
            onboardingCaseRepository.save(onboardingCase);
        }

        return mapToBgvDto(updatedBgv, items);
    }

    @Transactional(readOnly = true)
    public OnboardingCaseDto getCaseById(UUID id) {
        OnboardingCase onboardingCase = onboardingCaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OnboardingCase", id));
        return mapToDto(onboardingCase);
    }

    @Transactional(readOnly = true)
    public OnboardingCaseDto getCaseByToken(String token) {
        OnboardingCase onboardingCase = onboardingCaseRepository.findBySecureToken(token)
                .orElseThrow(() -> new EntityNotFoundException("OnboardingCase by token", token));
        return mapToDto(onboardingCase);
    }

    @Transactional(readOnly = true)
    public List<OnboardingCaseDto> listCases(UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return onboardingCaseRepository.findByCompanyId(effectiveCompanyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidateDocument> getDocuments(UUID onboardingCaseId) {
        return documentRepository.findByOnboardingCaseId(onboardingCaseId);
    }

    @Transactional(readOnly = true)
    public BgvCaseDto getBgvCase(UUID onboardingCaseId) {
        BgvCase bgvCase = bgvCaseRepository.findByOnboardingCaseId(onboardingCaseId)
                .orElseThrow(() -> new EntityNotFoundException("BgvCase for onboardingCaseId", onboardingCaseId));
        List<BgvCheckItem> items = bgvCheckItemRepository.findByBgvCaseId(bgvCase.getId());
        return mapToBgvDto(bgvCase, items);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    private OnboardingCaseDto mapToDto(OnboardingCase c) {
        return OnboardingCaseDto.builder()
                .id(c.getId())
                .companyId(c.getCompanyId())
                .candidateName(c.getCandidateName())
                .candidateEmail(c.getCandidateEmail())
                .phoneNumber(c.getPhoneNumber())
                .targetLegalEntityId(c.getTargetLegalEntityId())
                .targetDepartmentId(c.getTargetDepartmentId())
                .targetLocationId(c.getTargetLocationId())
                .proposedDesignation(c.getProposedDesignation())
                .proposedJoiningDate(c.getProposedJoiningDate())
                .secureToken(c.getSecureToken())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private BgvCaseDto mapToBgvDto(BgvCase bgv, List<BgvCheckItem> items) {
        List<BgvCaseDto.BgvCheckItemDto> itemDtos = items.stream()
                .map(i -> BgvCaseDto.BgvCheckItemDto.builder()
                        .id(i.getId())
                        .checkType(i.getCheckType())
                        .status(i.getStatus())
                        .discrepancyNotes(i.getDiscrepancyNotes())
                        .build())
                .collect(Collectors.toList());

        return BgvCaseDto.builder()
                .id(bgv.getId())
                .onboardingCaseId(bgv.getOnboardingCaseId())
                .vendorName(bgv.getVendorName())
                .vendorReferenceId(bgv.getVendorReferenceId())
                .status(bgv.getStatus())
                .manualReviewerNotes(bgv.getManualReviewerNotes())
                .decidedBy(bgv.getDecidedBy())
                .decidedAt(bgv.getDecidedAt())
                .checkItems(itemDtos)
                .build();
    }
}
