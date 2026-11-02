package com.abcd.hrms.identity.service;

import com.abcd.hrms.common.core.exception.ConflictException;
import com.abcd.hrms.common.core.exception.EntityNotFoundException;
import com.abcd.hrms.common.security.jwt.JwtTokenProvider;
import com.abcd.hrms.identity.dto.AuthRequest;
import com.abcd.hrms.identity.dto.AuthResponse;
import com.abcd.hrms.identity.dto.CreateUserRequest;
import com.abcd.hrms.identity.dto.UserDto;
import com.abcd.hrms.identity.entity.Permission;
import com.abcd.hrms.identity.entity.Role;
import com.abcd.hrms.identity.entity.User;
import com.abcd.hrms.identity.entity.UserSession;
import com.abcd.hrms.identity.repository.RoleRepository;
import com.abcd.hrms.identity.repository.UserRepository;
import com.abcd.hrms.identity.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse authenticate(AuthRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BadCredentialsException("User account is " + user.getStatus());
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setStatus("LOCKED");
                user.setLockedUntil(Instant.now().plus(30, ChronoUnit.MINUTES));
                log.warn("Account locked due to 5 consecutive failed login attempts: {}", user.getUsername());
            }
            userRepository.save(user);
            throw new BadCredentialsException("Invalid username or password");
        }

        // Reset failed login attempts
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissionCodes = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        String accessToken = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getId(),
                user.getCompanyId(),
                user.getLegalEntityId(),
                roleNames,
                permissionCodes
        );

        String refreshToken = UUID.randomUUID().toString();
        UserSession session = UserSession.builder()
                .userId(user.getId())
                .refreshTokenHash(passwordEncoder.encode(refreshToken))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .isRevoked(false)
                .build();
        userSessionRepository.save(session);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(7200)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .employeeId(user.getEmployeeId())
                .companyId(user.getCompanyId())
                .legalEntityId(user.getLegalEntityId())
                .roles(roleNames)
                .permissions(permissionCodes)
                .issuedAt(Instant.now())
                .build();
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("User", "Username [" + request.getUsername() + "] is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User", "Email [" + request.getEmail() + "] is already registered");
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role", roleName));
            roles.add(role);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .employeeId(request.getEmployeeId())
                .companyId(request.getCompanyId())
                .legalEntityId(request.getLegalEntityId())
                .status("ACTIVE")
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToDto)
                .orElseThrow(() -> new EntityNotFoundException("User", username));
    }

    private UserDto mapToDto(User user) {
        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissionCodes = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .employeeId(user.getEmployeeId())
                .companyId(user.getCompanyId())
                .legalEntityId(user.getLegalEntityId())
                .status(user.getStatus())
                .mfaEnabled(user.isMfaEnabled())
                .roles(roleNames)
                .permissions(permissionCodes)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
