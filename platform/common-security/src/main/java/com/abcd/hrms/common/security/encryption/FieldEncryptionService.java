package com.abcd.hrms.common.security.encryption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Enterprise-grade Field-Level Encryption using AES-256-GCM.
 * Used for securing sensitive PII, national tax IDs, and bank account numbers at rest.
 */
@Slf4j
@Service
public class FieldEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    public FieldEncryptionService(@Value("${abcd.security.encryption-key:ABCD-ENTERPRISE-SECRET-KEY-32B!}") String secret) {
        byte[] keyBytes = new byte[32];
        byte[] sourceBytes = secret.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(sourceBytes, 0, keyBytes, 0, Math.min(sourceBytes.length, 32));
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.secureRandom = new SecureRandom();
    }

    /**
     * Encrypts plaintext using AES-256-GCM with a randomized IV.
     * Output format: Base64(IV + Ciphertext + Tag).
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.trim().isEmpty()) {
            return plaintext;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Failed to encrypt sensitive field", e);
            throw new IllegalStateException("Encryption error occurred", e);
        }
    }

    /**
     * Decrypts Base64(IV + Ciphertext + Tag) ciphertext using AES-256-GCM.
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.trim().isEmpty()) {
            return encryptedText;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt sensitive field", e);
            throw new IllegalStateException("Decryption error occurred", e);
        }
    }
}
