package atingdagat.economy;

import atingdagat.core.Constants;
import atingdagat.map.TileMap;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * SupplyCrate.java
 *
 * A crate that floats in from the sea, bobbing, with a glowing beacon so it is easy to spot.
 * It drifts slowly toward the island and stops in the shallows. Cast a line and hold the mouse on it
 * to open it: it holds a little ore. It is the way the island gets ore that has run out, so the player
 * is never stuck. If nobody takes it, it drifts away after a while.
 *
 * Implements Harvestable, like a fish school or a resource spot, so the same "hold the mouse" code works.
 * What is inside (the ore and the amount) is decided by GameWorld when the crate is made.
 */
public class SupplyCrate implements Harvestable {

    private static final double CATCH_RADIUS = 13;
    private static final int LIFETIME = 60 * 45;           // 45 seconds
    private static final int HOLD_FRAMES = 70;             // quicker to open than an ore vein
    private static final double DRIFT_SPEED = 0.12;
    private static final String SPRITE_PATH = "/atingdagat/res/items/crate.png";

    private final BufferedImage sprite = Assets.get(SPRITE_PATH, 16, 16);

    private final ResourceType contents;
    private int units;
    private double x;
    private double y;
    private int age = 0;

    public SupplyCrate(ResourceType contents, int units, double x, double y) {
        this.contents = contents;
        this.units = units;
        this.x = x;
        this.y = y;
    }

    /** Drifts toward the island's centre until it reaches the shallows, then just bobs there. */
    public void update(TileMap map) {
        age++;
        int col = (int) (x / Constants.TILE_SIZE);
        int row = (int) (y / Constants.TILE_SIZE);
        if (map.isWater(row, col) && map.getDepth(row, col) <= 2) {
            return;   // arrived in the shallows
        }
        double dx = Constants.INTERNAL_WIDTH / 2.0 - x;
        double dy = Constants.INTERNAL_HEIGHT / 2.0 - y;
        double len = Math.max(1, Math.hypot(dx, dy));
        x += dx / len * DRIFT_SPEED;
        y += dy / len * DRIFT_SPEED;
    }

    public boolean isGone() {
        return units <= 0 || age >= LIFETIME;
    }

    @Override
    public boolean isNear(double px, double py) {
        return Math.hypot(px - x, py - y) <= CATCH_RADIUS;
    }

    @Override
    public ResourceType getType() {
        return contents;
    }

    @Override
    public int getHoldFrames() {
        return HOLD_FRAMES;
    }

    @Override
    public boolean isAvailable() {
        return units > 0 && age < LIFETIME;
    }

    /** Opening the crate gives everything inside at once. */
    @Override
    public int harvest() {
        int gained = units;
        units = 0;
        return gained;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    public void draw(Graphics2D g, int frame) {
        // Blinks during its last 6 seconds, so the player knows it is about to drift away
        if (age > LIFETIME - 360 && (frame / 8) % 2 == 0) {
            return;
        }
        double bob = Math.sin(age * 0.08) * 1.5;
        int cx = (int) x;
        int cy = (int) (y + bob);

        // The beacon: a soft pulsing glow
        float glow = (float) (10 + 3 * Math.sin(age * 0.12));
        g.setPaint(new RadialGradientPaint(new Point2D.Double(cx, cy), glow, new float[]{0f, 1f},
                new Color[]{new Color(255, 225, 120, 120), new Color(255, 225, 120, 0)}));
        g.fillOval((int) (cx - glow), (int) (cy - glow), (int) (glow * 2), (int) (glow * 2));

        // Little ripples on the water
        g.setColor(new Color(255, 255, 255, 70));
        g.drawOval(cx - 9, cy + 4, 18, 5);

        if (sprite != null) {
            g.drawImage(sprite, cx - 8, cy - 8, null);
            return;
        }
        g.setColor(new Color(140, 98, 56));                 // wooden box
        g.fillRect(cx - 6, cy - 5, 12, 10);
        g.setColor(new Color(96, 64, 34));                  // planks
        g.drawRect(cx - 6, cy - 5, 11, 9);
        g.drawLine(cx - 6, cy, cx + 5, cy);
        g.setColor(new Color(200, 205, 214));               // metal corners
        g.fillRect(cx - 6, cy - 5, 2, 2);
        g.fillRect(cx + 4, cy - 5, 2, 2);
        g.fillRect(cx - 6, cy + 3, 2, 2);
        g.fillRect(cx + 4, cy + 3, 2, 2);
        g.setColor(new Color(255, 225, 120));               // a yellow question mark: what is inside?
        g.fillRect(cx - 1, cy - 3, 2, 2);
        g.fillRect(cx, cy - 1, 1, 1);
        g.fillRect(cx, cy + 1, 1, 1);
    }
}
