package atingdagat.defense;

import atingdagat.core.Constants;
import atingdagat.entities.GlowSource;
import atingdagat.entities.Light;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Torch.java
 *
 * A bamboo torch stuck in the sand. It burns all night and keeps one patch of shore lit,
 * so ships that reach it can be seen and shot even when the beam is looking elsewhere.
 *
 * It is a Defense (the world keeps it in the same list as nets and mines) and also a Light
 * and a GlowSource (the night code treats it like any other lamp). One class, three promises.
 * It sits on LAND, so ships cannot attack it and it never blocks them.
 */
public class Torch implements Defense, Light, GlowSource, Durable {

    private static final String SPRITE_PATH = "/atingdagat/res/buildings/torch.png";
    private final BufferedImage sprite = Assets.get(SPRITE_PATH, 12, 16);

    private static final int MAX_DURABILITY = 3;
    private int durability = MAX_DURABILITY;

    private final int col;
    private final int row;
    private final double centerX;
    private final double centerY;

    public Torch(int col, int row) {
        this.col = col;
        this.row = row;
        this.centerX = col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
        this.centerY = row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
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
        return MAX_DURABILITY;
    }

    @Override
    public void restore(int amount) {
        durability = Math.min(MAX_DURABILITY, durability + amount);
    }

    @Override
    public void wear(int amount) {
        durability = Math.max(1, durability - amount);
    }

    @Override
    public int repairCostPerPoint() {
        return 2;
    }

    @Override
    public double getCenterX() {
        return centerX;
    }

    @Override
    public double getCenterY() {
        return centerY;
    }

    @Override
    public boolean lights(double x, double y) {
        return Math.hypot(x - centerX, y - (centerY - 8)) <= Constants.TORCH_RADIUS;
    }

    @Override
    public double getGlowX() {
        return centerX;
    }

    @Override
    public double getGlowY() {
        return centerY - 8;
    }

    @Override
    public double getGlowRadius() {
        return Constants.TORCH_RADIUS;
    }

    @Override
    public void draw(Graphics2D g, int frame) {
        int x = (int) centerX;
        int y = (int) centerY + 5;   // the bottom of the stick
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(x - 4, y - 2, 8, 4);
        if (sprite != null) {
            g.drawImage(sprite, x - 6, y - 15, null);
            drawWear(g, x, y + 3);
            return;
        }
        g.setColor(new Color(120, 84, 46));   // bamboo stick
        g.fillRect(x - 1, y - 11, 2, 11);
        int flicker = (frame / 5) % 3;        // the flame wobbles
        g.setColor(new Color(255, 120, 40));
        g.fillOval(x - 3, y - 17 - flicker / 2, 6, 7 + flicker % 2);
        g.setColor(new Color(255, 225, 120));
        g.fillOval(x - 1, y - 15 - flicker / 2, 3, 4);
        drawWear(g, x, y + 3);
    }
}
