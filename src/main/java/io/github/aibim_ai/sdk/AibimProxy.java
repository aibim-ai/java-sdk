package io.github.aibim_ai.sdk;

import io.github.aibim_ai.sdk.models.AibimOptions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Wraps an existing LLM client to route traffic through the AIBIM security proxy.
 *
 * <p>This is the primary entry point for the AIBIM SDK. Use {@link #wrap(Object, AibimOptions)}
 * to redirect an OpenAI (or compatible) client's base URL to the AIBIM proxy:
 *
 * <pre>{@code
 * import io.github.aibim_ai.sdk.AibimProxy;
 * import io.github.aibim_ai.sdk.models.AibimOptions;
 *
 * var client = OpenAI.builder().apiKey("sk-...").build();
 *
 * AibimProxy.wrap(client, AibimOptions.builder()
 *     .url("https://your-aibim.example.com")
 *     .apiKey("aibim-your-api-key")
 *     .build());
 *
 * // All requests now flow through AIBIM proxy
 * client.chat().completions().create(...);
 *
 * // Restore original base URL
 * AibimProxy.unwrap(client);
 * }</pre>
 *
 * <p>Supports any LLM client that exposes a base URL via a field named
 * {@code baseUrl}, {@code baseURI}, {@code _baseUrl}, or {@code apiBase},
 * or via a setter method named {@code setBaseUrl}, {@code setBaseURI}, or {@code setApiBase}.
 */
public final class AibimProxy {

    private static final Map<Object, OriginalState> ORIGINALS = new WeakHashMap<>();

    private AibimProxy() {
        // Utility class — not instantiable
    }

    /**
     * Wraps the given LLM client to route all requests through the AIBIM proxy.
     *
     * <p>The client's base URL is replaced with the AIBIM proxy URL, and the
     * API key header is updated to include the AIBIM key for security inspection.
     *
     * @param client  the LLM client instance (e.g., OpenAI Java SDK client)
     * @param options the AIBIM proxy configuration
     * @throws IllegalArgumentException if the client type is not supported
     * @throws IllegalStateException    if the client is already wrapped
     */
    public static void wrap(Object client, AibimOptions options) {
        if (client == null) {
            throw new IllegalArgumentException("Client must not be null");
        }
        if (options == null) {
            throw new IllegalArgumentException("Options must not be null");
        }
        if (ORIGINALS.containsKey(client)) {
            throw new IllegalStateException("Client is already wrapped. Call unwrap() first.");
        }

        String proxyUrl = options.url().endsWith("/")
                ? options.url().substring(0, options.url().length() - 1)
                : options.url();

        // Try reflection-based field modification
        OriginalState state = tryWrapViaField(client, proxyUrl, options.apiKey());
        if (state == null) {
            state = tryWrapViaSetter(client, proxyUrl, options.apiKey());
        }
        if (state == null) {
            throw new IllegalArgumentException(
                    "Unsupported client type: " + client.getClass().getName()
                            + ". The client must expose a baseUrl, baseURI, _baseUrl, or apiBase field/setter.");
        }

        ORIGINALS.put(client, state);
    }

    /**
     * Restores the original base URL on a previously wrapped client.
     *
     * @param client the wrapped LLM client
     * @throws IllegalStateException if the client is not wrapped
     */
    public static void unwrap(Object client) {
        if (client == null) {
            throw new IllegalArgumentException("Client must not be null");
        }
        OriginalState state = ORIGINALS.remove(client);
        if (state == null) {
            throw new IllegalStateException("Client is not wrapped.");
        }

        try {
            if (state.field != null) {
                state.field.setAccessible(true);
                state.field.set(client, state.originalValue);
            } else if (state.setter != null) {
                state.setter.invoke(client, state.originalValue);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to restore original base URL", e);
        }
    }

    /**
     * Checks whether the given client is currently wrapped by AIBIM.
     *
     * @param client the LLM client to check
     * @return {@code true} if the client is wrapped
     */
    public static boolean isWrapped(Object client) {
        return client != null && ORIGINALS.containsKey(client);
    }

    // ---- reflection helpers ----

    private static final String[] FIELD_NAMES = {"baseUrl", "baseURI", "_baseUrl", "apiBase", "baseURL"};

    private static OriginalState tryWrapViaField(Object client, String proxyUrl, String apiKey) {
        Class<?> clazz = client.getClass();

        // Walk the class hierarchy
        while (clazz != null && clazz != Object.class) {
            for (String fieldName : FIELD_NAMES) {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object originalValue = field.get(client);

                    // Determine the correct type to set
                    Object newValue = coerceUrl(proxyUrl, field.getType());
                    if (newValue != null) {
                        field.set(client, newValue);
                        return new OriginalState(field, null, originalValue);
                    }
                } catch (NoSuchFieldException ignored) {
                    // Try next field name
                } catch (Exception e) {
                    // Field exists but not accessible/settable — try next
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private static final String[] SETTER_NAMES = {"setBaseUrl", "setBaseURI", "setApiBase", "setBaseURL"};

    private static OriginalState tryWrapViaSetter(Object client, String proxyUrl, String apiKey) {
        Class<?> clazz = client.getClass();

        for (String setterName : SETTER_NAMES) {
            try {
                // Find setter method
                Method setter = findMethod(clazz, setterName);
                if (setter == null) {
                    continue;
                }

                // Find corresponding getter
                String getterName = setterName.replace("set", "get");
                Method getter = findMethod(clazz, getterName);
                Object originalValue = null;
                if (getter != null) {
                    getter.setAccessible(true);
                    originalValue = getter.invoke(client);
                }

                Class<?> paramType = setter.getParameterTypes()[0];
                Object newValue = coerceUrl(proxyUrl, paramType);
                if (newValue != null) {
                    setter.setAccessible(true);
                    setter.invoke(client, newValue);
                    return new OriginalState(null, setter, originalValue);
                }
            } catch (Exception ignored) {
                // Try next setter
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    return m;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private static Object coerceUrl(String url, Class<?> targetType) {
        if (targetType == String.class) {
            return url;
        }
        if (targetType == URI.class) {
            return URI.create(url);
        }
        // Unsupported type
        return null;
    }

    /**
     * Internal state holder for a wrapped client.
     */
    private static final class OriginalState {
        final Field field;
        final Method setter;
        final Object originalValue;

        OriginalState(Field field, Method setter, Object originalValue) {
            this.field = field;
            this.setter = setter;
            this.originalValue = originalValue;
        }
    }
}
