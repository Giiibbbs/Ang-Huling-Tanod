package atingdagat.core;

import atingdagat.entities.Barge;
import atingdagat.entities.Cutter;
import atingdagat.entities.Ghost;
import atingdagat.entities.Raider;
import atingdagat.entities.Skiff;
import atingdagat.entities.Warship;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * ShipSpawner.java
 *
 * Decides WHEN and WHERE the Kordan ships appear each night. It was part of GameWorld until Nov 23; it is its
 * own class now so GameWorld only has to say "start the night" and "update".
 *
 * Every night it builds a queue of ship kinds from the WavePlan (shuffled), and then sends them in "pulses":
 * a few ships at once from different sides, with a gap between pulses. A Twist can make ships stronger.
 */
public class ShipSpawner {

    private final WavePlan plan;
    private final Random random;
    private final ArrayList<WavePlan.Kind> queue = new ArrayList<>();   // tonight's ships, in the order they come
    private int spawned = 0;                                            // ships sent so far tonight
    private int spawnTimer = 0;

    public ShipSpawner(WavePlan plan, Random random) {
        this.plan = plan;
        this.random = random;
    }

    /** Builds tonight's queue and sets the timer so the first ships come in about 1.5 seconds. */
    public void startNight(int night) {
        queue.clear();
        for (WavePlan.Kind kind : WavePlan.Kind.values()) {
            for (int i = 0; i < plan.getCount(night, kind); i++) {
                queue.add(kind);
            }
        }
        Collections.shuffle(queue, random);
        spawned = 0;
        spawnTimer = pulseInterval(night) - 90;
    }

    /** Called once per frame at night: sends the next pulse when its time has come. */
    public void update(int night, double postX, double postY, List<Warship> ships) {
        spawnTimer++;
        if (spawned < queue.size() && spawnTimer >= pulseInterval(night)) {
            spawnTimer = 0;
            ArrayList<Integer> sides = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
            Collections.shuffle(sides, random);   // a pulse comes from different sides
            for (int k = 0; k < pulseSize(night) && spawned < queue.size(); k++) {
                ships.add(makeShip(night, queue.get(spawned), sides.get(k), postX, postY));
                spawned++;
            }
        }
    }

    /** Every ship of tonight has been sent. */
    public boolean allSpawned() {
        return spawned >= queue.size();
    }

    /** Has the queue been built for tonight? */
    public boolean hasQueue() {
        return !queue.isEmpty();
    }

    /** Has at least this fraction of tonight's ships been sent? (The small Bakunawa visit starts at one half.) */
    public boolean halfSpawned() {
        return spawned >= queue.size() / 2;
    }

    public int getTotal() {
        return queue.size();
    }

    public int getRemaining() {
        return Math.max(0, queue.size() - spawned);
    }

    /** How many ships arrive together in one pulse tonight. Bigger every few nights. */
    private int pulseSize(int night) {
        int[] base = {1, 1, 2, 1, 1, 3, 4, 4};   // index = night (0 unused). Nights 3 and 4 trickle one ship at a time so the pressure never lets up
        int size = base[Math.max(1, Math.min(Constants.TOTAL_NIGHTS, night))];
        return Math.min(4, Twist.forNight(night) == Twist.ASSAULT ? size + 1 : size);   // only 4 sides to come from
    }

    /** Frames between pulses. A pulse of 3 waits 3 times as long as one ship would, so the average pace matches waves.txt. */
    private int pulseInterval(int night) {
        double factor = Twist.forNight(night) == Twist.ASSAULT ? 0.8 : 1.0;
        return (int) (plan.getInterval(night) * pulseSize(night) * factor);
    }

    /** A ship appears just outside the screen, in a lane that points at the post. */
    private Warship makeShip(int night, WavePlan.Kind kind, int side, double postX, double postY) {
        double band = 110;   // how far from the post's line the ship may start
        double x;
        double y;
        switch (side) {
            case 0:  // left
                x = -12;
                y = laneY(postY, band);
                break;
            case 1:  // right
                x = Constants.INTERNAL_WIDTH + 12;
                y = laneY(postY, band);
                break;
            case 2:  // top
                x = laneX(postX, band);
                y = -12;
                break;
            default: // bottom
                x = laneX(postX, band);
                y = Constants.INTERNAL_HEIGHT + 12;
                break;
        }
        Warship ship;
        switch (kind) {
            case CUTTER:
                ship = new Cutter(x, y, postX, postY);
                break;
            case BARGE:
                ship = new Barge(x, y, postX, postY);
                break;
            case RAIDER:
                ship = new Raider(x, y, postX, postY);
                break;
            case GHOST:
                ship = new Ghost(x, y, postX, postY);
                break;
            default:
                ship = new Skiff(x, y, postX, postY);
                break;
        }
        switch (Twist.forNight(night)) {   // tonight's rule
            case SURGE:
                ship.powerUp(1.25, 0);
                break;
            case HARDENED:
                ship.powerUp(1.0, 1);
                break;
            case ASSAULT:
                ship.powerUp(1.1, 0);
                break;
            default:
                break;
        }
        return ship;
    }

    private double laneX(double postX, double band) {
        double v = postX + (random.nextDouble() * 2 - 1) * band;
        return Math.max(20, Math.min(Constants.INTERNAL_WIDTH - 20, v));
    }

    private double laneY(double postY, double band) {
        double v = postY + (random.nextDouble() * 2 - 1) * band;
        return Math.max(20, Math.min(Constants.INTERNAL_HEIGHT - 20, v));
    }
}
