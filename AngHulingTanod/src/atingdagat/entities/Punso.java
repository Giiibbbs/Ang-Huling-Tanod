package atingdagat.entities;

import atingdagat.core.Constants;
import atingdagat.economy.Harvestable;
import atingdagat.economy.ResourceType;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Punso.java
 *
 * The anthill (punso) on the west shore where a duwende lives. In Filipino folklore you say
 * "tabi-tabi po" when you pass a punso, because a duwende may live inside and he can bring
 * luck or trouble. Here he is a trickster-friend you win over with a small gift of bamboo.
 *
 * Stages: asleep, STIRRED (the story event after night 1: you may leave a gift) and AWAKE
 * (you left one: he gives you a little ore every morning, the ore that is running out).
 *
 * Harvestable: holding the mouse on the mound is how you leave the gift (GameWorld handles
 * what it costs). GlowSource: a faint warm light at night once he is awake.
 */
public class Punso implements Harvestable, GlowSource {

    private final BufferedImage sprite = Assets.get("/atingdagat/res/monsters/punso.png", 22, 22);
    private final int col;
    private final int row;
    private boolean stirred = false;
    private boolean awake = false;

    public Punso(int col, int row) {
        this.col = col;
        this.row = row;
    }

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

    @Override
    public double getX() {
        return col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    @Override
    public double getY() {
        return row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    @Override
    public boolean isNear(double px, double py) {
        return Math.hypot(px - getX(), py - (getY() - 4)) <= 12;
    }

    @Override
    public ResourceType getType() {
        return ResourceType.BAMBOO;
    }

    @Override
    public boolean isAvailable() {
        return stirred && !awake;
    }

    @Override
    public int getHoldFrames() {
        return 100;
    }

    /** The world takes the gift itself, so nothing is carried away. */
    @Override
    public int harvest() {
        return 0;
    }

    @Override
    public double getGlowX() {
        return getX();
    }

    @Override
    public double getGlowY() {
        return getY() - 6;
    }

    @Override
    public double getGlowRadius() {
        return 16;
    }

    public void draw(Graphics2D g, int frame) {
        int x = (int) getX();
        int y = (int) getY() + 4;
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x - 9, y - 2, 18, 5);
        if (sprite != null) {
            g.drawImage(sprite, x - 11, y - 20, null);
        } else {
            g.setColor(new Color(150, 84, 52));          // the mound of red earth
            g.fillOval(x - 8, y - 11, 16, 13);
            g.setColor(new Color(176, 106, 70));
            g.fillOval(x - 5, y - 11, 9, 8);
            g.setColor(new Color(90, 48, 30));           // the little doorway
            g.fillOval(x - 2, y - 5, 4, 5);
            g.setColor(new Color(30, 20, 16));           // ants
            for (int i = 0; i < 3; i++) {
                g.fillRect(x - 6 + ((frame / 6 + i * 5) % 12), y - 1 - i * 2, 1, 1);
            }
        }
        if (awake) {
            drawDuwende(g, x, y, frame);
        } else if (stirred) {
            int bob = (int) Math.round(Math.sin(frame * 0.1) * 2);   // a floating "?" : leave a gift
            g.setColor(new Color(255, 225, 140));
            g.fillOval(x - 5, y - 30 + bob, 11, 11);
            g.setColor(new Color(60, 40, 20));
            g.drawString("?", x - 2, y - 21 + bob);
        }
    }

    /** A tiny old man with a red pointed hat sitting on the mound. He nods now and then. */
    private void drawDuwende(Graphics2D g, int x, int y, int frame) {
        int nod = (frame / 40) % 2;
        int dx = x + 3;
        int dy = y - 11;
        g.setColor(new Color(70, 110, 160));            // blue coat
        g.fillRect(dx - 2, dy - 5, 5, 5);
        g.setColor(new Color(236, 192, 150));           // face
        g.fillRect(dx - 2, dy - 9 + nod, 5, 4);
        g.setColor(new Color(235, 235, 235));           // white beard
        g.fillRect(dx - 2, dy - 6 + nod, 5, 2);
        g.setColor(new Color(200, 50, 50));             // red pointed hat
        g.fillRect(dx - 3, dy - 10 + nod, 7, 2);
        g.fillRect(dx - 1, dy - 13 + nod, 3, 3);
        g.fillRect(dx, dy - 15 + nod, 1, 2);
    }
}
