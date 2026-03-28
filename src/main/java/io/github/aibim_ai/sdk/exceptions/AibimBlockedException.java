package io.github.aibim_ai.sdk.exceptions;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when the AIBIM proxy blocks a request due to detected threats.
 *
 * <p>Contains the risk score, matched detection rules, and correlation ID
 * for forensic investigation.
 */
public class AibimBlockedException extends AibimException {

    private final double riskScore;
    private final List<String> matchedRules;
    private final String correlationId;

    /**
     * Creates a new blocked exception.
     *
     * @param message       the error message
     * @param riskScore     the risk score (0.0–1.0)
     * @param matchedRules  the detection rules that triggered
     * @param correlationId the correlation ID for tracing
     */
    public AibimBlockedException(String message, double riskScore,
                                  List<String> matchedRules, String correlationId) {
        super(403, message);
        this.riskScore = riskScore;
        this.matchedRules = matchedRules != null
                ? Collections.unmodifiableList(matchedRules)
                : Collections.emptyList();
        this.correlationId = correlationId;
    }

    /**
     * Returns the risk score that triggered the block.
     *
     * @return risk score between 0.0 and 1.0
     */
    public double getRiskScore() {
        return riskScore;
    }

    /**
     * Returns the list of detection rules that matched.
     *
     * @return unmodifiable list of rule names
     */
    public List<String> getMatchedRules() {
        return matchedRules;
    }

    /**
     * Returns the correlation ID for tracing this blocked request.
     *
     * @return the correlation ID
     */
    public String getCorrelationId() {
        return correlationId;
    }
}
