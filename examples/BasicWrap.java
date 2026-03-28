// Example: Wrap an LLM client to route traffic through the AIBIM security proxy.
//
// Compile: javac -cp aibim-sdk.jar examples/BasicWrap.java
// Run:     java -cp aibim-sdk.jar:examples BasicWrap

import io.github.aibim_ai.sdk.AibimProxy;
import io.github.aibim_ai.sdk.models.AibimOptions;

public class BasicWrap {

    // Simulated LLM client with a baseUrl field.
    static class MockLlmClient {
        public String baseUrl = "https://api.openai.com";

        public String getBaseUrl() {
            return baseUrl;
        }
    }

    public static void main(String[] args) {
        var client = new MockLlmClient();
        System.out.println("Original base URL: " + client.getBaseUrl());

        // Configure AIBIM proxy options.
        String aibimUrl = System.getenv("AIBIM_URL") != null
                ? System.getenv("AIBIM_URL")
                : "https://proxy.aibim.ai";

        String apiKey = System.getenv("AIBIM_API_KEY") != null
                ? System.getenv("AIBIM_API_KEY")
                : "aibim-your-api-key";

        var options = AibimOptions.builder()
                .url(aibimUrl)
                .apiKey(apiKey)
                .build();

        // Wrap the client.
        AibimProxy.wrap(client, options);
        System.out.println("Wrapped base URL:  " + client.getBaseUrl());
        System.out.println("Is wrapped:        " + AibimProxy.isWrapped(client));

        // All requests through the client now flow through the AIBIM proxy.
        // The proxy inspects, scores, and optionally blocks malicious content.

        // Unwrap to restore the original URL.
        AibimProxy.unwrap(client);
        System.out.println("Restored base URL: " + client.getBaseUrl());
        System.out.println("Is wrapped:        " + AibimProxy.isWrapped(client));
    }
}
