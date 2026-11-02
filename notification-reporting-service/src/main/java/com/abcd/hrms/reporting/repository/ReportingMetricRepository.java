package com.abcd.hrms.reporting.repository;

import com.abcd.hrms.reporting.entity.ReportingMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportingMetricRepository extends JpaRepository<ReportingMetric, UUID> {
    List<ReportingMetric> findByCompanyIdAndMetricCategory(UUID companyId, String metricCategory);
    List<ReportingMetric> findByCompanyIdAndPeriod(UUID companyId, String period);
}

