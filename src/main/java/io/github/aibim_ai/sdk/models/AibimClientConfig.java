package io.github.aibim_ai.sdk.models;

import java.time.Duration;
import java.util.Objects;

/**
 * Configuration for {@link io.github.aibim_ai.sdk.AibimClient}.
 *
 * <p>Use the {@link #builder()} method to construct instances:
 * <pre>{@code
 * AibimClientConfig config = AibimClientConfig.builder()
 *     .baseUrl("https://your-aibim.example.com")
 *     .apiKey("aibim-your-api-key")
 *     .timeout(Duration.ofSeconds(30))
 *     .maxRetries(3)
 *     .build();
 * }</pre>
 */
public final class AibimClientConfig {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final int DEFAULT_MAX_RETRIES = 3;

    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;
    private final int maxRetries;

    private AibimClientConfig(Builder builder) {
        this.baseUrl = Objects.requireNonNull(builder.baseUrl, "baseUrl must not be null");
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey must not be null");
        this.timeout = builder.timeout != null ? builder.timeout : DEFAULT_TIMEOUT;
        this.maxRetries = builder.maxRetries > 0 ? builder.maxRetries : DEFAULT_MAX_RETRIES;
    }

    /**
     * Returns the AIBIM server base URL.
     *
     * @return the base URL
     */
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * Returns the API key used for authentication.
     *
     * @return the API key
     */
    public String apiKey() {
        return apiKey;
    }

    /**
     * Returns the HTTP request timeout.
     *
     * @return the timeout duration
     */
    public Duration timeout() {
        return timeout;
    }

    /**
     * Returns the maximum number of retries for failed requests.
     *
     * @return the max retry count
     */
    public int maxRetries() {
        return maxRetries;
    }

    /**
     * Creates a new builder for {@link AibimClientConfig}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link AibimClientConfig}.
     */
    public static final class Builder {
        private String baseUrl;
        private String apiKey;
        private Duration timeout;
        private int maxRetries = DEFAULT_MAX_RETRIES;

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
         * Builds the {@link AibimClientConfig} instance.
         *
         * @return the constructed config
         * @throws NullPointerException if baseUrl or apiKey is null
         */
        public AibimClientConfig build() {
            return new AibimClientConfig(this);
        }
    }
}
