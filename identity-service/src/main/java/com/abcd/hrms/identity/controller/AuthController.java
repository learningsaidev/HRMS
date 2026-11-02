package com.abcd.hrms.identity.controller;

import com.abcd.hrms.common.core.dto.ApiResponse;
import com.abcd.hrms.identity.dto.AuthRequest;
import com.abcd.hrms.identity.dto.AuthResponse;
import com.abcd.hrms.identity.service.IdentityAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Sessions", description = "OAuth2 / OIDC token issuance and session operations")
public class AuthController {

    private final IdentityAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT Bearer tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest servletRequest) {

        String ipAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");

        AuthResponse response = authService.authenticate(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
