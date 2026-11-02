package com.abcd.hrms.onboarding.repository;

import com.abcd.hrms.onboarding.entity.BgvCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BgvCaseRepository extends JpaRepository<BgvCase, UUID> {

    Optional<BgvCase> findByOnboardingCaseId(UUID onboardingCaseId);

    List<BgvCase> findByStatus(String status);
}

