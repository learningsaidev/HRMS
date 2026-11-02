package com.abcd.hrms.onboarding.repository;

import com.abcd.hrms.onboarding.entity.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {

    Optional<ConsentRecord> findByOnboardingCaseId(UUID onboardingCaseId);
}

