package com.construction.util;

/**
 * Lightweight, no-external-service spam check for public forms (Contact,
 * Quote request). Combines two classic signals:
 * <p>
 * 1. Honeypot: a form field real users never see or fill (hidden off-screen
 *    via CSS, not {@code type="hidden"}, since basic bots skip obviously
 *    hidden inputs but still auto-fill visually-hidden ones).
 * 2. Time-to-submit: bots typically submit within milliseconds of loading
 *    the page; a real person takes at least a couple of seconds.
 */
public final class SpamGuard {

    private SpamGuard() {
    }

    private static final long MIN_FILL_TIME_MS = 2000;

    public static boolean isLikelyBot(String honeypotValue, String formRenderedAtMillis) {
        if (honeypotValue != null && !honeypotValue.isBlank()) {
            return true;
        }
        try {
            long renderedAt = Long.parseLong(formRenderedAtMillis);
            return (System.currentTimeMillis() - renderedAt) < MIN_FILL_TIME_MS;
        } catch (Exception e) {
            // Missing/malformed timing field means the form wasn't rendered
            // normally by this server - treat it as suspicious too.
            return true;
        }
    }
}
