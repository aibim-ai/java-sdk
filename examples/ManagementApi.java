// Example: Use AibimClient for tenant management, rules, and data operations.
//
// Compile: javac -cp aibim-sdk.jar:gson.jar examples/ManagementApi.java
// Run:     java -cp aibim-sdk.jar:gson.jar:examples ManagementApi

import io.github.aibim_ai.sdk.AibimClient;
import io.github.aibim_ai.sdk.exceptions.AibimException;

import java.time.Duration;

public class ManagementApi {

    public static void main(String[] args) {
        String baseUrl = System.getenv("AIBIM_URL") != null
                ? System.getenv("AIBIM_URL")
                : "https://proxy.aibim.ai";

        String apiKey = System.getenv("AIBIM_API_KEY") != null
                ? System.getenv("AIBIM_API_KEY")
                : "aibim-your-api-key";

        try (var client = AibimClient.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(30))
                .build()) {

            // Health check.
            try {
                var health = client.health();
                System.out.println("Health: " + health);
            } catch (AibimException e) {
                System.out.println("Health check failed: " + e.getMessage());
            }

            System.out.println();

            // Sub-clients available:
            //   client.auth()    - Login, Register, Refresh, Validate, Me
            //   client.rules()   - List, Create, Delete detection rules
            //   client.tenant()  - Config, Detection mode, API keys, Usage
            //   client.data()    - Events, Stats, Benchmarks, Compliance
            //   client.alerts()  - List alerts, Rule management, Stats

            // Example: List detection rules.
            try {
                var rules = client.rules().list();
                System.out.println("Detection rules: " + rules);
            } catch (AibimException e) {
                System.out.println("Rules list error: " + e.getMessage());
            }

            // Example: Get tenant config.
            try {
                var config = client.tenant().getConfig();
                System.out.println("Tenant config: " + config);
            } catch (AibimException e) {
                System.out.println("Tenant config error: " + e.getMessage());
            }

            // Example: Deep health check.
            try {
                var deepHealth = client.deepHealth();
                System.out.println("Deep health: " + deepHealth);
            } catch (AibimException e) {
                System.out.println("Deep health error: " + e.getMessage());
            }

        }
    }
}
