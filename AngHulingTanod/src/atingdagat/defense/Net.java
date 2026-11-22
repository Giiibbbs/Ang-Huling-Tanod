package atingdagat.defense;

import atingdagat.core.Constants;
import java.awt.Color;
import atingdagat.util.Assets;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Net.java
 *
 * A bamboo-and-rope net stretched across one water tile. Ships cannot sail
 * through it: they stop and cut it, which buys time. It is cheap, so you build many.
 */
public class Net implements Defense, Durable {

    public static final int COST_BAMBOO = 3;
    private static final int MAX_HEALTH = 4;

    private static final String SPRITE_PATH = "/atingdagat/res/buildings/net.png";
    private final BufferedImage sprite = Assets.get(SPRITE_PATH, Constants.TILE_SIZE, Constants.TILE_SIZE);

    private final int col;
    private final int row;
    private int health = MAX_HEALTH;

    public Net(int col, int row) {
        this.col = col;
        this.row = row;
    }

    @Override
    public boolean occupies(int c, int r) {
        return c == col && r == row;
    }

    @Override
    public boolean blocksShips() {
        return true;
    }

    @Override
    public void damage(int amount) {
        health -= amount;
    }

    @Override
    public boolean isDestroyed() {
        return health <= 0;
    }

    @Override
    public int getDurability() {
        return health;
    }

    @Override
    public int getMaxDurability() {
        return MAX_HEALTH;
    }

    @Override
    public void restore(int amount) {
        health = Math.min(MAX_HEALTH, health + amount);
    }

    @Override
    public void wear(int amount) {
        health = Math.max(1, health - amount);
    }

    @Override
    public int repairCostPerPoint() {
        return 2;
    }

    @Override
    public double getCenterX() {
        return col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    @Override
    public double getCenterY() {
        return row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    @Override
    public void draw(Graphics2D g, int frame) {
        int x = col * Constants.TILE_SIZE;
        int y = row * Constants.TILE_SIZE;
        int s = Constants.TILE_SIZE;
        if (sprite != null) {   // a damaged net fades, so the player sees it weakening
            java.awt.Composite old = g.getComposite();
            float alpha = 0.45f + 0.55f * Math.max(0, health) / MAX_HEALTH;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.drawImage(sprite, x, y, null);
            g.setComposite(old);
            return;
        }
        int sway = (int) Math.round(Math.sin(frame * 0.05 + col + row) * 1);

        // Rope mesh: fewer lines when it is damaged, so the player can see it weakening.
        g.setColor(new Color(235, 220, 170, 200));
        int lines = Math.max(1, health);
        for (int i = 0; i < lines; i++) {
            int offset = 2 + i * (s - 4) / Math.max(1, MAX_HEALTH - 1);
            g.drawLine(x + 1, y + offset + sway, x + s - 1, y + offset - sway);
            g.drawLine(x + offset, y + 1, x + offset + sway, y + s - 1);
        }

        // Two bamboo posts
        g.setColor(new Color(150, 120, 60));
        g.fillRect(x, y + 1, 2, s - 2);
        g.fillRect(x + s - 2, y + 1, 2, s - 2);
        g.setColor(new Color(90, 70, 36));
        g.drawRect(x, y + 1, 1, s - 3);
        g.drawRect(x + s - 2, y + 1, 1, s - 3);
    }
}
