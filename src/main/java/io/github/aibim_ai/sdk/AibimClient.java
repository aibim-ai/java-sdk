package io.github.aibim_ai.sdk;

import io.github.aibim_ai.sdk.alerts.AlertsClient;
import io.github.aibim_ai.sdk.auth.AuthClient;
import io.github.aibim_ai.sdk.data.DataClient;
import io.github.aibim_ai.sdk.exceptions.AibimException;
import io.github.aibim_ai.sdk.models.AibimClientConfig;
import io.github.aibim_ai.sdk.rules.RulesClient;
import io.github.aibim_ai.sdk.tenant.TenantClient;
import com.google.gson.JsonObject;

import java.time.Duration;
import java.util.Objects;

/**
 * Full-featured client for the AIBIM API.
 *
 * <p>Provides access to all AIBIM API endpoints through typed sub-clients.
 * Use the {@link #builder()} method to construct instances:
 *
 * <pre>{@code
 * try (var client = AibimClient.builder()
 *         .baseUrl("https://your-aibim.example.com")
 *         .apiKey("aibim-your-api-key")
 *         .timeout(Duration.ofSeconds(30))
 *         .build()) {
 *
 *     // Authentication
 *     JsonObject user = client.auth().me();
 *
 *     // Detection rules
 *     JsonObject rules = client.rules().list();
 *
 *     // Real-time data
 *     JsonObject stats = client.data().realtimeStats();
 *
 *     // Health check
 *     JsonObject health = client.health();
 * }
 * }</pre>
 */
public final class AibimClient implements AutoCloseable {

    private final AibimHttpClient httpClient;
    private final AuthClient authClient;
    private final RulesClient rulesClient;
    private final TenantClient tenantClient;
    private final DataClient dataClient;
    private final AlertsClient alertsClient;

    private AibimClient(AibimClientConfig config) {
        this.httpClient = new AibimHttpClient(config);
        this.authClient = new AuthClient(httpClient);
        this.rulesClient = new RulesClient(httpClient);
        this.tenantClient = new TenantClient(httpClient);
        this.dataClient = new DataClient(httpClient);
        this.alertsClient = new AlertsClient(httpClient);
    }

    /**
     * Returns the authentication sub-client.
     *
     * @return the auth client for login, register, refresh, validate, me
     */
    public AuthClient auth() {
        return authClient;
    }

    /**
     * Returns the detection rules sub-client.
     *
     * @return the rules client for list, create, delete
     */
    public RulesClient rules() {
        return rulesClient;
    }

    /**
     * Returns the tenant management sub-client.
     *
     * @return the tenant client for config, detection mode, API keys, usage
     */
    public TenantClient tenant() {
        return tenantClient;
    }

    /**
     * Returns the data query sub-client.
     *
     * @return the data client for events, stats, benchmarks, compliance, etc.
     */
    public DataClient data() {
        return dataClient;
    }

    /**
     * Returns the alerts sub-client.
     *
     * @return the alerts client for list, rules, stats
     */
    public AlertsClient alerts() {
        return alertsClient;
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

    /**
     * Returns the underlying HTTP client for advanced use cases.
     *
     * @return the internal HTTP client
     */
    public AibimHttpClient httpClient() {
        return httpClient;
    }

    /**
     * Closes this client and releases resources.
     */
    @Override
    public void close() {
        // HttpClient does not require explicit close in Java 17
        // This method exists for AutoCloseable compatibility
    }

    /**
     * Creates a new builder for {@link AibimClient}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link AibimClient}.
     */
    public static final class Builder {
        private String baseUrl;
        private String apiKey;
        private Duration timeout;
        private int maxRetries = 3;

        private Builder() {
        }

        /**
         * Sets the AIBIM server base URL.
         *
         * @param baseUrl the base URL (without trailing slash)
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the API key for authentication.
         *
         * @param apiKey the API key
         * @return this builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Sets the HTTP request timeout.
         *
         * @param timeout the timeout duration (default: 30 seconds)
         * @return this builder
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets the maximum number of retries for failed requests.
         *
         * @param maxRetries the max retry count (default: 3)
         * @return this builder
         */
        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        /**
         * Builds the {@link AibimClient} instance.
         *
         * @return the constructed client
         * @throws NullPointerException if baseUrl or apiKey is null
         */
        public AibimClient build() {
            Objects.requireNonNull(baseUrl, "baseUrl must not be null");
            Objects.requireNonNull(apiKey, "apiKey must not be null");

            AibimClientConfig config = AibimClientConfig.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .timeout(timeout)
                    .maxRetries(maxRetries)
                    .build();

            return new AibimClient(config);
        }
    }
}
