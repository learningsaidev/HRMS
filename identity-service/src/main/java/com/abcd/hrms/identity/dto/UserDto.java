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
public class UserDto {

    private UUID id;

    private String username;

    private String email;

    private UUID employeeId;

    private UUID companyId;

    private UUID legalEntityId;

    private String status;

    private boolean mfaEnabled;

    private Set<String> roles;

    private Set<String> permissions;

    private Instant createdAt;

    private Instant updatedAt;
}
