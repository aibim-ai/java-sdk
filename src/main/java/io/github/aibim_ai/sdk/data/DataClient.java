package io.github.aibim_ai.sdk.data;

import io.github.aibim_ai.sdk.AibimHttpClient;
import io.github.aibim_ai.sdk.exceptions.AibimException;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Client for AIBIM data query endpoints.
 *
 * <p>Provides read access to detection events, real-time statistics,
 * benchmarks, compliance frameworks, trust scores, threat intelligence,
 * and DLP events.
 */
public final class DataClient {

    private final AibimHttpClient httpClient;

    /**
     * Creates a new data client. Used internally by {@link io.github.aibim_ai.sdk.AibimClient}.
     *
     * @param httpClient the shared HTTP client
     */
    public DataClient(AibimHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Retrieves detection events with optional filtering.
     *
     * @return the list of detection events
     * @throws AibimException if the request fails
     */
    public JsonObject events() throws AibimException {
        return httpClient.get("/data/events");
    }

    /**
     * Retrieves detection events with query parameters for filtering.
     *
     * @param params query parameters (e.g., limit, offset, severity, date range)
     * @return the filtered list of detection events
     * @throws AibimException if the request fails
     */
    public JsonObject events(Map<String, String> params) throws AibimException {
        return httpClient.get("/data/events", params);
    }

    /**
     * Retrieves real-time statistics for the SOC dashboard.
     *
     * @return real-time stats (active threats, request rate, detection counts)
     * @throws AibimException if the request fails
     */
    public JsonObject realtimeStats() throws AibimException {
        return httpClient.get("/data/stats/realtime");
    }

    /**
     * Retrieves benchmark results for monitored models.
     *
     * @return benchmark data with model scores and attack surface evaluations
     * @throws AibimException if the request fails
     */
    public JsonObject benchmarks() throws AibimException {
        return httpClient.get("/data/benchmarks/models");
    }

    /**
     * Retrieves compliance framework statuses.
     *
     * @return compliance data with framework scores and findings
     * @throws AibimException if the request fails
     */
    public JsonObject compliance() throws AibimException {
        return httpClient.get("/data/compliance/frameworks");
    }

    /**
     * Retrieves trust scores for AI agents.
     *
     * @return trust agent data with scores and risk assessments
     * @throws AibimException if the request fails
     */
    public JsonObject trustAgents() throws AibimException {
        return httpClient.get("/data/trust/agents");
    }

    /**
     * Retrieves the threat intelligence feed.
     *
     * @return threat intel data with indicators, patterns, and sources
     * @throws AibimException if the request fails
     */
    public JsonObject threatFeed() throws AibimException {
        return httpClient.get("/data/threat-intel/feed");
    }

    /**
     * Retrieves DLP (Data Loss Prevention) events.
     *
     * @return DLP event data with detected sensitive data exposures
     * @throws AibimException if the request fails
     */
    public JsonObject dlpEvents() throws AibimException {
        return httpClient.get("/data/dlp/events");
    }

    /**
     * Retrieves DLP events with query parameters for filtering.
     *
     * @param params query parameters (e.g., limit, offset, severity)
     * @return the filtered list of DLP events
     * @throws AibimException if the request fails
     */
    public JsonObject dlpEvents(Map<String, String> params) throws AibimException {
        return httpClient.get("/data/dlp/events", params);
    }
}
