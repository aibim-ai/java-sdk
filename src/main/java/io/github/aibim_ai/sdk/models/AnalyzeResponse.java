package io.github.aibim_ai.sdk.models;

import java.util.List;

/**
 * Response from the AIBIM analyze endpoint ({@code POST /api/v1/alerts/analyze}).
 *
 * <p>Contains the security analysis results for a given text input,
 * including risk assessment and any matched detection rules.
 *
 * @param riskScore     risk score between 0.0 (safe) and 1.0 (malicious)
 * @param isSuspicious  whether the input was flagged as suspicious
 * @param decision      the security decision (allow, warn, block)
 * @param matchedRules  list of detection rule names that matched
 * @param correlationId unique identifier for this analysis request
 */
public record AnalyzeResponse(
        double riskScore,
        boolean isSuspicious,
        AibimDecision decision,
        List<String> matchedRules,
        String correlationId
) {
}
