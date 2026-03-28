package io.github.aibim_ai.sdk.exceptions;

import java.time.Duration;
import java.util.Optional;

/**
 * Thrown when the AIBIM server returns HTTP 429 (Too Many Requests).
 *
 * <p>Includes the {@code Retry-After} duration when provided by the server.
 */
public class AibimRateLimitException extends AibimException {

    private final Duration retryAfter;

    /**
     * Creates a new rate limit exception.
     *
     * @param message    the error message
     * @param retryAfter the duration to wait before retrying, or {@code null} if not specified
     */
    public AibimRateLimitException(String message, Duration retryAfter) {
        super(429, message);
        this.retryAfter = retryAfter;
    }

    /**
     * Returns the duration to wait before retrying.
     *
     * @return the retry-after duration, or empty if not specified by the server
     */
    public Optional<Duration> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
