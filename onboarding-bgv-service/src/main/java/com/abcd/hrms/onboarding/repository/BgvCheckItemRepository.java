package com.abcd.hrms.onboarding.repository;

import com.abcd.hrms.onboarding.entity.BgvCheckItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BgvCheckItemRepository extends JpaRepository<BgvCheckItem, UUID> {

    List<BgvCheckItem> findByBgvCaseId(UUID bgvCaseId);
}

