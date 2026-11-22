package atingdagat.economy;

/**
 * Upgrades.java
 *
 * The tanod's four tools and how good each one is (level 0 to 3). Buying a level costs
 * bamboo (every level) and a little ore (levels 2 and 3). The effects are just numbers
 * that GameWorld asks for: "how long does this harvest take?", "how fast do I walk?".
 *
 * Gear is an enum: each tool's fixed facts (name, key, which ore it needs) sit in one
 * place. The levels are the only thing that changes, and they live in this class.
 */
public class Upgrades {

    public static final int MAX_LEVEL = 3;

    /** Bamboo needed for level 1, 2 and 3. */
    private static final int[] BAMBOO_COST = {4, 6, 8};
    /** Ore needed for level 1, 2 and 3. */
    private static final int[] ORE_COST = {0, 1, 1};

    /** Every level makes mining and chopping this much quicker (0.18 means 18% less time). */
    private static final double WORK_SAVING = 0.18;
    private static final double CAST_SAVING = 0.15;
    private static final double BOOT_BONUS = 0.15;
    private static final double ROD_REACH_BONUS = 15;
    private static final double RIFLE_RANGE_BONUS = 70;

    public enum Gear {
        //      key  label         ore                      what it does
        PICKAXE("5", "Pickaxe", ResourceType.NICKEL, "Mine ore faster: about 18% quicker per level."),
        AXE    ("6", "Axe",     ResourceType.COPPER, "Chop trees and bamboo faster: about 18% quicker per level."),
        BOOTS  ("7", "Boots",   ResourceType.COPPER, "Walk faster: about 15% per level."),
        ROD    ("8", "Rod", ResourceType.NICKEL, "Cast faster and reach further out to sea."),
        RIFLE  ("9", "Rifle",   ResourceType.CHROMITE, "Shoot lit ships from further away: 70 px more per level.");

        private final String key;
        private final String label;
        private final ResourceType ore;
        private final String description;

        Gear(String key, String label, ResourceType ore, String description) {
            this.key = key;
            this.label = label;
            this.ore = ore;
            this.description = description;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }

        public ResourceType getOre() {
            return ore;
        }

        public String getDescription() {
            return description;
        }
    }

    private final int[] level = new int[Gear.values().length];

    public int getLevel(Gear gear) {
        return level[gear.ordinal()];
    }

    public boolean isMaxed(Gear gear) {
        return getLevel(gear) >= MAX_LEVEL;
    }

    /** Bamboo for the next level (0 when maxed). */
    public int bambooCost(Gear gear) {
        return isMaxed(gear) ? 0 : BAMBOO_COST[getLevel(gear)];
    }

    /** Ore for the next level (0 when maxed, or when the level needs none). */
    public int oreCost(Gear gear) {
        return isMaxed(gear) ? 0 : ORE_COST[getLevel(gear)];
    }

    public boolean canAfford(Gear gear, Inventory inventory) {
        return !isMaxed(gear) && inventory.has(ResourceType.BAMBOO, bambooCost(gear))
                && inventory.has(gear.getOre(), oreCost(gear));
    }

    /** Pays and raises the level. Returns false (and takes nothing) if it cannot be bought. */
    public boolean buy(Gear gear, Inventory inventory) {
        if (!canAfford(gear, inventory)) {
            return false;
        }
        inventory.spend(ResourceType.BAMBOO, bambooCost(gear));
        inventory.spend(gear.getOre(), oreCost(gear));
        level[gear.ordinal()]++;
        return true;
    }

    /** Multiply a harvest time by this. Below 1 means quicker. */
    public double holdMultiplier(Harvestable target) {
        if (target instanceof FishSchool || target instanceof SupplyCrate) {
            return 1.0 - CAST_SAVING * getLevel(Gear.ROD);
        }
        if (target.getType().getCategory() == ResourceType.Category.ORE) {
            return 1.0 - WORK_SAVING * getLevel(Gear.PICKAXE);
        }
        return 1.0 - WORK_SAVING * getLevel(Gear.AXE);
    }

    /** Multiply walking speed by this. */
    public double speedMultiplier() {
        return 1.0 + BOOT_BONUS * getLevel(Gear.BOOTS);
    }

    /** Extra pixels a line can be cast. */
    public double extraFishReach() {
        return ROD_REACH_BONUS * getLevel(Gear.ROD);
    }

    /** Extra pixels the gun can shoot. */
    public double extraGunRange() {
        return RIFLE_RANGE_BONUS * getLevel(Gear.RIFLE);
    }
}
