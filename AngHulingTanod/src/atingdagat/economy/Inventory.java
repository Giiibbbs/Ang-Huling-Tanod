package atingdagat.economy;

/**
 * Inventory.java
 *
 * What the player is carrying. One number per ResourceType, kept in an array
 * indexed by the enum's position (FISH = 0, BAMBOO = 1, ORE = 2).
 */
public class Inventory {

    private final int[] amount = new int[ResourceType.values().length];

    public void add(ResourceType type, int n) {
        amount[type.ordinal()] += n;
    }

    public int get(ResourceType type) {
        return amount[type.ordinal()];
    }

    public boolean has(ResourceType type, int n) {
        return amount[type.ordinal()] >= n;
    }

    /** Takes n away if there is enough. Returns false (and takes nothing) if there is not. */
    public boolean spend(ResourceType type, int n) {
        if (!has(type, n)) {
            return false;
        }
        amount[type.ordinal()] -= n;
        return true;
    }
}
