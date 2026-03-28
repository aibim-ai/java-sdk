package io.github.aibim_ai.sdk.tenant;

import io.github.aibim_ai.sdk.AibimHttpClient;
import io.github.aibim_ai.sdk.exceptions.AibimException;
import com.google.gson.JsonObject;

/**
 * Client for AIBIM tenant management endpoints.
 *
 * <p>Provides access to tenant profile, configuration, detection mode,
 * API key management, and usage statistics.
 */
public final class TenantClient {

    private final AibimHttpClient httpClient;

    /**
     * Creates a new tenant client. Used internally by {@link io.github.aibim_ai.sdk.AibimClient}.
     *
     * @param httpClient the shared HTTP client
     */
    public TenantClient(AibimHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Retrieves the current tenant's profile.
     *
     * @return the tenant profile data
     * @throws AibimException if the request fails
     */
    public JsonObject me() throws AibimException {
        return httpClient.get("/tenant/me");
    }

    /**
     * Retrieves the current tenant's configuration.
     *
     * @return the tenant configuration
     * @throws AibimException if the request fails
     */
    public JsonObject getConfig() throws AibimException {
        return httpClient.get("/tenant/config");
    }

    /**
     * Updates the current tenant's configuration.
     *
     * @param config the new configuration values
     * @return the updated configuration
     * @throws AibimException if the request fails
     */
    public JsonObject updateConfig(JsonObject config) throws AibimException {
        return httpClient.put("/tenant/config", config);
    }

    /**
     * Retrieves the current detection mode (monitor, detect, enforce).
     *
     * @return the detection mode
     * @throws AibimException if the request fails
     */
    public JsonObject getDetectionMode() throws AibimException {
        return httpClient.get("/tenant/detection-mode");
    }

    /**
     * Sets the detection mode for the current tenant.
     *
     * @param mode the detection mode object (e.g., {@code {"mode": "enforce"}})
     * @return the updated detection mode
     * @throws AibimException if the request fails
     */
    public JsonObject setDetectionMode(JsonObject mode) throws AibimException {
        return httpClient.put("/tenant/detection-mode", mode);
    }

    /**
     * Lists all API keys for the current tenant.
     *
     * @return the list of API keys
     * @throws AibimException if the request fails
     */
    public JsonObject listApiKeys() throws AibimException {
        return httpClient.get("/tenant/keys");
    }

    /**
     * Creates a new API key for the current tenant.
     *
     * @param keyConfig the API key configuration (name, permissions, etc.)
     * @return the created API key (the secret is only returned once)
     * @throws AibimException if the request fails
     */
    public JsonObject createApiKey(JsonObject keyConfig) throws AibimException {
        return httpClient.post("/tenant/keys", keyConfig);
    }

    /**
     * Deletes (deactivates) an API key by ID.
     *
     * @param keyId the UUID of the API key to delete
     * @return the deletion confirmation
     * @throws AibimException if the request fails
     */
    public JsonObject deleteApiKey(String keyId) throws AibimException {
        return httpClient.delete("/tenant/keys/" + keyId);
    }

    /**
     * Retrieves usage statistics for the current tenant.
     *
     * @return the usage data (requests, tokens, costs, etc.)
     * @throws AibimException if the request fails
     */
    public JsonObject getUsage() throws AibimException {
        return httpClient.get("/tenant/usage");
    }
}
