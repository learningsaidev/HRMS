package com.abcd.hrms.common.security.masking;

/**
 * Enterprise PII and banking data masking utility.
 * Masks data consistently for display and logging without revealing sensitive values.
 */
public final class DataMaskingUtil {

    private DataMaskingUtil() {
    }

    /**
     * Masks bank account numbers: retains last 4 digits (e.g. "XXXXXXXX1234").
     */
    public static String maskBankAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        int len = accountNumber.length();
        String lastFour = accountNumber.substring(len - 4);
        return "X".repeat(len - 4) + lastFour;
    }

    /**
     * Masks tax/national IDs (e.g. PAN or SSN): formats as "XXXXX1234X" or "***-**-1234".
     */
    public static String maskNationalId(String nationalId) {
        if (nationalId == null || nationalId.length() <= 4) {
            return "****";
        }
        int len = nationalId.length();
        if (len == 10) { // Standard Indian PAN card format (ABCDE1234F -> XXXXX1234X)
            return "XXXXX" + nationalId.substring(5, 9) + nationalId.substring(9);
        }
        return "X".repeat(len - 4) + nationalId.substring(len - 4);
    }

    /**
     * Masks email addresses: "john.doe@abcd.com" -> "j***e@abcd.com".
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****";
        }
        int atIndex = email.indexOf("@");
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (username.length() <= 2) {
            return "*".repeat(username.length()) + domain;
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
    }
}
