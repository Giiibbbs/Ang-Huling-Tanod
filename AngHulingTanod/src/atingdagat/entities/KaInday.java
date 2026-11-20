package atingdagat.entities;

import atingdagat.core.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * KaInday.java
 *
 * The Babaylan, an elder who heals. Every few seconds she mends the post a little and
 * steadies the radio. She also glows softly (she is a GlowSource, like a torch, but she
 * does not light ships, so she helps you see without helping the ships get hit).
 */
public class KaInday extends Survivor implements GlowSource {

    private int timer = 0;
    private int flash = 0;   // draws a green "+" for a moment after a heal

    public KaInday(double startX, double startY, double homeX, double homeY) {
        super("Ka Inday", "Babaylan", startX, startY, homeX, homeY);
    }

    @Override
    public void act(Post post, Radio radio, List<Warship> ships, int frame) {
        if (flash > 0) {
            flash--;
        }
        if (!isHome()) {
            return;
        }
        timer++;
        if (timer >= Constants.INDAY_HEAL_FRAMES) {
            timer = 0;
            if (post.getHealth() < post.getMaxHealth() || radio.getSignal() < Radio.MAX_SIGNAL) {
                post.repair(1);
                radio.restore(2);
                flash = 40;
            }
        }
    }

    @Override
    protected Color getShirt() {
        return new Color(236, 232, 210);   // white cloth
    }

    @Override
    protected void drawExtra(Graphics2D g, int px, int py, int frame) {
        g.setColor(new Color(200, 60, 50));             // red headband
        g.fillRect(px - 3, py - 19, 6, 1);
        g.setColor(new Color(90, 150, 80));             // a little wrap of leaves at the waist
        g.fillRect(px - 4, py - 7, 8, 2);
        if (flash > 0) {
            g.setColor(new Color(140, 255, 160, Math.min(255, flash * 8)));
            g.fillRect(px - 1, py - 32 - (40 - flash) / 6, 3, 1);
            g.fillRect(px, py - 33 - (40 - flash) / 6, 1, 3);
        }
    }

    @Override
    public String getArrivalTitle() {
        return "A bangka lands: Ka Inday, the Babaylan";
    }

    @Override
    public String getArrivalLine() {
        return "\"Ang dagat ang nagsabi. You are not alone. Let me tend the post.\"";
    }

    @Override
    public double getGlowX() {
        return x;
    }

    @Override
    public double getGlowY() {
        return y - 8;
    }

    @Override
    public double getGlowRadius() {
        return 24;
    }
}
