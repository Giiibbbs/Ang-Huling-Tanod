package atingdagat.core;

/**
 * Phase.java
 *
 * The four parts of one day. An enum is a fixed list of names, so the code
 * can say Phase.NIGHT instead of a mystery number.
 */
public enum Phase {
    DAY("DAY"),
    DUSK("DUSK"),
    NIGHT("NIGHT"),
    DAWN("DAWN");

    private final String label;

    Phase(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
