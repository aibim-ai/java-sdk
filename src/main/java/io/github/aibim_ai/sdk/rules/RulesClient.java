package io.github.aibim_ai.sdk.rules;

import io.github.aibim_ai.sdk.AibimHttpClient;
import io.github.aibim_ai.sdk.exceptions.AibimException;
import com.google.gson.JsonObject;

/**
 * Client for AIBIM detection rules management.
 *
 * <p>Provides CRUD operations for custom detection rules that define
 * how the AIBIM proxy identifies and handles threats.
 */
public final class RulesClient {

    private final AibimHttpClient httpClient;

    /**
     * Creates a new rules client. Used internally by {@link io.github.aibim_ai.sdk.AibimClient}.
     *
     * @param httpClient the shared HTTP client
     */
    public RulesClient(AibimHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Lists all detection rules for the current tenant.
     *
     * @return the list of detection rules
     * @throws AibimException if the request fails
     */
    public JsonObject list() throws AibimException {
        return httpClient.get("/rules");
    }

    /**
     * Creates a new detection rule.
     *
     * @param rule the rule definition (name, pattern, action, severity, etc.)
     * @return the created rule
     * @throws AibimException if the request fails
     */
    public JsonObject create(JsonObject rule) throws AibimException {
        return httpClient.post("/rules", rule);
    }

    /**
     * Deletes a detection rule by ID.
     *
     * @param ruleId the UUID of the rule to delete
     * @return the deletion confirmation
     * @throws AibimException if the request fails
     */
    public JsonObject delete(String ruleId) throws AibimException {
        return httpClient.delete("/rules/" + ruleId);
    }
}
