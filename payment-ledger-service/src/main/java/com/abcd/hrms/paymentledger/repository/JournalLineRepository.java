package com.abcd.hrms.paymentledger.repository;

import com.abcd.hrms.paymentledger.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {
    List<JournalLine> findByJournalEntryId(UUID journalEntryId);
    void deleteByJournalEntryId(UUID journalEntryId);
}
