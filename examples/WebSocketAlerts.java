// Example: Subscribe to real-time AIBIM security alerts via WebSocket.
//
// Compile: javac -cp aibim-sdk.jar:gson.jar examples/WebSocketAlerts.java
// Run:     java -cp aibim-sdk.jar:gson.jar:examples WebSocketAlerts

import io.github.aibim_ai.sdk.exceptions.AibimException;
import io.github.aibim_ai.sdk.websocket.AlertsWebSocket;

public class WebSocketAlerts {

    public static void main(String[] args) {
        String baseUrl = System.getenv("AIBIM_URL") != null
                ? System.getenv("AIBIM_URL")
                : "https://proxy.aibim.ai";

        String apiKey = System.getenv("AIBIM_API_KEY") != null
                ? System.getenv("AIBIM_API_KEY")
                : "aibim-your-api-key";

        try (var ws = new AlertsWebSocket(baseUrl, apiKey)) {

            // Register alert callbacks.
            ws.onAlert(alert -> {
                System.out.println("[ALERT] " + alert);

                if (alert.has("decision")) {
                    System.out.println("  Decision: " + alert.get("decision").getAsString());
                }
                if (alert.has("risk_score")) {
                    System.out.printf("  Risk Score: %.2f%n", alert.get("risk_score").getAsDouble());
                }
                if (alert.has("correlation_id")) {
                    System.out.println("  Correlation ID: " + alert.get("correlation_id").getAsString());
                }
                if (alert.has("matched_rules")) {
                    System.out.println("  Matched Rules: " + alert.get("matched_rules"));
                }
                System.out.println();
            });

            // Connect and listen.
            System.out.println("Connecting to AIBIM alerts WebSocket...");
            ws.connect();
            System.out.println("Connected. Listening for alerts (Ctrl+C to stop)...");

            // Keep the main thread alive.
            // In a real application, you might use a shutdown hook or signal handler.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down...");
                ws.close();
            }));

            // Block until interrupted.
            Thread.currentThread().join();

        } catch (AibimException e) {
            System.err.println("WebSocket error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            System.out.println("Interrupted, exiting.");
            Thread.currentThread().interrupt();
        }
    }
}
