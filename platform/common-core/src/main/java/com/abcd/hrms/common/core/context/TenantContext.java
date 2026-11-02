package com.abcd.hrms.common.core.context;

import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Thread-local context holder for multi-tenancy, user identity, and correlation tracking.
 */
public final class TenantContext {

    private static final ThreadLocal<TenantData> CONTEXT = ThreadLocal.withInitial(() -> TenantData.builder().build());

    private TenantContext() {
    }

    public static TenantData get() {
        return CONTEXT.get();
    }

    public static void set(TenantData data) {
        CONTEXT.set(data != null ? data : TenantData.builder().build());
    }

    public static UUID getCompanyId() {
        return CONTEXT.get().getCompanyId();
    }

    public static void setCompanyId(UUID companyId) {
        CONTEXT.get().setCompanyId(companyId);
    }

    public static UUID getLegalEntityId() {
        return CONTEXT.get().getLegalEntityId();
    }

    public static void setLegalEntityId(UUID legalEntityId) {
        CONTEXT.get().setLegalEntityId(legalEntityId);
    }

    public static UUID getUserId() {
        return CONTEXT.get().getUserId();
    }

    public static void setUserId(UUID userId) {
        CONTEXT.get().setUserId(userId);
    }

    public static String getUsername() {
        return CONTEXT.get().getUsername();
    }

    public static void setUsername(String username) {
        CONTEXT.get().setUsername(username);
    }

    public static String getCorrelationId() {
        return CONTEXT.get().getCorrelationId();
    }

    public static void setCorrelationId(String correlationId) {
        CONTEXT.get().setCorrelationId(correlationId);
    }

    public static Set<String> getRoles() {
        return CONTEXT.get().getRoles();
    }

    public static void setRoles(Set<String> roles) {
        CONTEXT.get().setRoles(roles != null ? roles : Collections.emptySet());
    }

    public static void clear() {
        CONTEXT.remove();
    }

    @Data
    @Builder
    public static class TenantData {
        private UUID companyId;
        private UUID legalEntityId;
        private UUID userId;
        private String username;
        private String correlationId;
        @Builder.Default
        private Set<String> roles = Collections.emptySet();
    }
}
