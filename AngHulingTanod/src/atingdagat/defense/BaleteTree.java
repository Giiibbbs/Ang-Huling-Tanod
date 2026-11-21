package atingdagat.defense;

import atingdagat.core.Constants;
import atingdagat.economy.Harvestable;
import atingdagat.economy.ResourceType;
import atingdagat.entities.GlowSource;
import atingdagat.entities.Warship;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * BaleteTree.java
 *
 * The old balete tree on the northeast islet, home of a kapre. In Filipino folklore a kapre
 * guards his tree: he is only a danger to people who disturb it, and a good friend to those who
 * show respect. So here he is an ALLY you must win over.
 *
 * Three stages: asleep (nothing happens), STIRRED (the story event after night 3: you may leave
 * an offering) and AWAKE (you gave one: he throws coconuts at ships that come near, light or no light).
 *
 * It is a Defense (the world updates and draws it like the others) and a Harvestable (you "harvest" it
 * by holding the mouse on it, which makes the offering), and a GlowSource (his cigar glows).
 * Three interfaces, one object: that is what interfaces are for.
 */
public class BaleteTree implements Defense, Harvestable, GlowSource {

    private static final String SPRITE_PATH = "/atingdagat/res/monsters/balete.png";
    private final BufferedImage sprite = Assets.get(SPRITE_PATH, 36, 44);

    private final int col;
    private final int row;
    private boolean stirred = false;
    private boolean awake = false;
    private int cooldown = 0;
    private int throwTimer = 0;
    private double throwX;
    private double throwY;

    public BaleteTree(int col, int row) {
        this.col = col;
        this.row = row;
    }

    // ---- the story stages ----

    public void stir() {
        stirred = true;
    }

    public void wake() {
        stirred = true;
        awake = true;
    }

    public boolean isStirred() {
        return stirred;
    }

    public boolean isAwake() {
        return awake;
    }

    // ---- position ----

    @Override
    public double getX() {
        return col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    @Override
    public double getY() {
        return row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    // ---- Harvestable: holding the mouse on the tree makes the offering ----

    @Override
    public boolean isNear(double px, double py) {
        return Math.hypot(px - getX(), py - (getY() - 18)) <= 20;
    }

    @Override
    public ResourceType getType() {
        return ResourceType.TREE;
    }

    @Override
    public boolean isAvailable() {
        return stirred && !awake;
    }

    @Override
    public int getHoldFrames() {
        return 120;
    }

    /** The world handles the offering itself (it takes the meals), so there is nothing to carry away. */
    @Override
    public int harvest() {
        return 0;
    }

    // ---- Defense: the kapre throws coconuts ----

    @Override
    public void update(List<Warship> ships) {
        if (cooldown > 0) {
            cooldown--;
        }
        if (throwTimer > 0) {
            throwTimer--;
        }
        if (!awake || cooldown > 0) {
            return;
        }
        Warship best = null;
        double bestDist = Constants.KAPRE_RANGE;
        for (Warship ship : ships) {
            double d = Math.hypot(ship.getX() - getX(), ship.getY() - (getY() - 22));
            if (!ship.isDead() && d <= bestDist) {
                best = ship;
                bestDist = d;
            }
        }
        if (best != null) {
            best.hit(Constants.KAPRE_DAMAGE);
            cooldown = Constants.KAPRE_COOLDOWN;
            throwTimer = 18;
            throwX = best.getX();
            throwY = best.getY();
        }
    }

    @Override
    public boolean occupies(int c, int r) {
        return c == col && r == row;
    }

    @Override
    public boolean blocksShips() {
        return false;
    }

    @Override
    public void damage(int amount) {
        // Ships cannot reach the islet, and a kapre does not mind.
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    // ---- GlowSource: the cigar ----

    @Override
    public double getGlowX() {
        return getX() + 11;
    }

    @Override
    public double getGlowY() {
        return getY() - 22;
    }

    @Override
    public double getGlowRadius() {
        return 20;
    }

    // ---- drawing ----

    @Override
    public void draw(Graphics2D g, int frame) {
        int x = (int) getX();
        int y = (int) getY() + 5;
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x - 12, y - 3, 24, 7);
        if (sprite != null) {
            g.drawImage(sprite, x - 18, y - 42, null);
        } else {
            drawCode(g, x, y);
        }
        if (awake) {
            drawKapre(g, x, y, frame);
        } else if (stirred) {
            int bob = (int) Math.round(Math.sin(frame * 0.1) * 2);   // a floating "?" : leave an offering
            g.setColor(new Color(255, 225, 140));
            g.fillOval(x - 5, y - 58 + bob, 11, 11);
            g.setColor(new Color(60, 40, 20));
            g.drawString("?", x - 2, y - 49 + bob);
        }
        if (throwTimer > 0) {   // a coconut flying to the ship
            double t = 1.0 - throwTimer / 18.0;
            double cx = x + 11 + (throwX - (x + 11)) * t;
            double cy = y - 24 + (throwY - (y - 24)) * t - Math.sin(t * Math.PI) * 14;
            g.setColor(new Color(110, 78, 46));
            g.fillOval((int) cx - 2, (int) cy - 2, 5, 5);
        }
    }

    /** Code-drawn balete: a thick trunk, hanging aerial roots and a dark, heavy crown. */
    private void drawCode(Graphics2D g, int x, int y) {
        g.setColor(new Color(74, 56, 40));                 // trunk, wider at the foot
        g.fillRect(x - 4, y - 24, 8, 24);
        g.fillRect(x - 7, y - 6, 14, 6);
        g.setColor(new Color(40, 30, 22));
        g.drawLine(x - 2, y - 22, x - 2, y);
        g.setColor(new Color(112, 88, 60));                // aerial roots hang from the crown like curtains
        for (int i = -3; i <= 3; i++) {
            g.drawLine(x + i * 4, y - 28, x + i * 4 + (i % 2), y - 2);
        }
        g.setColor(new Color(28, 70, 40));                 // crown
        g.fillOval(x - 17, y - 40, 34, 18);
        g.setColor(new Color(36, 88, 50));
        g.fillOval(x - 12, y - 46, 24, 16);
        g.setColor(new Color(24, 58, 36));
        g.fillOval(x + 2, y - 40, 16, 12);
    }

    /** The kapre sits on a branch: a big dark shape with a glowing cigar and a curl of smoke. */
    private void drawKapre(Graphics2D g, int x, int y, int frame) {
        int kx = x + 8;
        int ky = y - 26;
        g.setColor(new Color(34, 28, 28));
        g.fillRect(kx - 3, ky - 8, 7, 10);                 // body
        g.fillOval(kx - 3, ky - 14, 7, 7);                 // head
        g.setColor(new Color(255, 215, 90));
        g.fillRect(kx - 2, ky - 12, 1, 1);                 // eyes
        g.fillRect(kx + 1, ky - 12, 1, 1);
        int pulse = 150 + (int) (Math.sin(frame * 0.12) * 80);
        g.setColor(new Color(255, 150, 60, Math.max(0, Math.min(255, pulse))));
        g.fillRect(kx + 4, ky - 8, 3, 2);                  // the cigar
        g.setColor(new Color(220, 220, 225, 90));          // smoke
        for (int i = 0; i < 3; i++) {
            int rise = (frame / 3 + i * 9) % 27;
            g.fillOval(kx + 6 + (int) (Math.sin((frame + i * 20) * 0.1) * 2), ky - 10 - rise, 2 + rise / 9, 2 + rise / 9);
        }
    }
}
