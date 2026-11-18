package atingdagat.entities;

import atingdagat.core.Constants;
import atingdagat.map.TileMap;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Warship.java
 *
 * The parent of every enemy ship (Skiff now, Cutter, Barge, Frigate, and
 * Dreadnought later). It holds everything ships have in common: position,
 * movement, being lit by the beam, and the wake behind it. A subclass only
 * says what it looks like and how fast or tough it is.
 *
 * This is inheritance: write the shared parts once, and each ship type adds
 * only what is different.
 */
public abstract class Warship {

    private static final int WAKE_POINTS = 10;

    protected double x;
    protected double y;
    protected double heading;      // direction of travel, in radians
    protected double speed;        // pixels per frame
    protected int health;
    protected final int maxHealth;

    private boolean arrived = false;    // true once it has reached the shore
    private double lit = 0;             // 0 = unseen, 1 = fully lit by the beam

    // Recent positions, used to draw the wake. A ring buffer built from plain arrays.
    private final double[] wakeX = new double[WAKE_POINTS];
    private final double[] wakeY = new double[WAKE_POINTS];
    private int wakeHead = 0;
    private int wakeCount = 0;
    private int wakeTimer = 0;

    protected Warship(double x, double y, double targetX, double targetY, double speed, int health) {
        this.x = x;
        this.y = y;
        this.heading = Math.atan2(targetY - y, targetX - x);
        this.speed = speed;
        this.health = health;
        this.maxHealth = health;
    }

    // What each ship type must provide:

    /** Draws the ship at (0, 0), pointing right. The base class moves and rotates it into place. */
    protected abstract void drawShape(Graphics2D g);

    public abstract String getName();

    // Shared behaviour:

    public void update(TileMap map, Beam beam) {
        move(map);
        updateLight(beam);
        updateWake();
    }

    /** Sails straight toward the target and stops when it touches land. */
    private void move(TileMap map) {
        if (arrived) {
            return;
        }
        x += Math.cos(heading) * speed;
        y += Math.sin(heading) * speed;

        int col = (int) Math.floor(x / Constants.TILE_SIZE);
        int row = (int) Math.floor(y / Constants.TILE_SIZE);
        if (map.isLand(row, col)) {
            arrived = true;
        }
    }

    /** Lit quickly when the beam touches it, then fades slowly: an afterglow of about a second. */
    private void updateLight(Beam beam) {
        if (beam.lights(x, y)) {
            lit = Math.min(1.0, lit + 0.3);
        } else {
            lit = Math.max(0.0, lit - 0.02);
        }
    }

    private void updateWake() {
        if (arrived) {
            return;
        }
        wakeTimer++;
        if (wakeTimer >= 5) {
            wakeTimer = 0;
            wakeX[wakeHead] = x;
            wakeY[wakeHead] = y;
            wakeHead = (wakeHead + 1) % WAKE_POINTS;
            if (wakeCount < WAKE_POINTS) {
                wakeCount++;
            }
        }
    }

    /** At night a ship is only as visible as the beam made it. In the day it is fully visible. */
    public void draw(Graphics2D g, boolean night) {
        float visibility = night ? (float) lit : 1f;
        if (visibility < 0.02f) {
            return; // not lit, so we don't even draw it
        }

        java.awt.Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, visibility));

        drawWake(g);

        AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        g.rotate(heading);
        drawShape(g);
        g.setTransform(oldTransform);

        g.setComposite(oldComposite);
    }

    private void drawWake(Graphics2D g) {
        for (int k = 0; k < wakeCount; k++) {
            int index = (wakeHead - 1 - k + WAKE_POINTS * 2) % WAKE_POINTS; // newest first
            double age = k / (double) WAKE_POINTS;                          // 0 = new, 1 = old
            int alpha = (int) (110 * (1 - age));
            double size = 1.5 + 4 * age;                                    // the wake spreads as it ages
            g.setColor(new Color(255, 255, 255, alpha));
            g.fillOval((int) (wakeX[index] - size), (int) (wakeY[index] - size), (int) (size * 2), (int) (size * 2));
        }
    }

    // Getters

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean hasArrived() {
        return arrived;
    }

    public boolean isLit() {
        return lit > 0.3;
    }

    public int getHealth() {
        return health;
    }
}
