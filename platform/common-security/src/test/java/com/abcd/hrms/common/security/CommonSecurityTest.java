package com.abcd.hrms.common.security;

import com.abcd.hrms.common.security.encryption.FieldEncryptionService;
import com.abcd.hrms.common.security.jwt.JwtTokenProvider;
import com.abcd.hrms.common.security.masking.DataMaskingUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Common Security Utilities & Encryption Tests")
class CommonSecurityTest {

    private FieldEncryptionService encryptionService;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        encryptionService = new FieldEncryptionService("ABCD-ENTERPRISE-SECRET-KEY-32B!");
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "ABCD-ENTERPRISE-SUPER-SECRET-JWT-SIGNING-KEY-FOR-ABCD-HRMS-PLATFORM-2026-MUST-BE-64-BYTES!");
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationMinutes", 60L);
        ReflectionTestUtils.setField(jwtTokenProvider, "issuer", "https://auth.abcd-hrms.corp");
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Should successfully encrypt and decrypt sensitive fields with AES-256-GCM")
    void testEncryptionDecryption() {
        String sensitivePan = "ABCDE1234F";
        String encrypted = encryptionService.encrypt(sensitivePan);

        assertNotNull(encrypted);
        assertNotEquals(sensitivePan, encrypted);

        String decrypted = encryptionService.decrypt(encrypted);
        assertEquals(sensitivePan, decrypted);
    }

    @Test
    @DisplayName("Should correctly mask bank accounts and national IDs")
    void testMasking() {
        assertEquals("XXXXXX5678", DataMaskingUtil.maskBankAccount("1234565678"));
        assertEquals("XXXXX1234F", DataMaskingUtil.maskNationalId("ABCDE1234F"));
        assertEquals("j***e@abcd.com", DataMaskingUtil.maskEmail("john.doe@abcd.com"));
    }

    @Test
    @DisplayName("Should issue and validate enterprise JWT with role claims")
    void testJwtTokenGenerationAndValidation() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID legalEntityId = UUID.randomUUID();
        List<String> roles = Collections.singletonList("ROLE_PAYROLL_MAKER");
        List<String> permissions = Collections.singletonList("payroll:simulate");

        String token = jwtTokenProvider.generateToken("payroll.specialist", userId, companyId, legalEntityId, roles, permissions);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));

        Claims claims = jwtTokenProvider.parseAndValidateClaims(token);
        assertEquals("payroll.specialist", claims.getSubject());
        assertEquals(userId.toString(), claims.get("userId"));
        assertEquals(companyId.toString(), claims.get("companyId"));
    }
}
