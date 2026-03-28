package io.github.aibim_ai.sdk.models;

import java.util.Objects;

/**
 * Configuration options for {@link io.github.aibim_ai.sdk.AibimProxy#wrap(Object, AibimOptions)}.
 *
 * <p>Use the {@link #builder()} method to construct instances:
 * <pre>{@code
 * AibimOptions opts = AibimOptions.builder()
 *     .url("https://your-aibim.example.com")
 *     .apiKey("aibim-your-api-key")
 *     .build();
 * }</pre>
 */
public final class AibimOptions {

    private final String url;
    private final String apiKey;

    private AibimOptions(Builder builder) {
        this.url = Objects.requireNonNull(builder.url, "url must not be null");
        this.apiKey = Objects.requireNonNull(builder.apiKey, "apiKey must not be null");
    }

    /**
     * Returns the AIBIM proxy base URL.
     *
     * @return the base URL (e.g. {@code https://your-aibim.example.com})
     */
    public String url() {
        return url;
    }

    /**
     * Returns the AIBIM API key used for authentication.
     *
     * @return the API key
     */
    public String apiKey() {
        return apiKey;
    }

    /**
     * Creates a new builder for {@link AibimOptions}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link AibimOptions}.
     */
    public static final class Builder {
        private String url;
        private String apiKey;

        private Builder() {
        }

        /**
         * Sets the AIBIM proxy base URL.
         *
         * @param url the base URL (without trailing slash)
         * @return this builder
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * Sets the AIBIM API key for authentication.
         *
         * @param apiKey the API key
         * @return this builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Builds the {@link AibimOptions} instance.
         *
         * @return the constructed options
         * @throws NullPointerException if url or apiKey is null
         */
        public AibimOptions build() {
            return new AibimOptions(this);
        }
    }
}
