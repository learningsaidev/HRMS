package com.abcd.hrms.common.core.entity;

import com.abcd.hrms.common.core.context.TenantContext;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Mapped superclass providing standard immutable audit tracking and optimistic locking.
 * Guarantees that every database entity has a UUID primary key, audit timestamps, and audit actor stamps.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseAuditedEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @PrePersist
    protected void onPrePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        String currentUser = TenantContext.getUsername() != null ? TenantContext.getUsername() : "SYSTEM";
        if (this.createdBy == null) {
            this.createdBy = currentUser;
        }
        if (this.updatedBy == null) {
            this.updatedBy = currentUser;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = TenantContext.getUsername() != null ? TenantContext.getUsername() : "SYSTEM";
    }
}
