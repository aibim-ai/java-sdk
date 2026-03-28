package io.github.aibim_ai.sdk;

import io.github.aibim_ai.sdk.exceptions.AibimAuthException;
import io.github.aibim_ai.sdk.exceptions.AibimBlockedException;
import io.github.aibim_ai.sdk.exceptions.AibimException;
import io.github.aibim_ai.sdk.exceptions.AibimRateLimitException;
import io.github.aibim_ai.sdk.models.AibimClientConfig;
import io.github.aibim_ai.sdk.retry.RetryPolicy;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Internal HTTP client for communicating with the AIBIM API.
 *
 * <p>Handles authentication headers, request serialization, response envelope
 * unwrapping, error mapping, and retry logic. Not intended for direct use
 * by SDK consumers.
 */
public final class AibimHttpClient {

    private static final String API_PREFIX = "/api/v1";
    private static final Gson GSON = new Gson();

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final RetryPolicy retryPolicy;

    /**
     * Creates an HTTP client from a full config.
     *
     * @param config the client configuration
     */
    public AibimHttpClient(AibimClientConfig config) {
        this.baseUrl = stripTrailingSlash(config.baseUrl());
        this.apiKey = config.apiKey();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.timeout())
                .build();
        this.retryPolicy = new RetryPolicy(config.maxRetries());
    }

    /**
     * Creates an HTTP client with minimal settings.
     *
     * @param baseUrl the AIBIM server base URL
     * @param apiKey  the API key
     */
    public AibimHttpClient(String baseUrl, String apiKey) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.retryPolicy = new RetryPolicy(3);
    }

    /**
     * Sends a GET request and returns the unwrapped data.
     *
     * @param path the API path (e.g. {@code /auth/me})
     * @return the parsed JSON data from the response envelope
     * @throws AibimException if the request fails
     */
    public JsonObject get(String path) throws AibimException {
        return retryPolicy.execute(() -> {
            HttpRequest request = newRequestBuilder(path)
                    .GET()
                    .build();
            return executeRequest(request);
        });
    }

    /**
     * Sends a GET request with query parameters and returns the unwrapped data.
     *
     * @param path   the API path
     * @param params query parameters
     * @return the parsed JSON data from the response envelope
     * @throws AibimException if the request fails
     */
    public JsonObject get(String path, Map<String, String> params) throws AibimException {
        return retryPolicy.execute(() -> {
            String queryString = buildQueryString(params);
            String fullPath = queryString.isEmpty() ? path : path + "?" + queryString;
            HttpRequest request = newRequestBuilder(fullPath)
                    .GET()
                    .build();
            return executeRequest(request);
        });
    }

    /**
     * Sends a POST request with a JSON body and returns the unwrapped data.
     *
     * @param path the API path
     * @param body the request body (will be serialized to JSON)
     * @return the parsed JSON data from the response envelope
     * @throws AibimException if the request fails
     */
    public JsonObject post(String path, Object body) throws AibimException {
        return retryPolicy.execute(() -> {
            String json = GSON.toJson(body);
            HttpRequest request = newRequestBuilder(path)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();
            return executeRequest(request);
        });
    }

    /**
     * Sends a PUT request with a JSON body and returns the unwrapped data.
     *
     * @param path the API path
     * @param body the request body (will be serialized to JSON)
     * @return the parsed JSON data from the response envelope
     * @throws AibimException if the request fails
     */
    public JsonObject put(String path, Object body) throws AibimException {
        return retryPolicy.execute(() -> {
            String json = GSON.toJson(body);
            HttpRequest request = newRequestBuilder(path)
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();
            return executeRequest(request);
        });
    }

    /**
     * Sends a DELETE request and returns the unwrapped data.
     *
     * @param path the API path
     * @return the parsed JSON data from the response envelope
     * @throws AibimException if the request fails
     */
    public JsonObject delete(String path) throws AibimException {
        return retryPolicy.execute(() -> {
            HttpRequest request = newRequestBuilder(path)
                    .DELETE()
                    .build();
            return executeRequest(request);
        });
    }

    /**
     * Sends a raw GET request without the API prefix (for health endpoints).
     *
     * @param fullPath the full path (e.g. {@code /health})
     * @return the raw JSON response
     * @throws AibimException if the request fails
     */
    public JsonObject getRaw(String fullPath) throws AibimException {
        return retryPolicy.execute(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + fullPath))
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = sendRequest(request);
            checkStatus(response);
            return JsonParser.parseString(response.body()).getAsJsonObject();
        });
    }

    /**
     * Returns the underlying java.net.http.HttpClient for WebSocket use.
     *
     * @return the HTTP client
     */
    public HttpClient rawClient() {
        return httpClient;
    }

    /**
     * Returns the base URL.
     *
     * @return the AIBIM server base URL
     */
    public String baseUrl() {
        return baseUrl;
    }

    // ---- internal helpers ----

    private HttpRequest.Builder newRequestBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + API_PREFIX + path))
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(30));
    }

    private JsonObject executeRequest(HttpRequest request) throws AibimException {
        HttpResponse<String> response = sendRequest(request);
        checkStatus(response);
        return unwrapEnvelope(response.body());
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws AibimException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new AibimException("Network error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AibimException("Request interrupted", e);
        }
    }

    private void checkStatus(HttpResponse<String> response) throws AibimException {
        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return;
        }

        String body = response.body();
        String errorMessage = extractErrorMessage(body, status);

        switch (status) {
            case 401 -> throw new AibimAuthException(errorMessage);
            case 403 -> {
                JsonObject errorData = tryParseJson(body);
                double riskScore = errorData != null && errorData.has("risk_score")
                        ? errorData.get("risk_score").getAsDouble() : 0.0;
                List<String> matchedRules = errorData != null && errorData.has("matched_rules")
                        ? GSON.fromJson(errorData.get("matched_rules"),
                        new TypeToken<List<String>>() {}.getType())
                        : Collections.emptyList();
                String correlationId = response.headers()
                        .firstValue("x-correlation-id").orElse(null);
                throw new AibimBlockedException(errorMessage, riskScore, matchedRules, correlationId);
            }
            case 429 -> {
                Duration retryAfter = response.headers()
                        .firstValue("retry-after")
                        .map(s -> {
                            try {
                                return Duration.ofSeconds(Long.parseLong(s));
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        })
                        .orElse(null);
                throw new AibimRateLimitException(errorMessage, retryAfter);
            }
            default -> throw new AibimException(status, errorMessage);
        }
    }

    private String extractErrorMessage(String body, int status) {
        JsonObject json = tryParseJson(body);
        if (json != null && json.has("error")) {
            JsonElement error = json.get("error");
            if (error.isJsonPrimitive()) {
                return error.getAsString();
            }
            if (error.isJsonObject() && error.getAsJsonObject().has("message")) {
                return error.getAsJsonObject().get("message").getAsString();
            }
        }
        if (json != null && json.has("message")) {
            return json.get("message").getAsString();
        }
        return "HTTP " + status + ": " + (body != null && !body.isEmpty() ? body : "Unknown error");
    }

    private JsonObject unwrapEnvelope(String body) throws AibimException {
        if (body == null || body.isEmpty()) {
            return new JsonObject();
        }

        JsonObject json = tryParseJson(body);
        if (json == null) {
            throw new AibimException(0, "Invalid JSON response: " + body);
        }

        // AIBIM response envelope: { success: bool, data: ..., error: ... }
        if (json.has("success")) {
            boolean success = json.get("success").getAsBoolean();
            if (!success) {
                String errorMsg = json.has("error") ? json.get("error").getAsString() : "Unknown error";
                throw new AibimException(0, errorMsg);
            }
            if (json.has("data")) {
                JsonElement data = json.get("data");
                if (data.isJsonObject()) {
                    return data.getAsJsonObject();
                }
                // Wrap non-object data in a wrapper
                JsonObject wrapper = new JsonObject();
                wrapper.add("data", data);
                return wrapper;
            }
            return new JsonObject();
        }

        // No envelope — return raw JSON
        return json;
    }

    private JsonObject tryParseJson(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            JsonElement el = JsonParser.parseString(body);
            return el.isJsonObject() ? el.getAsJsonObject() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildQueryString(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (var entry : params.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append("&");
            }
            sb.append(java.net.URLEncoder.encode(entry.getKey(), java.nio.charset.StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(java.net.URLEncoder.encode(entry.getValue(), java.nio.charset.StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
