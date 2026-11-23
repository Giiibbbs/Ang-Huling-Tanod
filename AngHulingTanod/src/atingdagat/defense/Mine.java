package atingdagat.defense;

import atingdagat.core.Constants;
import atingdagat.entities.Warship;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Mine.java
 *
 * A floating mine buoy. Ships do not stop for it (it does not block). But when one
 * sails close, it explodes and hurts every ship nearby, then it is gone.
 * It is the second Defense, and it shows why Defense is an interface: Net waits to be
 * attacked, while Mine acts by itself in update().
 */
public class Mine implements Defense {

    public static final int COST_NICKEL = Constants.MINE_COST_NICKEL;
    private static final double TRIGGER_RADIUS = 18;
    private static final double BLAST_RADIUS = 38;
    public static final int BLAST_DAMAGE = 4;
    private static final String SPRITE_PATH = "/atingdagat/res/buildings/mine.png";

    private final int col;
    private final int row;
    private boolean exploded = false;
    private final BufferedImage sprite = Assets.get(SPRITE_PATH, 16, 16);

    public Mine(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public double getX() {
        return col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    public double getY() {
        return row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    @Override
    public void update(List<Warship> ships) {
        if (exploded) {
            return;
        }
        boolean triggered = false;
        for (Warship ship : ships) {
            if (distanceTo(ship) <= TRIGGER_RADIUS) {
                triggered = true;
            }
        }
        if (triggered) {
            detonate(ships);
        }
    }

    /** Goes off now: every ship in the blast takes damage. Also used by a neighbour's chain reaction. */
    public void detonate(List<Warship> ships) {
        if (exploded) {
            return;
        }
        exploded = true;
        for (Warship ship : ships) {
            if (distanceTo(ship) <= BLAST_RADIUS) {
                ship.hit(BLAST_DAMAGE);
            }
        }
    }

    /** Is another mine close enough to be set off by this one's blast? */
    public boolean reaches(Mine other) {
        return Math.hypot(other.getX() - getX(), other.getY() - getY()) <= BLAST_RADIUS;
    }

    private double distanceTo(Warship ship) {
        return Math.hypot(ship.getX() - getX(), ship.getY() - getY());
    }

    public boolean hasExploded() {
        return exploded;
    }

    @Override
    public boolean occupies(int c, int r) {
        return c == col && r == row;
    }

    @Override
    public boolean blocksShips() {
        return false;
    }

    @Override
    public void damage(int amount) {
        // Ships do not attack mines.
    }

    @Override
    public boolean isDestroyed() {
        return exploded;
    }

    @Override
    public void draw(Graphics2D g, int frame) {
        int x = (int) getX();
        int y = (int) getY() + (int) Math.round(Math.sin(frame * 0.06 + col) * 1);
        if (sprite != null) {
            g.drawImage(sprite, x - 8, y - 8, null);
            return;
        }
        g.setColor(new Color(40, 44, 52));
        g.fillOval(x - 5, y - 4, 10, 10);
        g.setColor(new Color(10, 12, 16));
        g.drawOval(x - 5, y - 4, 10, 10);
        g.setColor(new Color(130, 130, 140));        // spikes
        g.drawLine(x, y - 7, x, y - 4);
        g.drawLine(x - 7, y + 1, x - 5, y + 1);
        g.drawLine(x + 5, y + 1, x + 7, y + 1);
        g.setColor((frame / 30) % 2 == 0 ? new Color(255, 70, 60) : new Color(120, 30, 30));
        g.fillOval(x - 1, y - 9, 3, 3);              // blinking light
    }
}
