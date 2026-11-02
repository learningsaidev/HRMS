package com.abcd.hrms.onboarding.repository;

import com.abcd.hrms.onboarding.entity.CandidateDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateDocumentRepository extends JpaRepository<CandidateDocument, UUID> {

    List<CandidateDocument> findByOnboardingCaseId(UUID onboardingCaseId);
}
