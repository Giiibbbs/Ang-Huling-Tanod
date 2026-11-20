package atingdagat.entities;

import atingdagat.core.Constants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Bangka.java
 *
 * The fishing boat that carries a survivor to the island. It sails in a straight line from
 * past the screen edge to the shallows, drops its passenger once, then stays a while
 * (bobbing and fading) before it is gone.
 */
public class Bangka {

    private double x;
    private final double y;
    private final double targetX;
    private final int direction;     // +1 sails to the right (came from the west), -1 sails to the left
    private Survivor cargo;
    private int lingerFrames = 0;

    public Bangka(double startX, double y, double targetX, Survivor cargo) {
        this.x = startX;
        this.y = y;
        this.targetX = targetX;
        this.direction = targetX >= startX ? 1 : -1;
        this.cargo = cargo;
    }

    public void update() {
        if (!hasArrived()) {
            x += direction * Constants.BANGKA_SPEED;
            if (hasArrived()) {
                x = targetX;
            }
        } else {
            lingerFrames++;
        }
    }

    public boolean hasArrived() {
        return direction > 0 ? x >= targetX : x <= targetX;
    }

    /** Hands over the passenger, once. Returns null if it was already handed over (or arrived with nobody). */
    public Survivor releaseCargo() {
        if (!hasArrived() || cargo == null) {
            return null;
        }
        Survivor out = cargo;
        cargo = null;
        return out;
    }

    public boolean isGone() {
        return lingerFrames > 360;
    }

    public void draw(Graphics2D g, int frame) {
        double fade = lingerFrames > 240 ? Math.max(0, 1 - (lingerFrames - 240) / 120.0) : 1;
        double bob = Math.sin(frame * 0.07) * 0.8;
        AffineTransform old = g.getTransform();
        g.translate(x, y + bob);
        if (direction < 0) {
            g.scale(-1, 1);
        }
        int a = (int) (255 * fade);
        g.setColor(new Color(0, 0, 0, (int) (50 * fade)));
        g.fillOval(-9, -2, 20, 7);
        g.setColor(new Color(120, 86, 52, a));                       // outriggers
        g.setStroke(new BasicStroke(1f));
        g.drawLine(-6, -6, 6, -6);
        g.drawLine(-6, 6, 6, 6);
        g.drawLine(-3, -6, -3, 6);
        g.drawLine(3, -6, 3, 6);
        g.setColor(new Color(176, 120, 64, a));                      // hull
        g.fillOval(-10, -2, 20, 5);
        g.setColor(new Color(90, 58, 34, a));
        g.drawOval(-10, -2, 20, 5);
        g.setColor(new Color(224, 90, 70, a));                       // stripe
        g.fillRect(-2, -1, 5, 3);
        g.setTransform(old);

        double lx = x + direction * 9;                               // lantern at the bow
        double ly = y + bob;
        float r = 8f;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(lx, ly), r, new float[]{0f, 1f},
                new Color[]{new Color(255, 215, 120, (int) (150 * fade)), new Color(255, 215, 120, 0)}));
        g.fill(new Ellipse2D.Double(lx - r, ly - r, r * 2, r * 2));
        g.setColor(new Color(255, 236, 170, a));
        g.fillRect((int) lx - 1, (int) ly - 1, 3, 3);
    }
}
