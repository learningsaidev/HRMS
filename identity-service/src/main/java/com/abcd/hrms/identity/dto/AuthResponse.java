package com.abcd.hrms.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresInSeconds;

    private UUID userId;

    private String username;

    private String email;

    private UUID employeeId;

    private UUID companyId;

    private UUID legalEntityId;

    private Set<String> roles;

    private Set<String> permissions;

    private Instant issuedAt;
}
