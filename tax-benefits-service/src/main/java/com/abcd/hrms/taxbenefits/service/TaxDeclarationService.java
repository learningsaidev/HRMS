package com.abcd.hrms.taxbenefits.service;

import com.abcd.hrms.common.core.context.TenantContext;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.core.util.MoneyUtil;
import com.abcd.hrms.common.messaging.KafkaTopicConstants;
import com.abcd.hrms.common.messaging.event.CanonicalEvent;
import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.taxbenefits.dto.TaxDeclarationDto;
import com.abcd.hrms.taxbenefits.entity.DeclarationItem;
import com.abcd.hrms.taxbenefits.entity.TaxDeclaration;
import com.abcd.hrms.taxbenefits.repository.DeclarationItemRepository;
import com.abcd.hrms.taxbenefits.repository.TaxDeclarationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxDeclarationService {

    private final TaxDeclarationRepository declarationRepository;
    private final DeclarationItemRepository itemRepository;
    private final OutboxPublisherService outboxPublisherService;

    @Transactional
    public TaxDeclarationDto.Response submitDeclaration(TaxDeclarationDto.SubmitRequest request) {
        UUID companyId = request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId();
        if (companyId == null) {
            companyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        TaxDeclaration declaration = declarationRepository
                .findByCompanyIdAndEmployeeIdAndFinancialYear(companyId, request.getEmployeeId(), request.getFinancialYear())
                .orElseGet(() -> TaxDeclaration.builder()
                        .companyId(request.getCompanyId() != null ? request.getCompanyId() : TenantContext.getCompanyId())
                        .employeeId(request.getEmployeeId())
                        .financialYear(request.getFinancialYear())
                        .build());

        if (declaration.getCompanyId() == null) {
            declaration.setCompanyId(companyId);
        }

        declaration.setRegimeCode(request.getRegimeCode() != null ? request.getRegimeCode().toUpperCase() : "NEW");
        declaration.setStatus("SUBMITTED");

        // Save declaration first to get ID if new
        TaxDeclaration savedDecl = declarationRepository.save(declaration);

        // Delete existing items for fresh declaration
        itemRepository.deleteByDeclarationId(savedDecl.getId());

        BigDecimal totalDeclared = BigDecimal.ZERO;
        List<DeclarationItem> items = new ArrayList<>();

        if (request.getItems() != null) {
            for (TaxDeclarationDto.ItemSubmitRequest itemReq : request.getItems()) {
                BigDecimal amount = MoneyUtil.round(itemReq.getDeclaredAmount());
                totalDeclared = MoneyUtil.add(totalDeclared, amount);

                DeclarationItem item = DeclarationItem.builder()
                        .declarationId(savedDecl.getId())
                        .sectionCode(itemReq.getSectionCode().toUpperCase())
                        .description(itemReq.getDescription())
                        .declaredAmount(amount)
                        .verifiedAmount(BigDecimal.ZERO)
                        .proofStoragePath(itemReq.getProofStoragePath())
                        .verificationStatus("PENDING")
                        .build();

                items.add(itemRepository.save(item));
            }
        }

        savedDecl.setTotalDeclaredDeductions(totalDeclared);
        TaxDeclaration updated = declarationRepository.save(savedDecl);

        // Publish TaxDeclarationSubmitted.v1
        Map<String, Object> payload = new HashMap<>();
        payload.put("declarationId", updated.getId());
        payload.put("employeeId", updated.getEmployeeId());
        payload.put("financialYear", updated.getFinancialYear());
        payload.put("regimeCode", updated.getRegimeCode());
        payload.put("totalDeclaredDeductions", updated.getTotalDeclaredDeductions().toString());

        CanonicalEvent<Map<String, Object>> event = CanonicalEvent.of(
                "TaxDeclarationSubmitted.v1",
                "TAX_DECLARATION",
                updated.getId().toString(),
                updated.getCompanyId(),
                null,
                "tax-benefits-service",
                TenantContext.getCorrelationId(),
                payload
        );

        outboxPublisherService.enqueueEvent(
                KafkaTopicConstants.TOPIC_PAYROLL_EVENTS,
                "TAX_DECLARATION",
                updated.getId().toString(),
                event
        );

        return mapToDto(updated, items);
    }

    @Transactional
    public TaxDeclarationDto.Response verifyItem(UUID declarationId, TaxDeclarationDto.VerifyItemRequest request) {
        TaxDeclaration declaration = declarationRepository.findById(declarationId)
                .orElseThrow(() -> new EntityNotFoundException("TaxDeclaration", declarationId));

        DeclarationItem item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("DeclarationItem", request.getItemId()));

        item.setVerificationStatus(request.getStatus().toUpperCase());
        if ("APPROVED".equalsIgnoreCase(request.getStatus())) {
            BigDecimal verifiedAmt = request.getVerifiedAmount() != null ?
                    MoneyUtil.round(request.getVerifiedAmount()) : item.getDeclaredAmount();
            item.setVerifiedAmount(verifiedAmt);
            item.setRejectionReason(null);
        } else {
            item.setVerifiedAmount(BigDecimal.ZERO);
            item.setRejectionReason(request.getRejectionReason());
        }
        itemRepository.save(item);

        // Recalculate total verified deductions
        List<DeclarationItem> allItems = itemRepository.findByDeclarationId(declarationId);
        BigDecimal totalVerified = allItems.stream()
                .filter(i -> "APPROVED".equalsIgnoreCase(i.getVerificationStatus()))
                .map(DeclarationItem::getVerifiedAmount)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        declaration.setTotalVerifiedDeductions(totalVerified);
        declaration.setStatus("VERIFIED");
        TaxDeclaration saved = declarationRepository.save(declaration);

        return mapToDto(saved, allItems);
    }

    @Transactional(readOnly = true)
    public TaxDeclarationDto.Response getDeclaration(UUID employeeId, String financialYear, UUID companyId) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }

        TaxDeclaration decl = declarationRepository
                .findByCompanyIdAndEmployeeIdAndFinancialYear(effectiveCompanyId, employeeId, financialYear)
                .orElseThrow(() -> new EntityNotFoundException("TaxDeclaration for employee " + employeeId, financialYear));

        List<DeclarationItem> items = itemRepository.findByDeclarationId(decl.getId());
        return mapToDto(decl, items);
    }

    @Transactional(readOnly = true)
    public List<TaxDeclarationDto.Response> listDeclarations(UUID companyId, String financialYear) {
        UUID effectiveCompanyId = companyId != null ? companyId : TenantContext.getCompanyId();
        if (effectiveCompanyId == null) {
            effectiveCompanyId = UUID.fromString("d0000000-0000-0000-0000-000000000001");
        }
        return declarationRepository.findByCompanyIdAndFinancialYear(effectiveCompanyId, financialYear).stream()
                .map(d -> mapToDto(d, itemRepository.findByDeclarationId(d.getId())))
                .collect(Collectors.toList());
    }

    private TaxDeclarationDto.Response mapToDto(TaxDeclaration d, List<DeclarationItem> items) {
        List<TaxDeclarationDto.ItemDto> itemDtos = items.stream()
                .map(i -> TaxDeclarationDto.ItemDto.builder()
                        .id(i.getId())
                        .sectionCode(i.getSectionCode())
                        .description(i.getDescription())
                        .declaredAmount(i.getDeclaredAmount())
                        .verifiedAmount(i.getVerifiedAmount())
                        .proofStoragePath(i.getProofStoragePath())
                        .verificationStatus(i.getVerificationStatus())
                        .rejectionReason(i.getRejectionReason())
                        .build())
                .collect(Collectors.toList());

        return TaxDeclarationDto.Response.builder()
                .id(d.getId())
                .companyId(d.getCompanyId())
                .employeeId(d.getEmployeeId())
                .financialYear(d.getFinancialYear())
                .regimeCode(d.getRegimeCode())
                .status(d.getStatus())
                .totalDeclaredDeductions(d.getTotalDeclaredDeductions())
                .totalVerifiedDeductions(d.getTotalVerifiedDeductions())
                .createdAt(d.getCreatedAt())
                .items(itemDtos)
                .build();
    }
}
