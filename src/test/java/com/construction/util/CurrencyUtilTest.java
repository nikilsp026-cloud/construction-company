package com.construction.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurrencyUtilTest {

    @Test
    void formatINR_returnsDashForNull() {
        assertEquals("—", CurrencyUtil.formatINR(null));
    }

    @Test
    void formatINR_includesRupeeSymbolAndTwoDecimals() {
        // NOTE: digit grouping (Indian "42,50,000" vs Western "4,250,000")
        // depends on the JVM's locale data provider and isn't asserted here -
        // it was observed to vary by environment when writing this test.
        String formatted = CurrencyUtil.formatINR(new BigDecimal("4250000.00"));
        assertTrue(formatted.contains("₹"), "Expected a rupee symbol in: " + formatted);
        assertTrue(formatted.endsWith(".00"), "Expected 2 decimal places in: " + formatted);
    }

    @Test
    void formatBudgetRange_mapsKnownCodes() {
        assertEquals("Under ₹10,00,000", CurrencyUtil.formatBudgetRange("UNDER_10L"));
        assertEquals("Over ₹1,00,00,000", CurrencyUtil.formatBudgetRange("OVER_1CR"));
    }

    @Test
    void formatBudgetRange_returnsDashForBlank() {
        assertEquals("—", CurrencyUtil.formatBudgetRange(null));
        assertEquals("—", CurrencyUtil.formatBudgetRange(""));
    }

    @Test
    void formatBudgetRange_fallsBackToRawValueForUnrecognizedCode() {
        assertEquals("some-legacy-value", CurrencyUtil.formatBudgetRange("some-legacy-value"));
    }
}
