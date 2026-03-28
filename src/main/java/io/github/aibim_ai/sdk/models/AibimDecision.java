package io.github.aibim_ai.sdk.models;

/**
 * Represents the security decision returned by the AIBIM proxy
 * via the {@code x-aibim-decision} response header.
 */
public enum AibimDecision {

    /** Request is safe and allowed through. */
    ALLOW("allow"),

    /** Request is suspicious but allowed with a warning. */
    WARN("warn"),

    /** Request is blocked due to detected threats. */
    BLOCK("block");

    private final String value;

    AibimDecision(String value) {
        this.value = value;
    }

    /**
     * Returns the wire-format string value.
     *
     * @return lowercase decision string
     */
    public String getValue() {
        return value;
    }

    /**
     * Parses a decision string from an HTTP header value.
     * Returns {@link #ALLOW} for unrecognized values.
     *
     * @param s the header value
     * @return the parsed decision
     */
    public static AibimDecision fromString(String s) {
        if (s == null) {
            return ALLOW;
        }
        for (AibimDecision d : values()) {
            if (d.value.equalsIgnoreCase(s.trim())) {
                return d;
            }
        }
        return ALLOW;
    }
}
