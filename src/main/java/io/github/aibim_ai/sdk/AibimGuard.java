package io.github.aibim_ai.sdk;

import io.github.aibim_ai.sdk.exceptions.AibimException;
import io.github.aibim_ai.sdk.models.AibimDecision;
import io.github.aibim_ai.sdk.models.AnalyzeResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Standalone guard for analyzing text inputs against AIBIM detection rules.
 *
 * <p>Use this when you want to pre-screen prompts or responses without routing
 * through the full proxy pipeline:
 *
 * <pre>{@code
 * var guard = new AibimGuard("https://your-aibim.example.com", "aibim-key");
 *
 * AnalyzeResponse result = guard.analyze("Ignore all previous instructions...");
 * if (result.decision() == AibimDecision.BLOCK) {
 *     System.out.println("Blocked! Risk: " + result.riskScore());
 * }
 * }</pre>
 */
public class AibimGuard {

    private static final Gson GSON = new Gson();
    private final AibimHttpClient httpClient;

    /**
     * Creates a new guard instance.
     *
     * @param baseUrl the AIBIM server base URL
     * @param apiKey  the API key for authentication
     */
    public AibimGuard(String baseUrl, String apiKey) {
        this.httpClient = new AibimHttpClient(baseUrl, apiKey);
    }

    /**
     * Analyzes text for security threats using the AIBIM detection pipeline.
     *
     * @param text the text to analyze (prompt or response content)
     * @return the analysis result with risk score, decision, and matched rules
     * @throws AibimException if the request fails
     */
    public AnalyzeResponse analyze(String text) throws AibimException {
        return analyze(text, null);
    }

    /**
     * Analyzes text for security threats, specifying the target LLM model.
     *
     * @param text  the text to analyze
     * @param model the target LLM model name (e.g., "gpt-4"), or {@code null}
     * @return the analysis result
     * @throws AibimException if the request fails
     */
    public AnalyzeResponse analyze(String text, String model) throws AibimException {
        JsonObject body = new JsonObject();
        body.addProperty("text", text);
        if (model != null && !model.isEmpty()) {
            body.addProperty("model", model);
        }

        JsonObject response = httpClient.post("/alerts/analyze", body);
        return parseAnalyzeResponse(response);
    }

    /**
     * Analyzes text asynchronously.
     *
     * @param text the text to analyze
     * @return a future that completes with the analysis result
     */
    public CompletableFuture<AnalyzeResponse> analyzeAsync(String text) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return analyze(text);
            } catch (AibimException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        });
    }

    /**
     * Analyzes text asynchronously with a specific model.
     *
     * @param text  the text to analyze
     * @param model the target LLM model name
     * @return a future that completes with the analysis result
     */
    public CompletableFuture<AnalyzeResponse> analyzeAsync(String text, String model) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return analyze(text, model);
            } catch (AibimException e) {
                throw new java.util.concurrent.CompletionException(e);
            }
        });
    }

    /**
     * Checks the AIBIM server health status.
     *
     * @return the health check response
     * @throws AibimException if the request fails
     */
    public JsonObject health() throws AibimException {
        return httpClient.getRaw("/health");
    }

    /**
     * Performs a deep health check of all AIBIM subsystems.
     *
     * @return the deep health check response
     * @throws AibimException if the request fails
     */
    public JsonObject deepHealth() throws AibimException {
        return httpClient.getRaw("/health/deep");
    }

    // ---- internal ----

    private AnalyzeResponse parseAnalyzeResponse(JsonObject json) {
        double riskScore = json.has("risk_score")
                ? json.get("risk_score").getAsDouble() : 0.0;

        boolean isSuspicious = json.has("is_suspicious")
                && json.get("is_suspicious").getAsBoolean();

        AibimDecision decision = json.has("decision")
                ? AibimDecision.fromString(json.get("decision").getAsString())
                : AibimDecision.ALLOW;

        List<String> matchedRules;
        if (json.has("matched_rules") && json.get("matched_rules").isJsonArray()) {
            JsonArray arr = json.getAsJsonArray("matched_rules");
            List<String> rules = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                rules.add(arr.get(i).getAsString());
            }
            matchedRules = Collections.unmodifiableList(rules);
        } else {
            matchedRules = Collections.emptyList();
        }

        String correlationId = json.has("correlation_id")
                ? json.get("correlation_id").getAsString() : null;

        return new AnalyzeResponse(riskScore, isSuspicious, decision, matchedRules, correlationId);
    }
}
