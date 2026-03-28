package io.github.aibim_ai.sdk.exceptions;

/**
 * Base exception for all AIBIM SDK errors.
 *
 * <p>Contains the HTTP status code and error message returned by the AIBIM server.
 */
public class AibimException extends Exception {

    private final int statusCode;

    /**
     * Creates a new AIBIM exception.
     *
     * @param statusCode the HTTP status code (0 if not an HTTP error)
     * @param message    the error message
     */
    public AibimException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Creates a new AIBIM exception wrapping a cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public AibimException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    /**
     * Returns the HTTP status code associated with this error.
     *
     * @return the status code, or 0 if not an HTTP error
     */
    public int getStatusCode() {
        return statusCode;
    }
}
