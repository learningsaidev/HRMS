package com.abcd.hrms.onboarding.repository;

import com.abcd.hrms.onboarding.entity.OnboardingCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingCaseRepository extends JpaRepository<OnboardingCase, UUID> {

    Optional<OnboardingCase> findBySecureToken(String secureToken);

    List<OnboardingCase> findByCompanyId(UUID companyId);

    List<OnboardingCase> findByCompanyIdAndStatus(UUID companyId, String status);
}
