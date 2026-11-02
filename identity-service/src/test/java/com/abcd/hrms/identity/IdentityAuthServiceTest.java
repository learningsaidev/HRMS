package com.abcd.hrms.identity;

import com.abcd.hrms.common.security.jwt.JwtTokenProvider;
import com.abcd.hrms.identity.dto.AuthRequest;
import com.abcd.hrms.identity.dto.AuthResponse;
import com.abcd.hrms.identity.entity.Role;
import com.abcd.hrms.identity.entity.User;
import com.abcd.hrms.identity.repository.RoleRepository;
import com.abcd.hrms.identity.repository.UserRepository;
import com.abcd.hrms.identity.repository.UserSessionRepository;
import com.abcd.hrms.identity.service.IdentityAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdentityAuthService Authentication & User Tests")
class IdentityAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserSessionRepository userSessionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private IdentityAuthService authService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .name("ROLE_EMPLOYEE")
                .description("Employee Self Service")
                .build();
        testRole.setId(UUID.randomUUID());

        testUser = User.builder()
                .username("john.doe")
                .email("john.doe@abcd.corp")
                .passwordHash("hashed_password_123")
                .status("ACTIVE")
                .companyId(UUID.randomUUID())
                .legalEntityId(UUID.randomUUID())
                .roles(Collections.singleton(testRole))
                .build();
        testUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should successfully authenticate user with valid credentials")
    void testSuccessfulAuthentication() {
        AuthRequest request = AuthRequest.builder()
                .username("john.doe")
                .password("Password@123")
                .build();

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password@123", "hashed_password_123")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(), any(), any(), any(), any(), any())).thenReturn("mocked.jwt.token");

        AuthResponse response = authService.authenticate(request, "127.0.0.1", "Mozilla/5.0");

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getAccessToken());
        assertEquals("john.doe", response.getUsername());
        assertTrue(response.getRoles().contains("ROLE_EMPLOYEE"));
        verify(userSessionRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on password mismatch")
    void testFailedAuthentication() {
        AuthRequest request = AuthRequest.builder()
                .username("john.doe")
                .password("WrongPassword")
                .build();

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "hashed_password_123")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request, "127.0.0.1", "Mozilla/5.0"));
    }
}
