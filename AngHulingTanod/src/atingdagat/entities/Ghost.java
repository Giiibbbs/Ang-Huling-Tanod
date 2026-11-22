package atingdagat.entities;

import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/**
 * Ghost.java
 *
 * A dark-hulled Kordan ship painted to vanish at night. Normal ships stay visible for about a
 * second after the beam touches them. A Ghost needs the light on it for several frames before it
 * shows, and slips back into the dark almost at once. The player must keep the beam (or a flare)
 * on it to shoot it. Everything else (health, speed, damage) is ordinary.
 *
 * The only thing that differs is two numbers from the base class (litRise and litFall).
 */
public class Ghost extends Warship {

    private static final String SPRITE_PATH = "/atingdagat/res/ships/ghost.png";
    private static final int LENGTH = 38;
    private static final int WIDTH = 12;

    private final BufferedImage sprite = Assets.get(SPRITE_PATH, LENGTH, WIDTH);

    public Ghost(double x, double y, double targetX, double targetY) {
        super(x, y, targetX, targetY, 0.5, 3);
        this.litRise = 0.1;    // slow to show (about 3 frames of light)
        this.litFall = 0.07;   // quick to vanish (about half a second)
        this.landingDamage = 3;
    }

    @Override
    protected double getHitRadius() {
        return 15;
    }

    @Override
    public String getName() {
        return "Ghost";
    }

    @Override
    protected void drawShape(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, -LENGTH / 2, -WIDTH / 2, null);
            return;
        }
        Polygon hull = new Polygon(
                new int[]{-15, 8, 16, 8, -15},
                new int[]{-5, -6, 0, 6, 5}, 5);
        g.setColor(new Color(150, 170, 200));       // pale, cold hull
        g.fillPolygon(hull);
        g.setColor(new Color(70, 90, 130));
        g.drawPolygon(hull);
        g.setColor(new Color(120, 255, 240));       // two glowing eyes at the bow
        g.fillOval(7, -3, 3, 2);
        g.fillOval(7, 1, 3, 2);
        g.setColor(new Color(210, 225, 245));
        g.fillRect(-13, -1, 5, 2);
    }
}
