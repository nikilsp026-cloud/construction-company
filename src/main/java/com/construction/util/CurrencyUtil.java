package com.construction.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Central place for all Indian Rupee (INR) formatting in the application.
 * <p>
 * Using a single utility (rather than sprinkling "'$' + amount" or ad-hoc
 * formatting across templates) keeps currency display consistent everywhere:
 * product/project prices, quote budgets, dashboard values, and reports.
 */
public final class CurrencyUtil {

    private CurrencyUtil() {
    }

    private static final Locale INDIA = new Locale("en", "IN");

    /**
     * Formats a BigDecimal as a Rupee amount with Indian digit grouping,
     * e.g. 4250000.00 -&gt; "₹42,50,000.00". Returns "—" for null.
     */
    public static String formatINR(BigDecimal amount) {
        if (amount == null) {
            return "—";
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(INDIA);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(amount);
    }

    /**
     * Same as {@link #formatINR(BigDecimal)} but without decimal places -
     * useful for dashboard summary cards and large aggregate totals.
     */
    public static String formatINRWhole(BigDecimal amount) {
        if (amount == null) {
            return "—";
        }
        NumberFormat format = NumberFormat.getCurrencyInstance(INDIA);
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }

    /**
     * Maps a quote request's stored budget-range code (e.g. "UNDER_10L", as
     * submitted by the &lt;select&gt; on the public quote form) to its
     * human-readable Rupee range. Falls back to the raw stored value for any
     * unrecognized code, so pre-existing free-text data still displays.
     */
    public static String formatBudgetRange(String code) {
        if (code == null || code.isBlank()) {
            return "—";
        }
        return switch (code) {
            case "UNDER_10L" -> "Under ₹10,00,000";
            case "10L_25L"   -> "₹10,00,000 – ₹25,00,000";
            case "25L_50L"   -> "₹25,00,000 – ₹50,00,000";
            case "50L_1CR"   -> "₹50,00,000 – ₹1,00,00,000";
            case "OVER_1CR"  -> "Over ₹1,00,00,000";
            default -> code;
        };
    }
}
