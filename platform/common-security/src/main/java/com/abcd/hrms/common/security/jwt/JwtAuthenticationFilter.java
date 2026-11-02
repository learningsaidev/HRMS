package com.abcd.hrms.common.security.jwt;

import com.abcd.hrms.common.core.context.TenantContext;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Filter intercepting incoming HTTP requests to validate JWT Bearer tokens and initialize TenantContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        TenantContext.setCorrelationId(correlationId);
        response.setHeader("X-Correlation-Id", correlationId);

        try {
            String jwt = resolveToken(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Claims claims = tokenProvider.parseAndValidateClaims(jwt);
                String username = claims.getSubject();
                String userIdStr = claims.get("userId", String.class);
                String companyIdStr = claims.get("companyId", String.class);
                String legalEntityIdStr = claims.get("legalEntityId", String.class);

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                @SuppressWarnings("unchecked")
                List<String> permissions = claims.get("permissions", List.class);

                Set<String> allAuthorities = new HashSet<>();
                if (roles != null) allAuthorities.addAll(roles);
                if (permissions != null) allAuthorities.addAll(permissions);

                UUID userId = StringUtils.hasText(userIdStr) ? UUID.fromString(userIdStr) : null;
                UUID companyId = StringUtils.hasText(companyIdStr) ? UUID.fromString(companyIdStr) : null;
                UUID legalEntityId = StringUtils.hasText(legalEntityIdStr) ? UUID.fromString(legalEntityIdStr) : null;

                // Populate TenantContext
                TenantContext.TenantData tenantData = TenantContext.TenantData.builder()
                        .userId(userId)
                        .username(username)
                        .companyId(companyId)
                        .legalEntityId(legalEntityId)
                        .correlationId(correlationId)
                        .roles(roles != null ? new HashSet<>(roles) : Collections.emptySet())
                        .build();
                TenantContext.set(tenantData);

                // Populate Spring Security Context
                List<SimpleGrantedAuthority> grantedAuthorities = allAuthorities.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
