package com.abcd.hrms.common.core.entity;

import com.abcd.hrms.common.core.context.TenantContext;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.util.UUID;

/**
 * Base entity for all tenant-scoped domain entities.
 * Automatically binds company_id and legal_entity_id from TenantContext on persist.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class TenantAuditedEntity extends BaseAuditedEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "company_id", nullable = false, updatable = false)
    private UUID companyId;

    @Column(name = "legal_entity_id")
    private UUID legalEntityId;

    @Override
    @PrePersist
    protected void onPrePersist() {
        super.onPrePersist();
        if (this.companyId == null && TenantContext.getCompanyId() != null) {
            this.companyId = TenantContext.getCompanyId();
        }
        if (this.legalEntityId == null && TenantContext.getLegalEntityId() != null) {
            this.legalEntityId = TenantContext.getLegalEntityId();
        }
    }
}
