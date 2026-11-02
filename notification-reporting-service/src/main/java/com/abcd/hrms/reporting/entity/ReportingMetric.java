package com.abcd.hrms.reporting.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reporting_metrics")
public class ReportingMetric extends BaseAuditedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "metric_category", nullable = false, length = 50)
    private String metricCategory; // HEADCOUNT, PAYROLL, LEAVE, DIVERSITY

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "period", nullable = false, length = 30)
    private String period; // 2026-11, Q4-2026, 2026

    @Column(name = "metric_value", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal metricValue = BigDecimal.ZERO;

    @Column(name = "dimensions_json", columnDefinition = "TEXT")
    private String dimensionsJson;
}

