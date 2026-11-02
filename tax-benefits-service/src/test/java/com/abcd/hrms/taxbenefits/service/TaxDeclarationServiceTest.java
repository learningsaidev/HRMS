package com.abcd.hrms.taxbenefits.service;

import com.abcd.hrms.common.messaging.outbox.OutboxPublisherService;
import com.abcd.hrms.taxbenefits.dto.TaxDeclarationDto;
import com.abcd.hrms.taxbenefits.entity.DeclarationItem;
import com.abcd.hrms.taxbenefits.entity.TaxDeclaration;
import com.abcd.hrms.taxbenefits.repository.DeclarationItemRepository;
import com.abcd.hrms.taxbenefits.repository.TaxDeclarationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaxDeclarationService Declarations & Proofs Tests")
class TaxDeclarationServiceTest {

    @Mock
    private TaxDeclarationRepository declarationRepository;
    @Mock
    private DeclarationItemRepository itemRepository;
    @Mock
    private OutboxPublisherService outboxPublisherService;

    @InjectMocks
    private TaxDeclarationService declarationService;

    private UUID employeeId;
    private UUID companyId;
    private UUID declarationId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        declarationId = UUID.randomUUID();
    }

    @Test
    void testSubmitDeclaration() {
        TaxDeclarationDto.SubmitRequest req = new TaxDeclarationDto.SubmitRequest();
        req.setCompanyId(companyId);
        req.setEmployeeId(employeeId);
        req.setFinancialYear("2026-2027");
        req.setRegimeCode("OLD");

        TaxDeclarationDto.ItemSubmitRequest item1 = new TaxDeclarationDto.ItemSubmitRequest();
        item1.setSectionCode("80C");
        item1.setDeclaredAmount(new BigDecimal("150000.0000"));
        item1.setDescription("PPF & ELSS");

        TaxDeclarationDto.ItemSubmitRequest item2 = new TaxDeclarationDto.ItemSubmitRequest();
        item2.setSectionCode("80D");
        item2.setDeclaredAmount(new BigDecimal("25000.0000"));
        item2.setDescription("Health Insurance");

        req.setItems(Arrays.asList(item1, item2));

        when(declarationRepository.findByCompanyIdAndEmployeeIdAndFinancialYear(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(declarationRepository.save(any(TaxDeclaration.class))).thenAnswer(i -> {
            TaxDeclaration d = i.getArgument(0);
            if (d.getId() == null) d.setId(declarationId);
            return d;
        });
        when(itemRepository.save(any(DeclarationItem.class))).thenAnswer(i -> {
            DeclarationItem item = i.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });

        TaxDeclarationDto.Response response = declarationService.submitDeclaration(req);
        assertNotNull(response);
        assertEquals("OLD", response.getRegimeCode());
        assertEquals("SUBMITTED", response.getStatus());
        assertEquals(0, new BigDecimal("175000.0000").compareTo(response.getTotalDeclaredDeductions()));
        assertEquals(2, response.getItems().size());

        verify(outboxPublisherService, times(1)).enqueueEvent(any(), any(), any(), any());
    }

    @Test
    void testVerifyItemApproval() {
        UUID itemId = UUID.randomUUID();
        TaxDeclaration declaration = TaxDeclaration.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .financialYear("2026-2027")
                .regimeCode("OLD")
                .totalDeclaredDeductions(new BigDecimal("150000.0000"))
                .status("SUBMITTED")
                .build();
        declaration.setId(declarationId);

        DeclarationItem item = DeclarationItem.builder()
                .declarationId(declarationId)
                .sectionCode("80C")
                .declaredAmount(new BigDecimal("150000.0000"))
                .verificationStatus("PENDING")
                .build();
        item.setId(itemId);

        when(declarationRepository.findById(declarationId)).thenReturn(Optional.of(declaration));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.findByDeclarationId(declarationId)).thenReturn(Collections.singletonList(item));
        when(declarationRepository.save(any(TaxDeclaration.class))).thenAnswer(i -> i.getArgument(0));

        TaxDeclarationDto.VerifyItemRequest verifyReq = new TaxDeclarationDto.VerifyItemRequest();
        verifyReq.setItemId(itemId);
        verifyReq.setStatus("APPROVED");
        verifyReq.setVerifiedAmount(new BigDecimal("150000.0000"));

        TaxDeclarationDto.Response verified = declarationService.verifyItem(declarationId, verifyReq);
        assertEquals("VERIFIED", verified.getStatus());
        assertEquals(0, new BigDecimal("150000.0000").compareTo(verified.getTotalVerifiedDeductions()));
    }
}

