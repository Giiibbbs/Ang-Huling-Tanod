package atingdagat.defense;

import atingdagat.core.Constants;
import atingdagat.entities.Warship;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * PanaTower.java
 *
 * A bamboo tower with a bow ("pana" means arrow). It shoots the nearest ship in range every
 * second or so, and it does NOT need light: that is its whole point, since you can only shoot
 * by hand what the beam has found. It stands on land, so ships cannot attack it.
 * The fourth Defense. Like Mine it acts by itself in update().
 */
public class PanaTower implements Defense, Durable {

    private static final String SPRITE_PATH = "/atingdagat/res/buildings/pana.png";
    private final BufferedImage sprite = Assets.get(SPRITE_PATH, 14, 20);

    private static final int MAX_DURABILITY = 4;
    private int plating = 0;
    private int durability = MAX_DURABILITY;

    private final int col;
    private final int row;
    private int cooldown = 0;
    private int shotTimer = 0;
    private double shotX;
    private double shotY;

    public PanaTower(int col, int row) {
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
        if (cooldown > 0) {
            cooldown--;
        }
        if (shotTimer > 0) {
            shotTimer--;
        }
        if (cooldown > 0) {
            return;
        }
        Warship best = null;
        double bestDist = Constants.PANA_RANGE;
        for (Warship ship : ships) {
            double d = Math.hypot(ship.getX() - getX(), ship.getY() - getY());
            if (!ship.isDead() && d <= bestDist) {
                best = ship;
                bestDist = d;
            }
        }
        if (best != null) {
            best.hit(1);
            cooldown = Constants.PANA_COOLDOWN;
            shotTimer = 8;
            shotX = best.getX();
            shotY = best.getY();
        }
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
        durability -= amount;   // only a Raider's gun can reach it
    }

    @Override
    public boolean isDestroyed() {
        return durability <= 0;
    }

    @Override
    public int getDurability() {
        return durability;
    }

    @Override
    public int getMaxDurability() {
        return MAX_DURABILITY + plating;
    }

    @Override
    public void setPlating(int level) {
        if (level > plating) {
            durability += level - plating;
        }
        plating = level;
    }

    @Override
    public void restore(int amount) {
        durability = Math.min(MAX_DURABILITY + plating, durability + amount);
    }

    @Override
    public void wear(int amount) {
        durability = Math.max(1, durability - amount);
    }

    @Override
    public int repairCostPerPoint() {
        return 3;
    }

    @Override
    public double getCenterX() {
        return getX();
    }

    @Override
    public double getCenterY() {
        return getY();
    }

    @Override
    public void draw(Graphics2D g, int frame) {
        int x = (int) getX();
        int y = (int) getY() + 6;
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(x - 5, y - 2, 10, 4);
        if (sprite != null) {
            g.drawImage(sprite, x - 7, y - 19, null);
            drawWear(g, x, y + 3);
        } else {
            g.setColor(new Color(120, 84, 46));          // four legs
            g.fillRect(x - 4, y - 12, 1, 12);
            g.fillRect(x + 3, y - 12, 1, 12);
            g.drawLine(x - 4, y - 4, x + 3, y - 8);      // a cross brace
            g.setColor(new Color(160, 120, 70));         // platform
            g.fillRect(x - 6, y - 14, 12, 3);
            g.setColor(new Color(90, 62, 34));
            g.drawRect(x - 6, y - 14, 11, 2);
            g.setColor(new Color(200, 190, 150));        // the bow
            g.drawArc(x - 3, y - 20, 6, 7, 270, 180);
            g.drawLine(x, y - 20, x, y - 14);
            drawWear(g, x, y + 3);
        }
        if (shotTimer > 0) {
            g.setColor(new Color(255, 245, 200, 30 * shotTimer));
            g.drawLine(x, y - 14, (int) shotX, (int) shotY);
        }
    }
}
