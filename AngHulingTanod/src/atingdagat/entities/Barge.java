package atingdagat.entities;

import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Barge.java
 *
 * A slow landing barge full of troops. It takes a lot of hits to sink, and when it reaches
 * the shore it hurts the post twice as much as a skiff. Nets and mines are the answer to it.
 */
public class Barge extends Warship {

    private static final String SPRITE_PATH = "/atingdagat/res/ships/barge.png";
    private static final int LENGTH = 58;
    private static final int WIDTH = 25;

    private final BufferedImage sprite = Assets.get(SPRITE_PATH, LENGTH, WIDTH);

    public Barge(double x, double y, double targetX, double targetY) {
        super(x, y, targetX, targetY, 0.3, 8);
        landingDamage = 6;
    }

    @Override
    protected double getHitRadius() {
        return 20;
    }

    @Override
    public String getName() {
        return "Barge";
    }

    @Override
    protected void drawShape(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, -LENGTH / 2, -WIDTH / 2, null);
            return;
        }
        g.setColor(new Color(52, 56, 66));
        g.fillRect(-20, -8, 38, 16);             // flat hull
        g.fillPolygon(new int[]{18, 22, 18}, new int[]{-8, 0, 8}, 3);   // blunt bow
        g.setColor(new Color(14, 16, 22));
        g.drawRect(-20, -8, 38, 16);
        g.setColor(new Color(96, 66, 60));       // cargo boxes
        g.fillRect(-16, -5, 9, 10);
        g.fillRect(-5, -5, 9, 10);
        g.setColor(new Color(190, 34, 48));
        g.fillRect(6, -2, 7, 4);
    }
}
