package com.abcd.hrms.taxbenefits.repository;

import com.abcd.hrms.taxbenefits.entity.TaxRegime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaxRegimeRepository extends JpaRepository<TaxRegime, UUID> {
    Optional<TaxRegime> findByRegimeCodeAndFinancialYear(String regimeCode, String financialYear);
    List<TaxRegime> findByFinancialYear(String financialYear);
    Optional<TaxRegime> findByFinancialYearAndIsDefaultTrue(String financialYear);
}

