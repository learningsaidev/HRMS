package com.abcd.hrms.common.security.rbac;

/**
 * Fine-grained Attribute-Based and Action-Based Permissions.
 */
public final class Permissions {

    private Permissions() {
    }

    // Workforce Permissions
    public static final String WORKFORCE_READ = "workforce:read";
    public static final String WORKFORCE_WRITE = "workforce:write";
    public static final String WORKFORCE_DELETE = "workforce:delete";

    // Time & Leave Permissions
    public static final String TIME_PUNCH = "time:punch";
    public static final String TIME_TIMESHEET_SUBMIT = "time:timesheet:submit";
    public static final String TIME_TIMESHEET_APPROVE = "time:timesheet:approve";
    public static final String LEAVE_APPLY = "leave:apply";
    public static final String LEAVE_APPROVE = "leave:approve";
    public static final String LEAVE_ADMIN = "leave:admin";

    // Onboarding & BGV Permissions
    public static final String ONBOARDING_READ = "onboarding:read";
    public static final String ONBOARDING_WRITE = "onboarding:write";
    public static final String BGV_REVIEW = "bgv:review";

    // Payroll Permissions (Strict Separation of Duties)
    public static final String PAYROLL_SIMULATE = "payroll:simulate";
    public static final String PAYROLL_CALCULATE = "payroll:calculate";
    public static final String PAYROLL_APPROVE = "payroll:approve";
    public static final String PAYROLL_LOCK = "payroll:lock";
    public static final String PAYROLL_REVERT = "payroll:revert";
    public static final String PAYSLIP_READ_SELF = "payslip:read:self";
    public static final String PAYSLIP_READ_ALL = "payslip:read:all";

    // Finance & Payment Ledger Permissions
    public static final String PAYMENT_BATCH_CREATE = "payment:batch:create";
    public static final String PAYMENT_RELEASE = "payment:release";
    public static final String PAYMENT_REVERSAL = "payment:reversal";
    public static final String LEDGER_VIEW = "ledger:view";
    public static final String BANK_RECONCILE = "bank:reconcile";
}
