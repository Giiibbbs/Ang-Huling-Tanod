package atingdagat.entities;

import atingdagat.core.Constants;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Flare.java
 *
 * The flare gun (key F). A flare flies in a short arc to the spot under the mouse, then burns for
 * 6 seconds, lighting a circle of 70 px. It is both a Light (ships inside it can be seen and shot,
 * including Ghosts) and a GlowSource (the night overlay cuts a bright hole around it). Because it
 * implements the same two interfaces as a torch, the world needed almost no new code to use it.
 */
public class Flare implements Light, GlowSource {

    private static final int FLIGHT = 24;   // frames in the air

    private final double startX;
    private final double startY;
    private final double targetX;
    private final double targetY;
    private int age = 0;

    public Flare(double startX, double startY, double targetX, double targetY) {
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
    }

    public void update() {
        age++;
    }

    public boolean isDone() {
        return age > FLIGHT + Constants.FLARE_LIFE;
    }

    public boolean hasLanded() {
        return age > FLIGHT;
    }

    /** 0 to 1: grows quickly when it lands and dies away over the last second. */
    private double strength() {
        if (!hasLanded()) {
            return 0;
        }
        int burn = age - FLIGHT;
        double rise = Math.min(1.0, burn / 12.0);
        double fall = Math.min(1.0, (Constants.FLARE_LIFE - burn) / 60.0);
        return Math.max(0, Math.min(rise, fall));
    }

    @Override
    public boolean lights(double x, double y) {
        double r = Constants.FLARE_RADIUS * strength();
        return r > 0 && Math.hypot(x - targetX, y - targetY) <= r;
    }

    @Override
    public double getGlowX() {
        return targetX;
    }

    @Override
    public double getGlowY() {
        return targetY;
    }

    @Override
    public double getGlowRadius() {
        return Math.max(1, Constants.FLARE_RADIUS * strength());
    }

    public void draw(Graphics2D g, int frame) {
        if (!hasLanded()) {   // in the air: a bright dot on an arc, with a small trail
            double t = age / (double) FLIGHT;
            double x = startX + (targetX - startX) * t;
            double y = startY + (targetY - startY) * t - Math.sin(t * Math.PI) * 38;
            g.setColor(new Color(255, 120, 80, 120));
            g.fillOval((int) x - 3, (int) y - 3, 6, 6);
            g.setColor(new Color(255, 240, 200));
            g.fillOval((int) x - 2, (int) y - 2, 4, 4);
            return;
        }
        double s = strength();
        int flicker = (frame / 3) % 3;
        g.setColor(new Color(255, 200, 120, (int) (150 * s)));
        g.fillOval((int) targetX - 5 - flicker, (int) targetY - 5 - flicker, 10 + flicker * 2, 10 + flicker * 2);
        g.setColor(new Color(255, 250, 225, (int) (255 * s)));
        g.fillOval((int) targetX - 2, (int) targetY - 2, 4, 4);
    }
}
