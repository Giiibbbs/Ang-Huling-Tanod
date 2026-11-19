package atingdagat.economy;

/**
 * Inventory.java
 *
 * What the player is carrying. One number per ResourceType, kept in an array
 * indexed by the enum's position (ordinal). Adding a new ResourceType needs no change here.
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

    /** All the fish you carry, counted in meals (a tuna is worth 4 tambans). */
    public int getMeals() {
        int total = 0;
        for (ResourceType type : ResourceType.values()) {
            total += amount[type.ordinal()] * type.getMeals();
        }
        return total;
    }

    /**
     * Uses up fish worth at least n meals, cheapest fish first, so the big catches are kept for later.
     * Returns false (and takes nothing) if you do not have enough.
     */
    public boolean spendMeals(int n) {
        if (getMeals() < n) {
            return false;
        }
        int needed = n;
        // Go through the fish from the lowest meal value to the highest.
        for (int value = 1; needed > 0 && value <= 8; value++) {
            for (ResourceType type : ResourceType.values()) {
                while (needed > 0 && type.getMeals() == value && amount[type.ordinal()] > 0) {
                    amount[type.ordinal()]--;
                    needed -= value;
                }
            }
        }
        return true;
    }
}
