package io.github.aibim_ai.sdk.auth;

import io.github.aibim_ai.sdk.AibimHttpClient;
import io.github.aibim_ai.sdk.exceptions.AibimException;
import com.google.gson.JsonObject;

/**
 * Client for AIBIM authentication endpoints.
 *
 * <p>Provides methods for user login, registration, token refresh,
 * validation, and retrieving the current user profile.
 */
public final class AuthClient {

    private final AibimHttpClient httpClient;

    /**
     * Creates a new auth client. Used internally by {@link io.github.aibim_ai.sdk.AibimClient}.
     *
     * @param httpClient the shared HTTP client
     */
    public AuthClient(AibimHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return the authentication response containing access and refresh tokens
     * @throws AibimException if authentication fails
     */
    public JsonObject login(String email, String password) throws AibimException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);
        return httpClient.post("/auth/login", body);
    }

    /**
     * Registers a new user account.
     *
     * @param email    the user's email
     * @param password the user's password
     * @param name     the user's display name
     * @return the registration response
     * @throws AibimException if registration fails
     */
    public JsonObject register(String email, String password, String name) throws AibimException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);
        body.addProperty("name", name);
        return httpClient.post("/auth/register", body);
    }

    /**
     * Refreshes an authentication token pair.
     *
     * @param refreshToken the current refresh token
     * @return the new token pair
     * @throws AibimException if the refresh token is invalid or expired
     */
    public JsonObject refresh(String refreshToken) throws AibimException {
        JsonObject body = new JsonObject();
        body.addProperty("refresh_token", refreshToken);
        return httpClient.post("/auth/refresh", body);
    }

    /**
     * Validates the current authentication token.
     *
     * @return the validation result
     * @throws AibimException if the token is invalid
     */
    public JsonObject validate() throws AibimException {
        return httpClient.post("/auth/validate", new JsonObject());
    }

    /**
     * Retrieves the current user's profile.
     *
     * @return the user profile data
     * @throws AibimException if the request fails
     */
    public JsonObject me() throws AibimException {
        return httpClient.get("/auth/me");
    }
}
