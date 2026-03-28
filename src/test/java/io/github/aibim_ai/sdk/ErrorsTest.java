package io.github.aibim_ai.sdk;

import io.github.aibim_ai.sdk.exceptions.AibimAuthException;
import io.github.aibim_ai.sdk.exceptions.AibimBlockedException;
import io.github.aibim_ai.sdk.exceptions.AibimException;
import io.github.aibim_ai.sdk.exceptions.AibimRateLimitException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorsTest {

    // --- AibimException ---

    @Test
    void aibimException_hasStatusCodeAndMessage() {
        var ex = new AibimException(500, "internal server error");
        assertEquals(500, ex.getStatusCode());
        assertEquals("internal server error", ex.getMessage());
    }

    @Test
    void aibimException_zeroStatusCodeForNonHttp() {
        var ex = new AibimException(0, "connection refused");
        assertEquals(0, ex.getStatusCode());
    }

    @Test
    void aibimException_withCause() {
        var cause = new RuntimeException("underlying error");
        var ex = new AibimException("wrapped error", cause);
        assertEquals("wrapped error", ex.getMessage());
        assertSame(cause, ex.getCause());
        assertEquals(0, ex.getStatusCode());
    }

    @Test
    void aibimException_isCheckedException() {
        assertInstanceOf(Exception.class, new AibimException(500, "test"));
    }

    // --- AibimBlockedException ---

    @Test
    void blockedException_hasAllFields() {
        var rules = List.of("INJECT_001", "JAILBREAK_003");
        var ex = new AibimBlockedException("blocked", 0.95, rules, "corr-123");

        assertEquals("blocked", ex.getMessage());
        assertEquals(403, ex.getStatusCode());
        assertEquals(0.95, ex.getRiskScore(), 0.001);
        assertEquals(2, ex.getMatchedRules().size());
        assertTrue(ex.getMatchedRules().contains("INJECT_001"));
        assertEquals("corr-123", ex.getCorrelationId());
    }

    @Test
    void blockedException_nullRulesDefaultsToEmpty() {
        var ex = new AibimBlockedException("blocked", 0.8, null, null);
        assertTrue(ex.getMatchedRules().isEmpty());
        assertNull(ex.getCorrelationId());
    }

    @Test
    void blockedException_rulesAreUnmodifiable() {
        var ex = new AibimBlockedException("blocked", 0.5, List.of("R1"), "c1");
        assertThrows(UnsupportedOperationException.class, () ->
                ex.getMatchedRules().add("R2"));
    }

    @Test
    void blockedException_extendsAibimException() {
        assertInstanceOf(AibimException.class, new AibimBlockedException("b", 0.5, null, null));
    }

    // --- AibimAuthException ---

    @Test
    void authException_has401Status() {
        var ex = new AibimAuthException("invalid API key");
        assertEquals("invalid API key", ex.getMessage());
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    void authException_extendsAibimException() {
        assertInstanceOf(AibimException.class, new AibimAuthException("test"));
    }

    // --- AibimRateLimitException ---

    @Test
    void rateLimitException_has429StatusAndRetryAfter() {
        var ex = new AibimRateLimitException("too many requests", Duration.ofSeconds(30));
        assertEquals("too many requests", ex.getMessage());
        assertEquals(429, ex.getStatusCode());
        assertTrue(ex.getRetryAfter().isPresent());
        assertEquals(Duration.ofSeconds(30), ex.getRetryAfter().get());
    }

    @Test
    void rateLimitException_nullRetryAfter() {
        var ex = new AibimRateLimitException("rate limited", null);
        assertTrue(ex.getRetryAfter().isEmpty());
    }

    @Test
    void rateLimitException_extendsAibimException() {
        assertInstanceOf(AibimException.class, new AibimRateLimitException("test", null));
    }
}
