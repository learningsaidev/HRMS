package com.abcd.hrms.common.core.exception;

import org.springframework.http.HttpStatus;
import java.math.BigDecimal;

/**
 * Thrown when double-entry general ledger debit and credit totals do not balance to zero.
 * Absolute zero-drift financial invariant violation.
 */
public class LedgerImbalanceException extends BaseBusinessException {

    public LedgerImbalanceException(BigDecimal totalDebit, BigDecimal totalCredit) {
        super(String.format("General ledger imbalance detected! Total Debit [%s] != Total Credit [%s]. Imbalance delta [%s].",
                totalDebit, totalCredit, totalDebit.subtract(totalCredit).abs()),
                "ERR_LEDGER_IMBALANCE",
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public LedgerImbalanceException(String message) {
        super(message, "ERR_LEDGER_IMBALANCE", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
