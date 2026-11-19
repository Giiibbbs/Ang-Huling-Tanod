package atingdagat.economy;

import java.awt.Color;

/** The three things the island gives you. */
public enum ResourceType {
    FISH("Fish", new Color(120, 200, 235)),
    BAMBOO("Bamboo", new Color(120, 190, 90)),
    ORE("Ore", new Color(170, 170, 185));

    private final String label;
    private final Color color;

    ResourceType(String label, Color color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }
}
