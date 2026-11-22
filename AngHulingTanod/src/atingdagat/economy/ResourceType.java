package atingdagat.economy;

import java.awt.Color;

/**
 * ResourceType.java
 *
 * Every kind of thing the player can collect. Each constant is ONE LINE of data:
 * its name, group, color, art name, how long you must hold the mouse on it (in frames),
 * how many you get per harvest, how many "meals" it is worth (fish only), and how fast
 * it grows back (0 = never).
 *
 * Because the differences between these are just numbers, they live here as data and
 * not as an if-else chain. To add a new ore, add one line below and give it a use.
 */
public enum ResourceType {

    //        label       group           color                       art name     hold  yield meals regrow
    BAMBOO   ("Bamboo",    Category.WOOD, new Color(120, 190, 90),    "bamboo",     90,   2,    0,    1500),

    // Filipino trees. Narra and acacia (rain tree) give timber (bamboo). Mango, coconut and banana give fruit, which counts as a meal.
    TREE     ("Narra",     Category.WOOD, new Color(70, 140, 70),     "tree",      110,   3,    0,    2400),
    ACACIA   ("Acacia",    Category.WOOD, new Color(90, 150, 70),     "acacia",    100,   3,    0,    2400),
    MANGO    ("Mango",     Category.FOOD, new Color(240, 170, 50),    "mango",      90,   2,    0,    2400),
    COCONUT  ("Coconut",   Category.FOOD, new Color(150, 110, 70),    "coconut",   100,   2,    0,    2400),
    BANANA   ("Banana",    Category.FOOD, new Color(235, 215, 80),    "banana",     70,   3,    0,    2000),
    FRUIT    ("Fruit",     Category.FOOD, new Color(240, 170, 50),    "fruit",       0,   1,    1,    0),
    NICKEL   ("Nickel",    Category.ORE,  new Color(150, 195, 175),   "nickel",    150,   1,    0,    0),
    COPPER   ("Copper",    Category.ORE,  new Color(210, 125, 75),    "copper",    150,   1,    0,    0),
    CHROMITE ("Chromite",  Category.ORE,  new Color(115, 125, 150),   "chromite",  180,   1,    0,    0),
    GOLD     ("Gold",      Category.ORE,  new Color(245, 205, 70),    "gold",      240,   1,    0,    0),

    TAMBAN   ("Tamban",    Category.FISH, new Color(200, 215, 225),   "tamban",     40,   1,    1,    0),
    GALUNGGONG("Galunggong", Category.FISH, new Color(150, 180, 215), "galunggong", 60,   1,    2,    0),
    BANGUS   ("Bangus",    Category.FISH, new Color(228, 232, 236),   "bangus",     60,   1,    2,    0),
    TUNA     ("Tuna",      Category.FISH, new Color(70, 100, 165),    "tuna",      100,   1,    4,    0),
    LAPULAPU ("Lapu-lapu", Category.FISH, new Color(235, 110, 70),    "lapulapu",  140,   1,    8,    0);

    /** The big groups. The HUD and the harvest rules treat each group a bit differently. */
    public enum Category { WOOD, ORE, FISH, FOOD }

    private final String label;
    private final Category category;
    private final Color color;
    private final String fileName;
    private final int holdFrames;
    private final int yield;
    private final int meals;
    private final int regrowFrames;

    ResourceType(String label, Category category, Color color, String fileName,
                 int holdFrames, int yield, int meals, int regrowFrames) {
        this.label = label;
        this.category = category;
        this.color = color;
        this.fileName = fileName;
        this.holdFrames = holdFrames;
        this.yield = yield;
        this.meals = meals;
        this.regrowFrames = regrowFrames;
    }

    public String getLabel() {
        return label;
    }

    /**
     * What goes into the bag when you collect this. A tree gives bamboo (timber, same as a bamboo grove),
     * so the player has one building material, not two. Everything else gives itself.
     */
    public ResourceType getStock() {
        switch (this) {
            case TREE:
            case ACACIA:
                return BAMBOO;
            case MANGO:
            case COCONUT:
            case BANANA:
                return FRUIT;
            default:
                return this;
        }
    }

    /** Is this a tree on the map (timber or fruit)? Trees are taller than a tile and chopped from the leaves. */
    public boolean isTree() {
        return this == TREE || this == ACACIA || this == MANGO || this == COCONUT || this == BANANA;
    }

    public Category getCategory() {
        return category;
    }

    public Color getColor() {
        return color;
    }

    public int getHoldFrames() {
        return holdFrames;
    }

    /** How many you get from one harvest. */
    public int getYield() {
        return yield;
    }

    /** How much this fish counts toward repairs. */
    public int getMeals() {
        return meals;
    }

    /** Frames until one unit grows back at a spot. 0 means it never does. */
    public int getRegrowFrames() {
        return regrowFrames;
    }

    /** Art for the harvest spot on the map (16x16). */
    public String getSpritePath() {
        return "/atingdagat/res/nodes/" + fileName + ".png";
    }

    /** Small icon for the HUD (10x10). */
    public String getIconPath() {
        return "/atingdagat/res/ui/" + fileName + ".png";
    }

    /** Art for a fish at sea. */
    public String getFishPath() {
        return "/atingdagat/res/fish/" + fileName + ".png";
    }
}
