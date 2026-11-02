package com.abcd.hrms.identity.entity;

import com.abcd.hrms.common.core.entity.BaseAuditedEntity;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_sessions")
public class UserSession extends BaseAuditedEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "refresh_token_hash", nullable = false, unique = true, length = 255)
    private String refreshTokenHash;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private boolean isRevoked = false;
}
