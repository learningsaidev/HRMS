package com.abcd.hrms.identity.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.identity.dto.CreateUserRequest;
import com.abcd.hrms.identity.dto.UserDto;
import com.abcd.hrms.identity.service.IdentityAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User identities, roles, and credential administration")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final IdentityAuthService authService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_HR_ADMIN')")
    @Operation(summary = "Create a new platform user with assigned roles")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDto userDto = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(userDto));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Principal principal) {
        UserDto userDto = authService.getUserByUsername(principal.getName());
        return ResponseEntity.ok(ApiResponse.ok(userDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_HR_ADMIN')")
    @Operation(summary = "Get user details by UUID")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        UserDto userDto = authService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ok(userDto));
    }
}
