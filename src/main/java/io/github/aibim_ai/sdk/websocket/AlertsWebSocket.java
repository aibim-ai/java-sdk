package io.github.aibim_ai.sdk.websocket;

import io.github.aibim_ai.sdk.exceptions.AibimException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * WebSocket client for real-time AIBIM security alerts.
 *
 * <p>Connects to the AIBIM WebSocket endpoint and dispatches incoming
 * alert messages to registered callbacks:
 *
 * <pre>{@code
 * var ws = new AlertsWebSocket("https://your-aibim.example.com", "aibim-key");
 * ws.onAlert(alert -> {
 *     System.out.println("Alert: " + alert);
 * });
 * ws.connect();
 *
 * // ... later
 * ws.close();
 * }</pre>
 */
public class AlertsWebSocket implements AutoCloseable {

    private final String wsUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final List<Consumer<JsonObject>> callbacks;
    private final AtomicReference<WebSocket> webSocketRef;
    private final AtomicBoolean connected;

    /**
     * Creates a new alerts WebSocket client.
     *
     * @param baseUrl the AIBIM server base URL (http/https, will be converted to ws/wss)
     * @param apiKey  the API key for authentication
     */
    public AlertsWebSocket(String baseUrl, String apiKey) {
        this.wsUrl = toWsUrl(baseUrl) + "/ws/alerts";
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.callbacks = new CopyOnWriteArrayList<>();
        this.webSocketRef = new AtomicReference<>();
        this.connected = new AtomicBoolean(false);
    }

    /**
     * Registers a callback to receive alert messages.
     *
     * <p>Multiple callbacks can be registered. Each will receive every alert.
     * Callbacks are invoked on the WebSocket listener thread.
     *
     * @param callback the alert handler
     */
    public void onAlert(Consumer<JsonObject> callback) {
        callbacks.add(callback);
    }

    /**
     * Establishes the WebSocket connection.
     *
     * <p>Blocks until the connection is established or fails.
     *
     * @throws AibimException if the connection fails
     */
    public void connect() throws AibimException {
        if (connected.get()) {
            throw new AibimException(0, "Already connected. Call close() first.");
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        try {
            WebSocket ws = httpClient.newWebSocketBuilder()
                    .header("Authorization", "Bearer " + apiKey)
                    .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {

                        private final StringBuilder messageBuffer = new StringBuilder();

                        @Override
                        public void onOpen(WebSocket webSocket) {
                            connected.set(true);
                            latch.countDown();
                            webSocket.request(1);
                        }

                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                            messageBuffer.append(data);
                            if (last) {
                                String message = messageBuffer.toString();
                                messageBuffer.setLength(0);
                                dispatchMessage(message);
                            }
                            webSocket.request(1);
                            return null;
                        }

                        @Override
                        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                            connected.set(false);
                            latch.countDown();
                            return null;
                        }

                        @Override
                        public void onError(WebSocket webSocket, Throwable throwable) {
                            connected.set(false);
                            error.set(throwable);
                            latch.countDown();
                        }
                    })
                    .join();

            webSocketRef.set(ws);

            boolean opened = latch.await(10, TimeUnit.SECONDS);
            if (!opened) {
                throw new AibimException(0, "WebSocket connection timed out");
            }

            Throwable err = error.get();
            if (err != null) {
                throw new AibimException("WebSocket connection failed: " + err.getMessage(), err);
            }

            if (!connected.get()) {
                throw new AibimException(0, "WebSocket connection closed immediately");
            }
        } catch (AibimException e) {
            throw e;
        } catch (Exception e) {
            throw new AibimException("Failed to connect WebSocket: " + e.getMessage(), e);
        }
    }

    /**
     * Returns whether the WebSocket is currently connected.
     *
     * @return {@code true} if connected
     */
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * Closes the WebSocket connection.
     */
    @Override
    public void close() {
        WebSocket ws = webSocketRef.getAndSet(null);
        if (ws != null) {
            connected.set(false);
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "SDK close")
                    .exceptionally(e -> null);
        }
    }

    // ---- internal ----

    private void dispatchMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            for (Consumer<JsonObject> callback : callbacks) {
                try {
                    callback.accept(json);
                } catch (Exception e) {
                    // Callback errors should not break the listener loop
                }
            }
        } catch (Exception e) {
            // Non-JSON messages are silently ignored
        }
    }

    private static String toWsUrl(String httpUrl) {
        String url = httpUrl.endsWith("/")
                ? httpUrl.substring(0, httpUrl.length() - 1)
                : httpUrl;
        if (url.startsWith("https://")) {
            return "wss://" + url.substring("https://".length());
        }
        if (url.startsWith("http://")) {
            return "ws://" + url.substring("http://".length());
        }
        // Already ws/wss
        return url;
    }
}
