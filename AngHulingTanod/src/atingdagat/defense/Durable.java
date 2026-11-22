package atingdagat.defense;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Durable.java
 *
 * A defense that wears out. Nets, torches and Pana towers lose 1 point of durability every night
 * (wear never destroys them: it stops at 1) and lose more when a ship hits them. The player mends
 * them by holding X next to them, which costs bamboo. Mines are one-shot, so they are not Durable.
 *
 * An interface, because a net, a torch and a tower are different classes with the same need.
 */
public interface Durable {

    int getDurability();

    int getMaxDurability();

    /** Adds durability back (never above the maximum). */
    void restore(int amount);

    /** The nightly wear: takes points off but never destroys the defense. */
    void wear(int amount);

    /** Bamboo needed to mend ONE point. */
    int repairCostPerPoint();

    double getCenterX();

    double getCenterY();

    default boolean isDamaged() {
        return getDurability() < getMaxDurability();
    }

    /** A little row of pips above a damaged defense, so the player can see what needs mending. */
    default void drawWear(Graphics2D g, int x, int y) {
        if (!isDamaged()) {
            return;
        }
        int max = getMaxDurability();
        int cur = Math.max(0, getDurability());
        int w = max * 4;
        int left = x - w / 2;
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(left - 1, y - 1, w + 1, 4);
        for (int i = 0; i < max; i++) {
            boolean full = i < cur;
            g.setColor(full ? (cur <= 1 ? new Color(255, 90, 80) : new Color(120, 220, 120)) : new Color(70, 70, 70));
            g.fillRect(left + i * 4, y, 3, 2);
        }
    }
}
