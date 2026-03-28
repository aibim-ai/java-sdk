package io.github.aibim_ai.sdk;

import io.github.aibim_ai.sdk.models.AibimOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProxyTest {

    /** Simple mock with a public String baseUrl field. */
    static class MockClient {
        public String baseUrl = "https://api.openai.com";
    }

    @Test
    void wrap_setsBaseUrl() {
        var client = new MockClient();
        var options = AibimOptions.builder()
                .url("https://proxy.aibim.ai")
                .apiKey("test-key")
                .build();

        AibimProxy.wrap(client, options);

        assertEquals("https://proxy.aibim.ai", client.baseUrl);
        assertTrue(AibimProxy.isWrapped(client));

        AibimProxy.unwrap(client);
    }

    @Test
    void unwrap_restoresOriginalUrl() {
        var client = new MockClient();
        var originalUrl = client.baseUrl;
        var options = AibimOptions.builder()
                .url("https://proxy.aibim.ai")
                .apiKey("test-key")
                .build();

        AibimProxy.wrap(client, options);
        AibimProxy.unwrap(client);

        assertEquals(originalUrl, client.baseUrl);
        assertFalse(AibimProxy.isWrapped(client));
    }

    @Test
    void isWrapped_returnsFalseForUnwrappedClient() {
        var client = new MockClient();
        assertFalse(AibimProxy.isWrapped(client));
    }

    @Test
    void isWrapped_returnsFalseForNull() {
        assertFalse(AibimProxy.isWrapped(null));
    }

    @Test
    void wrap_throwsOnAlreadyWrapped() {
        var client = new MockClient();
        var options = AibimOptions.builder()
                .url("https://proxy.aibim.ai")
                .apiKey("test-key")
                .build();

        AibimProxy.wrap(client, options);

        assertThrows(IllegalStateException.class, () -> AibimProxy.wrap(client, options));

        AibimProxy.unwrap(client);
    }

    @Test
    void unwrap_throwsOnNotWrapped() {
        var client = new MockClient();
        assertThrows(IllegalStateException.class, () -> AibimProxy.unwrap(client));
    }

    @Test
    void wrap_throwsOnNullClient() {
        var options = AibimOptions.builder()
                .url("https://proxy.aibim.ai")
                .apiKey("test-key")
                .build();
        assertThrows(IllegalArgumentException.class, () -> AibimProxy.wrap(null, options));
    }

    @Test
    void wrap_throwsOnNullOptions() {
        var client = new MockClient();
        assertThrows(IllegalArgumentException.class, () -> AibimProxy.wrap(client, null));
    }

    @Test
    void wrap_trimsTrailingSlash() {
        var client = new MockClient();
        var options = AibimOptions.builder()
                .url("https://proxy.aibim.ai/")
                .apiKey("test-key")
                .build();

        AibimProxy.wrap(client, options);

        assertEquals("https://proxy.aibim.ai", client.baseUrl);

        AibimProxy.unwrap(client);
    }

    @Test
    void wrap_throwsOnUnsupportedClient() {
        var client = new Object();
        var options = AibimOptions.builder()
                .url("https://proxy.aibim.ai")
                .apiKey("test-key")
                .build();

        assertThrows(IllegalArgumentException.class, () -> AibimProxy.wrap(client, options));
    }
}
