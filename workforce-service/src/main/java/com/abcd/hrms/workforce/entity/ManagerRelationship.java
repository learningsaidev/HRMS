package com.abcd.hrms.workforce.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "manager_relationship")
public class ManagerRelationship extends BaseAuditedEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "manager_employee_id", nullable = false)
    private UUID managerEmployeeId;

    @Column(name = "relationship_type", nullable = false, length = 30)
    @Builder.Default
    private String relationshipType = "PRIMARY"; // PRIMARY, DOTTED_LINE

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
