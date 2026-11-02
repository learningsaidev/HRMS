package com.abcd.hrms.common.security.rbac;

/**
 * Standard enterprise role definitions for ABCD HRMS platform.
 * Adheres to strict separation of duties (e.g. Maker vs Checker vs Disburser).
 */
public final class SecurityRoles {

    private SecurityRoles() {
    }

    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_HR_ADMIN = "ROLE_HR_ADMIN";
    public static final String ROLE_PAYROLL_MAKER = "ROLE_PAYROLL_MAKER";
    public static final String ROLE_PAYROLL_CHECKER = "ROLE_PAYROLL_CHECKER";
    public static final String ROLE_FINANCE_DIRECTOR = "ROLE_FINANCE_DIRECTOR";
    public static final String ROLE_COMPLIANCE_OFFICER = "ROLE_COMPLIANCE_OFFICER";
    public static final String ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM_ADMIN";
}
