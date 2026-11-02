package com.abcd.hrms.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an attempt is made to mutate an approved and locked payroll run or period.
 */
public class PayrollLockedException extends BaseBusinessException {

    public PayrollLockedException(String payrollRunId) {
        super(String.format("Payroll run [%s] is LOCKED and strictly immutable. Mutations prohibited.", payrollRunId),
                "ERR_PAYROLL_LOCKED",
                HttpStatus.CONFLICT);
    }

    public PayrollLockedException(String periodName, String status) {
        super(String.format("Payroll period [%s] is currently in state [%s]. Modifications are frozen.", periodName, status),
                "ERR_PAYROLL_LOCKED",
                HttpStatus.CONFLICT);
    }
}
