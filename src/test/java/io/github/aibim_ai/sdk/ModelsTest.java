package io.github.aibim_ai.sdk;

import io.github.aibim_ai.sdk.models.AibimDecision;
import io.github.aibim_ai.sdk.models.AibimOptions;
import io.github.aibim_ai.sdk.models.AibimResponseMeta;
import io.github.aibim_ai.sdk.models.AnalyzeResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ModelsTest {

    // --- AibimDecision ---

    @Test
    void decision_fromString_allow() {
        assertEquals(AibimDecision.ALLOW, AibimDecision.fromString("allow"));
    }

    @Test
    void decision_fromString_warn() {
        assertEquals(AibimDecision.WARN, AibimDecision.fromString("warn"));
    }

    @Test
    void decision_fromString_block() {
        assertEquals(AibimDecision.BLOCK, AibimDecision.fromString("block"));
    }

    @Test
    void decision_fromString_caseInsensitive() {
        assertEquals(AibimDecision.BLOCK, AibimDecision.fromString("BLOCK"));
        assertEquals(AibimDecision.WARN, AibimDecision.fromString("Warn"));
        assertEquals(AibimDecision.ALLOW, AibimDecision.fromString("ALLOW"));
    }

    @Test
    void decision_fromString_nullReturnsAllow() {
        assertEquals(AibimDecision.ALLOW, AibimDecision.fromString(null));
    }

    @Test
    void decision_fromString_unknownReturnsAllow() {
        assertEquals(AibimDecision.ALLOW, AibimDecision.fromString("unknown"));
        assertEquals(AibimDecision.ALLOW, AibimDecision.fromString(""));
    }

    @Test
    void decision_fromString_trims() {
        assertEquals(AibimDecision.BLOCK, AibimDecision.fromString("  block  "));
    }

    @Test
    void decision_getValue() {
        assertEquals("allow", AibimDecision.ALLOW.getValue());
        assertEquals("warn", AibimDecision.WARN.getValue());
        assertEquals("block", AibimDecision.BLOCK.getValue());
    }

    // --- AibimOptions ---

    @Test
    void options_builderCreatesInstance() {
        var options = AibimOptions.builder()
                .url("https://proxy.aibim.ai")
                .apiKey("my-key")
                .build();

        assertEquals("https://proxy.aibim.ai", options.url());
        assertEquals("my-key", options.apiKey());
    }

    @Test
    void options_builderThrowsOnNullUrl() {
        assertThrows(NullPointerException.class, () ->
                AibimOptions.builder().apiKey("k").build());
    }

    @Test
    void options_builderThrowsOnNullApiKey() {
        assertThrows(NullPointerException.class, () ->
                AibimOptions.builder().url("u").build());
    }

    // --- AibimResponseMeta ---

    @Test
    void responseMeta_recordFields() {
        var meta = new AibimResponseMeta(
                AibimDecision.BLOCK,
                Optional.of(0.95),
                true,
                Optional.of("corr-123")
        );

        assertEquals(AibimDecision.BLOCK, meta.decision());
        assertEquals(0.95, meta.riskScore().orElse(0.0), 0.001);
        assertTrue(meta.cacheHit());
        assertEquals("corr-123", meta.correlationId().orElse(""));
    }

    @Test
    void responseMeta_emptyOptionals() {
        var meta = new AibimResponseMeta(
                AibimDecision.ALLOW,
                Optional.empty(),
                false,
                Optional.empty()
        );

        assertEquals(AibimDecision.ALLOW, meta.decision());
        assertTrue(meta.riskScore().isEmpty());
        assertFalse(meta.cacheHit());
        assertTrue(meta.correlationId().isEmpty());
    }

    // --- AnalyzeResponse ---

    @Test
    void analyzeResponse_recordFields() {
        var resp = new AnalyzeResponse(
                0.85,
                true,
                AibimDecision.BLOCK,
                List.of("INJECT_001", "JAILBREAK_003"),
                "corr-1"
        );

        assertEquals(0.85, resp.riskScore(), 0.001);
        assertTrue(resp.isSuspicious());
        assertEquals(AibimDecision.BLOCK, resp.decision());
        assertEquals(2, resp.matchedRules().size());
        assertIterableEquals(List.of("INJECT_001", "JAILBREAK_003"), resp.matchedRules());
        assertEquals("corr-1", resp.correlationId());
    }

    @Test
    void analyzeResponse_emptyRules() {
        var resp = new AnalyzeResponse(0.0, false, AibimDecision.ALLOW, List.of(), null);

        assertEquals(0.0, resp.riskScore());
        assertFalse(resp.isSuspicious());
        assertTrue(resp.matchedRules().isEmpty());
        assertNull(resp.correlationId());
    }
}
