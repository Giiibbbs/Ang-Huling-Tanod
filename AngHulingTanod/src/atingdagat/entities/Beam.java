package atingdagat.entities;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

/**
 * Beam.java
 *
 * The lighthouse beam: a cone of light that turns toward the mouse.
 *
 * Two jobs:
 *  - Logic: it can answer "does the beam light this point?" (lights()).
 *  - Drawing: it paints a soft warm glow. NightOverlay uses its numbers to cut
 *    the beam-shaped hole in the darkness.
 *
 * The beam does not snap to the mouse. It turns at a fixed speed, so the player
 * has to plan where to look. That is the core decision of the game.
 */
public class Beam implements Light {

    // Tuning numbers. Change these while playtesting.
    private static final double TURN_SPEED = 0.045;                  // steering by hand: radians per frame (about 155 degrees a second)
    private static final double SWEEP_SPEED = 0.02;                  // turning by itself: one full circle in about 5 seconds
    private static final double BASE_HALF_ANGLE = Math.toRadians(16);     // half the width of the cone
    private static final double BASE_LENGTH = 240;                       // how far the beam reaches (pixels)
    private static final double LAMP_GLOW = 44;                      // always-lit circle around the lamp

    private double halfAngle = BASE_HALF_ANGLE;   // grows with lamp upgrades
    private double length = BASE_LENGTH;
    private int upgradeLevel = 0;

    private final double originX;
    private final double originY;

    private double angle = -Math.PI / 2;        // current direction (starts pointing up)
    private double targetAngle = angle;         // where the player wants it to point

    public Beam(double originX, double originY) {
        this.originX = originX;
        this.originY = originY;
    }

    /** A lamp upgrade: a wider and longer beam. Returns false once fully upgraded. */
    public boolean upgrade(int maxLevel) {
        if (upgradeLevel >= maxLevel) {
            return false;
        }
        upgradeLevel++;
        halfAngle = BASE_HALF_ANGLE * (1 + 0.18 * upgradeLevel);
        length = BASE_LENGTH * (1 + 0.12 * upgradeLevel);
        return true;
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    /** Tell the beam where the player is pointing (a position in the game). */
    public void aimAt(double x, double y) {
        targetAngle = Math.atan2(y - originY, x - originX);
    }

    /**
     * One frame. When nobody is steering, the beam sweeps around by itself.
     * When the tanod steers it (manual), it turns toward the mouse, but no faster than TURN_SPEED.
     */
    public void update(boolean manual) {
        if (!manual) {
            angle += SWEEP_SPEED;
            if (angle > Math.PI) {
                angle -= 2 * Math.PI;
            }
            targetAngle = angle;   // so a hand-over to the player is smooth
            return;
        }
        double diff = angleDiff(targetAngle, angle);
        if (Math.abs(diff) <= TURN_SPEED) {
            angle = targetAngle;
        } else {
            angle += Math.signum(diff) * TURN_SPEED;
        }
    }

    /** True if this point is inside the beam or inside the lamp's glow. */
    @Override
    public boolean lights(double x, double y) {
        double dx = x - originX;
        double dy = y - originY;
        double distance = Math.hypot(dx, dy);
        if (distance <= LAMP_GLOW) {
            return true;
        }
        if (distance > length) {
            return false;
        }
        double toPoint = Math.atan2(dy, dx);
        return Math.abs(angleDiff(toPoint, angle)) <= halfAngle;
    }

    /** Paints a soft warm tint inside the cone and around the lamp (drawn after the darkness). */
    public void drawLight(Graphics2D g) {
        Arc2D cone = new Arc2D.Double(originX - length, originY - length, length * 2, length * 2,
                -Math.toDegrees(angle) - Math.toDegrees(halfAngle),
                2 * Math.toDegrees(halfAngle), Arc2D.PIE);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(originX, originY), (float) length,
                new float[]{0f, 0.5f, 0.9f, 1f},
                new Color[]{new Color(255, 240, 170, 95), new Color(255, 235, 150, 50),
                    new Color(255, 230, 140, 6), new Color(255, 230, 140, 0)}));
        g.fill(cone);

        g.setPaint(new RadialGradientPaint(new Point2D.Double(originX, originY), (float) LAMP_GLOW,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 240, 170, 80), new Color(255, 240, 170, 0)}));
        g.fillOval((int) (originX - LAMP_GLOW), (int) (originY - LAMP_GLOW),
                (int) (LAMP_GLOW * 2), (int) (LAMP_GLOW * 2));
    }

    /**
     * The shortest turn from angle b to angle a, in radians, between -PI and PI.
     * Needed because angles wrap around: 359 degrees and 1 degree are only 2 degrees apart.
     */
    public static double angleDiff(double a, double b) {
        double d = a - b;
        while (d > Math.PI) {
            d -= 2 * Math.PI;
        }
        while (d < -Math.PI) {
            d += 2 * Math.PI;
        }
        return d;
    }

    // Getters (the overlay needs these to draw the hole in the darkness)

    public double getOriginX() {
        return originX;
    }

    public double getOriginY() {
        return originY;
    }

    public double getAngle() {
        return angle;
    }

    public double getHalfAngle() {
        return halfAngle;
    }

    public double getLength() {
        return length;
    }

    public double getLampGlow() {
        return LAMP_GLOW;
    }
}
