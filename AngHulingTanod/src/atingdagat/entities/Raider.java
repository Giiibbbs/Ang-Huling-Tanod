package atingdagat.entities;

import atingdagat.defense.Defense;
import atingdagat.defense.Durable;
import atingdagat.defense.PanaTower;
import atingdagat.defense.Torch;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Raider.java
 *
 * A Kordan gun boat that hunts your towers. When a Pana tower or a torch is within gun range it
 * stops and shoots it (1 durability every 1.7 seconds) until it is destroyed, and only then sails
 * on toward the post. Nets and mines do not interest it. This gives the player a reason to protect
 * and repair defenses, not only to sink ships.
 *
 * It overrides the hook act(): returning true means "I used this frame, do not sail".
 */
public class Raider extends Warship {

    private static final String SPRITE_PATH = "/atingdagat/res/ships/raider.png";
    private static final int LENGTH = 40;
    private static final int WIDTH = 14;
    private static final double RANGE = 85;       // how far its gun reaches
    private static final int FIRE_INTERVAL = 100; // frames between shots

    private final BufferedImage sprite = Assets.get(SPRITE_PATH, LENGTH, WIDTH);
    private final double baseHeading;             // the course toward the post, to return to
    private int fireTimer = FIRE_INTERVAL / 2;
    private double[] shot = null;                 // {fromX, fromY, toX, toY} for one frame

    public Raider(double x, double y, double targetX, double targetY) {
        super(x, y, targetX, targetY, 0.6, 4);
        this.baseHeading = heading;
        this.landingDamage = 3;
    }

    @Override
    protected boolean act(List<Defense> defenses) {
        Durable target = nearestTower(defenses);
        if (target == null) {
            heading = baseHeading;   // nothing to shoot: back on course for the post
            return false;
        }
        heading = Math.atan2(target.getCenterY() - y, target.getCenterX() - x);   // turn the gun on it
        fireTimer--;
        if (fireTimer <= 0) {
            fireTimer = FIRE_INTERVAL;
            ((Defense) target).damage(1);
            shot = new double[]{x, y, target.getCenterX(), target.getCenterY()};
        }
        return true;   // it stays put while it shoots
    }

    /** The nearest live Pana tower or torch inside gun range, or null. */
    private Durable nearestTower(List<Defense> defenses) {
        Durable best = null;
        double bestDist = RANGE;
        for (Defense d : defenses) {
            if (d.isDestroyed() || !(d instanceof PanaTower || d instanceof Torch)) {
                continue;
            }
            Durable du = (Durable) d;
            double dist = Math.hypot(du.getCenterX() - x, du.getCenterY() - y);
            if (dist < bestDist) {
                bestDist = dist;
                best = du;
            }
        }
        return best;
    }

    /** The shot fired this frame (from, to), or null. Reading it clears it, so the world draws it once. */
    public double[] takeShot() {
        double[] s = shot;
        shot = null;
        return s;
    }

    @Override
    protected double getHitRadius() {
        return 16;
    }

    @Override
    public String getName() {
        return "Raider";
    }

    @Override
    protected void drawShape(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, -LENGTH / 2, -WIDTH / 2, null);
            return;
        }
        Polygon hull = new Polygon(
                new int[]{-16, 10, 17, 10, -16},
                new int[]{-6, -7, 0, 7, 6}, 5);
        g.setColor(new Color(92, 36, 40));          // dark red hull
        g.fillPolygon(hull);
        g.setColor(new Color(24, 10, 12));
        g.drawPolygon(hull);
        g.setColor(new Color(150, 150, 160));       // a long gun on the bow
        g.fillRect(4, -1, 12, 2);
        g.setColor(new Color(190, 190, 200));
        g.fillOval(-2, -3, 6, 6);                   // turret
        g.setColor(new Color(220, 150, 60));        // orange stripe: easy to tell from a Skiff
        g.fillRect(-14, -1, 8, 2);
    }
}
