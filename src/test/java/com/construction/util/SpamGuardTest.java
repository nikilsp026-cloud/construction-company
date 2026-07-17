package com.construction.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpamGuardTest {

    @Test
    void isLikelyBot_trueWhenHoneypotFilled() {
        String fiveSecondsAgo = String.valueOf(System.currentTimeMillis() - 5000);
        assertTrue(SpamGuard.isLikelyBot("http://spam.example", fiveSecondsAgo));
    }

    @Test
    void isLikelyBot_trueWhenSubmittedTooFast() {
        String justNow = String.valueOf(System.currentTimeMillis());
        assertTrue(SpamGuard.isLikelyBot("", justNow));
    }

    @Test
    void isLikelyBot_falseForRealisticHumanSubmission() {
        String fiveSecondsAgo = String.valueOf(System.currentTimeMillis() - 5000);
        assertFalse(SpamGuard.isLikelyBot("", fiveSecondsAgo));
        assertFalse(SpamGuard.isLikelyBot(null, fiveSecondsAgo));
    }

    @Test
    void isLikelyBot_trueWhenTimingFieldMissingOrMalformed() {
        assertTrue(SpamGuard.isLikelyBot(null, null));
        assertTrue(SpamGuard.isLikelyBot(null, "not-a-number"));
    }
}
