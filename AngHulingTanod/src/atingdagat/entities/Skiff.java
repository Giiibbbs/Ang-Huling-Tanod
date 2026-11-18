package atingdagat.entities;

import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/**
 * Skiff.java
 *
 * The first enemy: a small, fast scout boat. It extends Warship, so it only
 * defines its own speed, health, and appearance.
 *
 * If the art file exists (res/ships/skiff.png), the game draws it. If not, it
 * draws a simple placeholder shape. That means the game works with or without art.
 */
public class Skiff extends Warship {

    private static final String SPRITE_PATH = "/atingdagat/res/ships/skiff.png";
    private static final int LENGTH = 32;
    private static final int WIDTH = 16;

    private final BufferedImage sprite = Assets.get(SPRITE_PATH, LENGTH, WIDTH);

    public Skiff(double x, double y, double targetX, double targetY) {
        super(x, y, targetX, targetY, 0.55, 3);
    }

    @Override
    public String getName() {
        return "Skiff";
    }

    @Override
    protected void drawShape(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, -LENGTH / 2, -WIDTH / 2, null);
            return;
        }

        // Placeholder: a narrow dark hull pointing right.
        Polygon hull = new Polygon(
                new int[]{-14, 8, 15, 8, -14},
                new int[]{-5, -6, 0, 6, 5}, 5);
        g.setColor(new Color(44, 48, 58));
        g.fillPolygon(hull);
        g.setColor(new Color(14, 16, 22));
        g.drawPolygon(hull);

        g.setColor(new Color(160, 160, 170));  // light gun
        g.fillOval(1, -2, 4, 4);
        g.setColor(new Color(190, 34, 48));    // crimson pennant at the stern
        g.fillRect(-13, -1, 5, 2);
    }
}
