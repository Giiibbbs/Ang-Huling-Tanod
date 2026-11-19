package atingdagat.ui;

import java.awt.Color;
import java.awt.Graphics2D;

/** A quick expanding ring and flash. Shows a mine going off. */
public class Explosion {

    private static final int LIFE = 22;
    private final double x;
    private final double y;
    private int age = 0;

    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        age++;
    }

    public boolean isDone() {
        return age >= LIFE;
    }

    public void draw(Graphics2D g) {
        double t = age / (double) LIFE;
        int r = (int) (4 + 30 * t);
        int alpha = (int) (230 * (1 - t));
        g.setColor(new Color(255, 200, 90, alpha / 2));
        g.fillOval((int) x - r, (int) y - r, r * 2, r * 2);
        g.setColor(new Color(255, 245, 200, alpha));
        g.drawOval((int) x - r, (int) y - r, r * 2, r * 2);
    }
}
