package io.github.aibim_ai.sdk.models;

import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Metadata extracted from AIBIM-specific response headers.
 *
 * <p>The AIBIM proxy injects the following headers into every proxied response:
 * <ul>
 *   <li>{@code x-aibim-decision} — allow, warn, or block</li>
 *   <li>{@code x-aibim-score} — numeric risk score (0.0–1.0)</li>
 *   <li>{@code x-aibim-cache} — whether the response was served from semantic cache</li>
 *   <li>{@code x-correlation-id} — unique request correlation identifier</li>
 * </ul>
 *
 * @param decision    the security decision
 * @param riskScore   the risk score (0.0 = safe, 1.0 = malicious), empty if header absent
 * @param cacheHit    whether the response came from the semantic cache
 * @param correlationId the correlation ID for tracing, empty if header absent
 */
public record AibimResponseMeta(
        AibimDecision decision,
        Optional<Double> riskScore,
        boolean cacheHit,
        Optional<String> correlationId
) {

    /**
     * Extracts AIBIM metadata from an {@link HttpResponse}.
     *
     * @param response the HTTP response
     * @return parsed metadata
     */
    public static AibimResponseMeta fromResponse(HttpResponse<?> response) {
        var headers = response.headers();

        AibimDecision decision = AibimDecision.fromString(
                headers.firstValue("x-aibim-decision").orElse(null));

        Optional<Double> riskScore = headers.firstValue("x-aibim-score")
                .map(s -> {
                    try {
                        return Double.parseDouble(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                });

        boolean cacheHit = headers.firstValue("x-aibim-cache")
                .map(s -> "hit".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s))
                .orElse(false);

        Optional<String> correlationId = headers.firstValue("x-correlation-id");

        return new AibimResponseMeta(decision, riskScore, cacheHit, correlationId);
    }
}
