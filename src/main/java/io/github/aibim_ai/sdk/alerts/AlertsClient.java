package io.github.aibim_ai.sdk.alerts;

import io.github.aibim_ai.sdk.AibimHttpClient;
import io.github.aibim_ai.sdk.exceptions.AibimException;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Client for AIBIM alerts and alert rules management.
 *
 * <p>Provides access to security alerts, alert rule configuration,
 * and alert statistics.
 */
public final class AlertsClient {

    private final AibimHttpClient httpClient;

    /**
     * Creates a new alerts client. Used internally by {@link io.github.aibim_ai.sdk.AibimClient}.
     *
     * @param httpClient the shared HTTP client
     */
    public AlertsClient(AibimHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Lists security alerts.
     *
     * @return the list of alerts
     * @throws AibimException if the request fails
     */
    public JsonObject list() throws AibimException {
        return httpClient.get("/alerts");
    }

    /**
     * Lists security alerts with query parameters for filtering.
     *
     * @param params query parameters (e.g., severity, status, limit, offset)
     * @return the filtered list of alerts
     * @throws AibimException if the request fails
     */
    public JsonObject list(Map<String, String> params) throws AibimException {
        return httpClient.get("/alerts", params);
    }

    /**
     * Lists alert rules configured for the current tenant.
     *
     * @return the list of alert rules
     * @throws AibimException if the request fails
     */
    public JsonObject listRules() throws AibimException {
        return httpClient.get("/alerts/rules");
    }

    /**
     * Creates a new alert rule.
     *
     * @param rule the alert rule definition (name, condition, severity, action, etc.)
     * @return the created alert rule
     * @throws AibimException if the request fails
     */
    public JsonObject createRule(JsonObject rule) throws AibimException {
        return httpClient.post("/alerts/rules", rule);
    }

    /**
     * Retrieves alert statistics (counts by severity, status, time period).
     *
     * @return the alert statistics
     * @throws AibimException if the request fails
     */
    public JsonObject stats() throws AibimException {
        return httpClient.get("/alerts/stats");
    }
}
