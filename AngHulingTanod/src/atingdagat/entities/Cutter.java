package atingdagat.entities;

import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/**
 * Cutter.java
 *
 * A fast, fragile patrol boat. It is hard to track with a slow beam, but two
 * hits sink it. It extends Warship, so only speed, health, damage and looks are written here.
 */
public class Cutter extends Warship {

    private static final String SPRITE_PATH = "/atingdagat/res/ships/cutter.png";
    private static final int LENGTH = 40;
    private static final int WIDTH = 12;

    private final BufferedImage sprite = Assets.get(SPRITE_PATH, LENGTH, WIDTH);

    public Cutter(double x, double y, double targetX, double targetY) {
        super(x, y, targetX, targetY, 1.0, 2);
        landingDamage = 2;
    }

    @Override
    protected double getHitRadius() {
        return 15;
    }

    @Override
    public String getName() {
        return "Cutter";
    }

    @Override
    protected void drawShape(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, -LENGTH / 2, -WIDTH / 2, null);
            return;
        }
        Polygon hull = new Polygon(new int[]{-12, 6, 14, 6, -12}, new int[]{-4, -5, 0, 5, 4}, 5);
        g.setColor(new Color(70, 76, 90));
        g.fillPolygon(hull);
        g.setColor(new Color(14, 16, 22));
        g.drawPolygon(hull);
        g.setColor(new Color(190, 34, 48));   // crimson stripe
        g.fillRect(-9, -1, 12, 2);
    }
}
