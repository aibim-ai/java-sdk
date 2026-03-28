package io.github.aibim_ai.sdk.exceptions;

/**
 * Thrown when authentication with the AIBIM server fails.
 *
 * <p>This typically indicates an invalid or expired API key,
 * or an expired JWT token.
 */
public class AibimAuthException extends AibimException {

    /**
     * Creates a new authentication exception.
     *
     * @param message the error message
     */
    public AibimAuthException(String message) {
        super(401, message);
    }
}
