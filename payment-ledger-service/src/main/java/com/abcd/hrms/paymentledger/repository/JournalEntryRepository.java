package com.abcd.hrms.paymentledger.repository;

import com.abcd.hrms.paymentledger.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    Optional<JournalEntry> findByCompanyIdAndEntryNumber(UUID companyId, String entryNumber);
    List<JournalEntry> findByCompanyIdOrderByEntryDateDesc(UUID companyId);
    List<JournalEntry> findByLegalEntityIdOrderByEntryDateDesc(UUID legalEntityId);
    Optional<JournalEntry> findBySourceModuleAndSourceReferenceId(String sourceModule, String sourceReferenceId);
}
