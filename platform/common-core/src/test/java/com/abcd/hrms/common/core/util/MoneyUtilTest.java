package com.abcd.hrms.common.core.util;

import com.abcd.hrms.common.core.exception.BusinessValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoneyUtil Invariant & Banker's Rounding Tests")
class MoneyUtilTest {

    @Test
    @DisplayName("Should enforce Banker's Rounding (HALF_EVEN) to eliminate statistical bias")
    void testBankersRounding() {
        // 2.5 rounds to 2 (even), 3.5 rounds to 4 (even)
        BigDecimal twoPointFive = new BigDecimal("2.50005");
        BigDecimal rounded = MoneyUtil.round(twoPointFive);
        assertNotNull(rounded);
        assertEquals(4, rounded.scale());

        // Test display rounding (2 decimal places)
        BigDecimal val1 = new BigDecimal("10.125"); // 2 is even -> 10.12
        BigDecimal val2 = new BigDecimal("10.135"); // 3 is odd -> 10.14
        assertEquals(new BigDecimal("10.12"), MoneyUtil.toDisplay(val1));
        assertEquals(new BigDecimal("10.14"), MoneyUtil.toDisplay(val2));
    }

    @Test
    @DisplayName("Should correctly calculate percentage with scale 4")
    void testPercentageOf() {
        BigDecimal amount = new BigDecimal("15000.0000");
        BigDecimal pfRate = new BigDecimal("12.0000"); // 12% PF

        BigDecimal result = MoneyUtil.percentageOf(amount, pfRate);
        assertEquals(new BigDecimal("1800.0000"), result);
    }

    @Test
    @DisplayName("Should prorate salary accurately based on payable days")
    void testProrate() {
        BigDecimal fullMonthlyBasic = new BigDecimal("60000.0000");
        BigDecimal payableDays = new BigDecimal("15.0000");
        BigDecimal totalDays = new BigDecimal("30.0000");

        BigDecimal prorated = MoneyUtil.prorate(fullMonthlyBasic, payableDays, totalDays);
        assertEquals(new BigDecimal("30000.0000"), prorated);
    }

    @Test
    @DisplayName("Should throw BusinessValidationException on division by zero")
    void testDivisionByZeroThrows() {
        BigDecimal numerator = new BigDecimal("1000.0000");
        assertThrows(BusinessValidationException.class, () -> MoneyUtil.divide(numerator, BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should safely add multiple amounts treating null as zero")
    void testSafeAdd() {
        BigDecimal sum = MoneyUtil.add(
                new BigDecimal("1000.5000"),
                null,
                new BigDecimal("2000.2500"),
                BigDecimal.ZERO
        );
        assertEquals(new BigDecimal("3000.7500"), sum);
    }
}
