package atingdagat.defense;

import atingdagat.core.Constants;
import atingdagat.entities.GlowSource;
import atingdagat.entities.Light;
import java.awt.Color;
import java.awt.Graphics2D;

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


    private static final int MAX_DURABILITY = 3;
    private int plating = 0;
    private double radiusScale = 1.0;   // copper wiring makes torches glow wider
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
        return MAX_DURABILITY + plating;
    }

    @Override
    public void setPlating(int level) {
        if (level > plating) {
            durability += level - plating;
        }
        plating = level;
    }

    public void setRadiusScale(double scale) {
        radiusScale = scale;
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
        return Math.hypot(x - centerX, y - (centerY - 8)) <= Constants.TORCH_RADIUS * radiusScale;
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
        return Constants.TORCH_RADIUS * radiusScale;
    }

    /** The parol colours (red, yellow, green, blue): each lantern on the shore gets its own by its tile. */
    private static final Color[] PAROL_COLORS = {
        new Color(235, 60, 55), new Color(250, 200, 50), new Color(70, 190, 90), new Color(70, 140, 235)
    };

    /**
     * Drawn as a parol, the star lantern of a Filipino Christmas: a bamboo pole, a five-pointed star and two
     * little tails, and it twinkles. (The class is still called Torch because it is the same Defense and Light.)
     */
    @Override
    public void draw(Graphics2D g, int frame) {
        int x = (int) centerX;
        int y = (int) centerY + 5;   // the bottom of the pole
        Color body = PAROL_COLORS[(col + row) % PAROL_COLORS.length];
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(x - 4, y - 2, 8, 4);
        g.setColor(new Color(120, 84, 46));   // bamboo pole
        g.fillRect(x - 1, y - 9, 2, 9);
        int cy = y - 14;                      // centre of the star
        double twinkle = 0.85 + 0.15 * Math.sin(frame * 0.12 + col);
        int[] px = new int[10];
        int[] py = new int[10];
        for (int i = 0; i < 10; i++) {        // alternate long and short points to make a star
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0 ? 7.0 : 3.2);
            px[i] = x + (int) Math.round(Math.cos(ang) * r);
            py[i] = cy + (int) Math.round(Math.sin(ang) * r);
        }
        g.setColor(new Color(255, 245, 190, 70));
        g.fillOval(x - 9, cy - 9, 18, 18);    // soft glow around it
        g.setColor(new Color((int) (body.getRed() * twinkle), (int) (body.getGreen() * twinkle), (int) (body.getBlue() * twinkle)));
        g.fillPolygon(px, py, 10);
        g.setColor(body.darker().darker());
        g.drawPolygon(px, py, 10);
        g.setColor(new Color(255, 250, 215));
        g.fillOval(x - 1, cy - 1, 3, 3);      // the bulb inside
        g.setColor(body);                     // the tails
        g.drawLine(x - 2, cy + 6, x - 3, cy + 9);
        g.drawLine(x + 2, cy + 6, x + 3, cy + 9);
        drawWear(g, x, y + 3);
    }
}
