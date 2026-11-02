package com.abcd.hrms.common.core.util;

import com.abcd.hrms.common.core.exception.BusinessValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;

/**
 * Strict enterprise financial and monetary calculation utilities.
 *
 * Invariants:
 * 1. Floating-point types (float, double) are strictly banned.
 * 2. Default calculation scale is 4 decimal places (matching PostgreSQL NUMERIC(18, 4)).
 * 3. Default rounding mode is RoundingMode.HALF_EVEN (Banker's Rounding) to eliminate statistical bias.
 * 4. Display/disbursement scale is 2 decimal places.
 */
public final class MoneyUtil {

    public static final int CALCULATION_SCALE = 4;
    public static final int DISPLAY_SCALE = 2;
    public static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public static final BigDecimal ZERO = round(BigDecimal.ZERO);
    public static final BigDecimal ONE_HUNDRED = round(BigDecimal.valueOf(100));

    private MoneyUtil() {
        // Private constructor for utility class
    }

    /**
     * Converts a BigDecimal to the standard calculation scale of 4 with HALF_EVEN rounding.
     */
    public static BigDecimal round(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(CALCULATION_SCALE, DEFAULT_ROUNDING_MODE);
        }
        return value.setScale(CALCULATION_SCALE, DEFAULT_ROUNDING_MODE);
    }

    public static BigDecimal scale(BigDecimal value) {
        return round(value);
    }

    /**
     * Converts a BigDecimal to display/disbursement scale of 2 with HALF_EVEN rounding.
     */
    public static BigDecimal toDisplay(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(DISPLAY_SCALE, DEFAULT_ROUNDING_MODE);
        }
        return value.setScale(DISPLAY_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Parses a string safely to standard scale BigDecimal.
     */
    public static BigDecimal of(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ZERO;
        }
        return round(new BigDecimal(value.trim()));
    }

    /**
     * Converts long/int to standard scale BigDecimal.
     */
    public static BigDecimal of(long value) {
        return round(BigDecimal.valueOf(value));
    }

    /**
     * Sums an array of BigDecimals safely, treating nulls as zero.
     */
    public static BigDecimal add(BigDecimal... amounts) {
        if (amounts == null || amounts.length == 0) {
            return ZERO;
        }
        return round(Arrays.stream(amounts)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    /**
     * Subtracts subtrahend from minuend (minuend - subtrahend).
     */
    public static BigDecimal subtract(BigDecimal minuend, BigDecimal subtrahend) {
        BigDecimal m = minuend != null ? minuend : BigDecimal.ZERO;
        BigDecimal s = subtrahend != null ? subtrahend : BigDecimal.ZERO;
        return round(m.subtract(s));
    }

    /**
     * Multiplies two BigDecimals.
     */
    public static BigDecimal multiply(BigDecimal multiplicand, BigDecimal multiplier) {
        if (multiplicand == null || multiplier == null) {
            return ZERO;
        }
        return round(multiplicand.multiply(multiplier));
    }

    /**
     * Safely divides numerator by denominator with HALF_EVEN rounding.
     * Throws BusinessValidationException if denominator is zero.
     */
    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        return divide(numerator, denominator, CALCULATION_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * Divides with custom scale and rounding mode.
     */
    public static BigDecimal divide(BigDecimal numerator, BigDecimal denominator, int scale, RoundingMode roundingMode) {
        if (numerator == null) {
            return BigDecimal.ZERO.setScale(scale, roundingMode);
        }
        if (denominator == null || isZero(denominator)) {
            throw new BusinessValidationException("Division by zero in financial calculation is prohibited", "ERR_DIV_BY_ZERO");
        }
        return numerator.divide(denominator, scale, roundingMode);
    }

    /**
     * Computes percentage: (amount * ratePercent) / 100
     */
    public static BigDecimal percentageOf(BigDecimal amount, BigDecimal ratePercent) {
        if (amount == null || ratePercent == null || isZero(amount) || isZero(ratePercent)) {
            return ZERO;
        }
        return divide(multiply(amount, ratePercent), ONE_HUNDRED);
    }

    /**
     * Prorates an amount based on days worked vs total days: (amount * payableDays) / totalPeriodDays
     */
    public static BigDecimal prorate(BigDecimal fullAmount, BigDecimal payableDays, BigDecimal totalPeriodDays) {
        if (fullAmount == null || payableDays == null || isZero(payableDays)) {
            return ZERO;
        }
        if (totalPeriodDays == null || isZero(totalPeriodDays)) {
            throw new BusinessValidationException("Total period days cannot be zero for proration", "ERR_INVALID_PRORATION_DAYS");
        }
        if (payableDays.compareTo(totalPeriodDays) >= 0) {
            return round(fullAmount);
        }
        return divide(multiply(fullAmount, payableDays), totalPeriodDays);
    }

    public static boolean isZero(BigDecimal amount) {
        if (amount == null) {
            return true;
        }
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNegative(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public static boolean isEqual(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.compareTo(b) == 0;
    }

    public static boolean equals(BigDecimal a, BigDecimal b) {
        return isEqual(a, b);
    }

    public static boolean isGreaterThan(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        return a.compareTo(b) > 0;
    }

    public static boolean isLessThan(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) return false;
        return a.compareTo(b) < 0;
    }
}
