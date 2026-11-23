package atingdagat.core;

import atingdagat.economy.FishKind;
import atingdagat.economy.FishSchool;
import atingdagat.economy.Harvestable;
import atingdagat.economy.ResourceType;
import atingdagat.economy.SupplyCrate;
import atingdagat.map.TileMap;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * SeaLife.java
 *
 * Everything that floats or swims in the sea by day: the fish schools and the supply crates. It was part of
 * GameWorld until Nov 23. It creates them at random spots of the right depth, moves them, removes the ones that
 * swam or drifted away, draws them, and answers "is there something to harvest under the mouse?".
 */
public class SeaLife {

    private final TileMap map;
    private final Random random;
    private final ArrayList<FishSchool> fish = new ArrayList<>();
    private final ArrayList<SupplyCrate> crates = new ArrayList<>();
    private int fishTimer = 0;
    private int crateTimer = Constants.CRATE_INTERVAL - 6 * Constants.TARGET_FPS;   // the first crate comes after about 6 seconds

    public SeaLife(TileMap map, Random random) {
        this.map = map;
        this.random = random;
    }

    /** The sea starts with a few schools already swimming. */
    public void startWithFish(int count) {
        for (int i = 0; i < count; i++) {
            spawnFish();
        }
    }

    /** The fish go deep in the dark and any crate left floating drifts away. */
    public void clear() {
        fish.clear();
        crates.clear();
    }

    /**
     * One frame of daytime sea life. Returns whatever left the sea this frame (swam away or drifted off), so the
     * caller can stop harvesting it.
     *
     * @param fastCrates true when the radio is strong or a survivor boosts crates (they come more often)
     * @param oreLeft    how many units of an ore are still in the ground (used to pick what a crate holds)
     */
    public List<Harvestable> update(boolean fastCrates, ToIntFunction<ResourceType> oreLeft) {
        List<Harvestable> gone = new ArrayList<>();
        fishTimer++;
        if (fishTimer >= Constants.FISH_SPAWN_FRAMES && fish.size() < Constants.MAX_FISH_SCHOOLS) {
            fishTimer = 0;
            spawnFish();
        }
        for (int i = fish.size() - 1; i >= 0; i--) {
            fish.get(i).update(map);
            if (fish.get(i).isGone()) {
                gone.add(fish.remove(i));
            }
        }
        crateTimer++;
        int interval = fastCrates ? Constants.CRATE_INTERVAL_STRONG : Constants.CRATE_INTERVAL;
        if (crateTimer >= interval && crates.size() < Constants.MAX_CRATES) {
            crateTimer = 0;
            spawnCrate(oreLeft);
        }
        for (int i = crates.size() - 1; i >= 0; i--) {
            crates.get(i).update(map);
            if (crates.get(i).isGone()) {
                gone.add(crates.remove(i));
            }
        }
        return gone;
    }

    /** The first thing under the mouse that can be harvested: fish first, then crates. Null if none. */
    public Harvestable findHarvestable(double x, double y) {
        for (FishSchool school : fish) {
            if (school.isAvailable() && school.isNear(x, y)) {
                return school;
            }
        }
        for (SupplyCrate crate : crates) {
            if (crate.isAvailable() && crate.isNear(x, y)) {
                return crate;
            }
        }
        return null;
    }

    public void draw(Graphics2D g, int frame) {
        for (FishSchool school : fish) {
            school.draw(g, frame);
        }
        for (SupplyCrate crate : crates) {
            crate.draw(g, frame);
        }
    }

    public int getCrateCount() {
        return crates.size();
    }

    /** A crate appears at sea. What is inside favors the ores that have run out on the island. Returns it, or null. */
    public SupplyCrate spawnCrate(ToIntFunction<ResourceType> oreLeft) {
        ResourceType contents = pickCrateContents(oreLeft);
        int units = (contents == ResourceType.GOLD) ? 1 : 2;
        for (int attempt = 0; attempt < 80; attempt++) {
            int col = random.nextInt(Constants.MAP_COLS);
            int row = 2 + random.nextInt(Constants.MAP_ROWS - 4);
            if (!map.isWater(row, col) || map.getDepth(row, col) < 3 || map.getDepth(row, col) > 6) {
                continue;
            }
            double x = col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            double y = row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            if (y < 40 || y > Constants.INTERNAL_HEIGHT - 24) {
                continue;
            }
            SupplyCrate crate = new SupplyCrate(contents, units, x, y);
            crates.add(crate);
            return crate;
        }
        return null;
    }

    /** Nickel and copper are common, gold is rare. An ore that is gone from the map is 3 times as likely, so you are never stuck. */
    private ResourceType pickCrateContents(ToIntFunction<ResourceType> oreLeft) {
        ResourceType[] ores = {ResourceType.NICKEL, ResourceType.COPPER, ResourceType.CHROMITE, ResourceType.GOLD};
        double[] weight = {4, 4, 2, 1};
        double total = 0;
        for (int i = 0; i < ores.length; i++) {
            if (oreLeft.applyAsInt(ores[i]) == 0) {
                weight[i] *= 3;
            }
            total += weight[i];
        }
        double roll = random.nextDouble() * total;
        for (int i = 0; i < ores.length; i++) {
            roll -= weight[i];
            if (roll <= 0) {
                return ores[i];
            }
        }
        return ResourceType.NICKEL;
    }

    /** Picks a kind (rare ones less often), then looks for a spot at the right depth. */
    private void spawnFish() {
        FishKind kind = pickFishKind();
        for (int attempt = 0; attempt < 60; attempt++) {
            int col = random.nextInt(Constants.MAP_COLS);
            int row = 2 + random.nextInt(Constants.MAP_ROWS - 4);
            if (!map.isWater(row, col)) {
                continue;
            }
            int depth = map.getDepth(row, col);
            if (depth < kind.getMinDepth() || depth > kind.getMaxDepth()) {
                continue;
            }
            double x = col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            double y = row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            fish.add(new FishSchool(kind, x, y, random));
            return;
        }
    }

    /** A weighted random pick: tamban is common, lapu-lapu is rare. */
    private FishKind pickFishKind() {
        double total = 0;
        for (FishKind kind : FishKind.values()) {
            total += kind.getWeight();
        }
        double roll = random.nextDouble() * total;
        for (FishKind kind : FishKind.values()) {
            roll -= kind.getWeight();
            if (roll <= 0) {
                return kind;
            }
        }
        return FishKind.TAMBAN;
    }
}
