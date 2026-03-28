package io.github.aibim_ai.sdk.retry;

import io.github.aibim_ai.sdk.exceptions.AibimException;
import io.github.aibim_ai.sdk.exceptions.AibimRateLimitException;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Retry policy with exponential backoff and jitter.
 *
 * <p>Retries failed operations that throw {@link AibimException} with a status code
 * in the configured retryable set. Non-retryable exceptions are rethrown immediately.
 *
 * <p>For rate limit errors (429), the policy respects the {@code Retry-After} header
 * when available.
 */
public final class RetryPolicy {

    private static final Set<Integer> DEFAULT_RETRYABLE_STATUSES = Set.of(408, 429, 500, 502, 503, 504);

    private final int maxRetries;
    private final double backoffFactor;
    private final long maxBackoffMs;
    private final Set<Integer> retryableStatuses;

    /**
     * Creates a retry policy with default settings.
     *
     * @param maxRetries the maximum number of retries (default: 3)
     */
    public RetryPolicy(int maxRetries) {
        this(maxRetries, 2.0, 30_000L, DEFAULT_RETRYABLE_STATUSES);
    }

    /**
     * Creates a retry policy with full configuration.
     *
     * @param maxRetries        maximum number of retries
     * @param backoffFactor     exponential backoff multiplier
     * @param maxBackoffMs      maximum backoff duration in milliseconds
     * @param retryableStatuses HTTP status codes that should trigger a retry
     */
    public RetryPolicy(int maxRetries, double backoffFactor, long maxBackoffMs,
                       Set<Integer> retryableStatuses) {
        this.maxRetries = maxRetries;
        this.backoffFactor = backoffFactor;
        this.maxBackoffMs = maxBackoffMs;
        this.retryableStatuses = retryableStatuses;
    }

    /**
     * Executes the given callable with retry logic.
     *
     * <p>On failure, waits with exponential backoff plus jitter before retrying.
     * Rate limit errors respect the server-provided {@code Retry-After} duration.
     *
     * @param fn  the operation to execute
     * @param <T> the return type
     * @return the result of a successful execution
     * @throws AibimException if all retries are exhausted or a non-retryable error occurs
     */
    public <T> T execute(Callable<T> fn) throws AibimException {
        AibimException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return fn.call();
            } catch (AibimException e) {
                lastException = e;

                if (attempt >= maxRetries || !isRetryable(e)) {
                    throw e;
                }

                long sleepMs = calculateBackoff(attempt, e);
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AibimException("Retry interrupted", ie);
                }
            } catch (Exception e) {
                throw new AibimException("Unexpected error during request", e);
            }
        }

        // Should not reach here, but satisfy compiler
        throw lastException != null ? lastException
                : new AibimException(0, "Retry policy exhausted with no exception");
    }

    private boolean isRetryable(AibimException e) {
        return retryableStatuses.contains(e.getStatusCode());
    }

    private long calculateBackoff(int attempt, AibimException e) {
        // For rate limits, respect Retry-After header if present
        if (e instanceof AibimRateLimitException rle) {
            var retryAfter = rle.getRetryAfter();
            if (retryAfter.isPresent()) {
                return retryAfter.get().toMillis();
            }
        }

        // Exponential backoff: baseMs * factor^attempt
        long baseMs = 500L;
        long exponentialMs = (long) (baseMs * Math.pow(backoffFactor, attempt));
        long cappedMs = Math.min(exponentialMs, maxBackoffMs);

        // Add jitter: random value between 0 and cappedMs
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, cappedMs));
        return cappedMs + jitter;
    }
}
