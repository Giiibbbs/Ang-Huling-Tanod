package atingdagat.entities;

import atingdagat.core.Constants;
import atingdagat.defense.Defense;
import atingdagat.map.TileMap;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;

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
    private static final int ATTACK_INTERVAL = 45;   // frames between hits on a net
    private static final double HIT_RADIUS = 13;     // how close a click must be to hit the ship (a big ship can say more)

    protected double x;
    protected double y;
    protected double heading;      // direction of travel, in radians
    protected double speed;        // pixels per frame
    protected int health;
    protected int maxHealth;
    private static final int FLASH_FRAMES = 8;
    private int flash = 0;              // counts down after a hit, for the white flash
    private int pendingDamage = 0;      // damage not yet shown as a floating number
    protected int landingDamage = 3;   // how much it hurts the post if it reaches the shore

    private boolean arrived = false;    // true once it has reached the shore
    private int attackTimer = 0;        // counts frames while the ship is cutting a net
    private double lit = 0;             // 0 = unseen, 1 = fully lit by the beam
    protected double litRise = 0.3;     // how fast a light shows the ship (a Ghost is slow)
    protected double litFall = 0.02;    // how fast it fades back into the dark (a Ghost is quick)

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

    /**
     * A hook for ships with their own behaviour. Return true if the ship used this frame (so it does
     * not sail). The default does nothing, so Skiff, Cutter and Barge are unchanged.
     */
    protected boolean act(List<Defense> defenses) {
        return false;
    }

    // What each ship type must provide:

    /** Draws the ship at (0, 0), pointing right. The base class moves and rotates it into place. */
    protected abstract void drawShape(Graphics2D g);

    public abstract String getName();

    // Shared behaviour:

    public void update(TileMap map, Light light, List<Defense> defenses) {
        if (flash > 0) {
            flash--;
        }
        if (!act(defenses)) {   // a special ship (the Raider) may spend its turn shooting instead of sailing
            move(map, defenses);
        }
        updateLight(light);
        updateWake();
    }

    /** Sails straight toward the target. Stops to cut a net in its way, and stops for good on land. */
    private void move(TileMap map, List<Defense> defenses) {
        if (arrived) {
            return;
        }

        Defense blocker = findBlocker(defenses);
        if (blocker != null) {
            attackTimer++;
            if (attackTimer >= ATTACK_INTERVAL) {
                attackTimer = 0;
                blocker.damage(1);
            }
            return; // stuck in the net: no movement
        }
        attackTimer = 0;

        x += Math.cos(heading) * speed;
        y += Math.sin(heading) * speed;

        int col = (int) Math.floor(x / Constants.TILE_SIZE);
        int row = (int) Math.floor(y / Constants.TILE_SIZE);
        if (map.isLand(row, col)) {
            arrived = true;
        }
    }

    /** Looks a little ahead of the bow (and a little to each side) for a defense in the way. */
    private Defense findBlocker(List<Defense> defenses) {
        double ahead = 10;
        double side = 5;
        double fx = Math.cos(heading);
        double fy = Math.sin(heading);
        double[][] probes = {
            {x + fx * ahead, y + fy * ahead},
            {x + fx * ahead - fy * side, y + fy * ahead + fx * side},
            {x + fx * ahead + fy * side, y + fy * ahead - fx * side}
        };
        for (Defense d : defenses) {
            if (d.isDestroyed() || !d.blocksShips()) {
                continue;
            }
            for (double[] p : probes) {
                int col = (int) Math.floor(p[0] / Constants.TILE_SIZE);
                int row = (int) Math.floor(p[1] / Constants.TILE_SIZE);
                if (d.occupies(col, row)) {
                    return d;
                }
            }
        }
        return null;
    }

    /** Lit quickly when any light touches it, then fades slowly: an afterglow of about a second. */
    private void updateLight(Light light) {
        if (light.lights(x, y)) {
            lit = Math.min(1.0, lit + litRise);
        } else {
            lit = Math.max(0.0, lit - litFall);
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

    /**
     * darkness 0 = day (always visible), 1 = night (only as visible as the beam made it).
     * Dusk and dawn blend between the two.
     */
    public void draw(Graphics2D g, double darkness) {
        float visibility = (float) (1.0 - darkness * (1.0 - lit));
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

    /** The player shot at it. */
    /** A night's twist can make a ship faster and tougher. It also counts as more maximum health, so the health bar stays right. */
    public void powerUp(double speedFactor, int extraHealth) {
        speed *= speedFactor;
        health += extraHealth;
        maxHealth += extraHealth;
    }

    public void hit(int damage) {
        health -= damage;
        pendingDamage += damage;
        flash = FLASH_FRAMES;
    }

    /** How white the ship is right now, 0 to 1 (it flashes when hit). */
    public double getFlash() {
        return flash / (double) FLASH_FRAMES;
    }

    /** Damage taken since the last call, for the floating "-2" number. Reading it clears it. */
    public int takePendingDamage() {
        int d = pendingDamage;
        pendingDamage = 0;
        return d;
    }

    public boolean isDead() {
        return health <= 0;
    }

    /** Is this point (a mouse click) on the ship? */
    public boolean contains(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        double r = getHitRadius();
        return dx * dx + dy * dy <= r * r;
    }

    /** How big the click target is. Long ships override this, so you can hit the bow and the stern too. */
    protected double getHitRadius() {
        return HIT_RADIUS;
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

    public int getLandingDamage() {
        return landingDamage;
    }

    public int getHealth() {
        return health;
    }
}
