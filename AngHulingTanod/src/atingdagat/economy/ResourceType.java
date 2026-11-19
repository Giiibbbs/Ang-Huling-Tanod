package atingdagat.economy;

import java.awt.Color;

/** The three things the island gives you. */
public enum ResourceType {
    FISH("Fish", new Color(120, 200, 235), "fish"),
    BAMBOO("Bamboo", new Color(120, 190, 90), "bamboo"),
    ORE("Ore", new Color(170, 170, 185), "ore");

    private final String label;
    private final Color color;
    private final String fileName;

    ResourceType(String label, Color color, String fileName) {
        this.label = label;
        this.color = color;
        this.fileName = fileName;
    }

    /** Art for the harvest spot on the map (16x16). */
    public String getSpritePath() {
        return "/atingdagat/res/nodes/" + fileName + ".png";
    }

    /** Small icon for the HUD (10x10). */
    public String getIconPath() {
        return "/atingdagat/res/ui/" + fileName + ".png";
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }
}
