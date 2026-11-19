package atingdagat.defense;

import atingdagat.entities.Warship;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Defense.java
 *
 * An interface is a promise: "anything that calls itself a Defense can do
 * these things". The world keeps one list of Defenses and does not care
 * whether an item is a Net, a mine, or a gun. Net is the first one. Others come later.
 */
public interface Defense {

    /** Does this defense fill the tile (col, row)? */
    boolean occupies(int col, int row);

    /** True if enemy ships cannot sail through it (they stop and attack it). */
    boolean blocksShips();

    /** Something hit it. */
    void damage(int amount);

    boolean isDestroyed();

    /** Called every frame. Passive defenses (like a net) do nothing. A mine looks for ships here. */
    default void update(List<Warship> ships) {
    }

    /** Draws it. */
    void draw(Graphics2D g, int frame);
}
