package atingdagat.entities;

import atingdagat.core.Constants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * Bakunawa.java
 *
 * The moon-eating dragon of Filipino folklore, and the big scripted event of night 7.
 *
 * In the old stories the Bakunawa rose from the sea to swallow the moon, and people
 * drove it away by making noise with pots, gongs and drums. In our game that is the whole
 * event, in four stages:
 *
 *   RISING    The moon is eaten (an eclipse). The lighthouse beam goes out. The dragon rises.
 *   GONGS     The player must sound the gongs: tap SPACE to fill the noise meter.
 *   SWALLOW   The meter is full. The dragon dives, swallows the Kordan flagship, and the
 *             Kordan ships that are left are lost with it. Then it sinks away.
 *   DONE      The moon is back. The night can now end.
 *
 * This class owns the timing and the drawing. GameWorld only asks it a few questions.
 */
public class Bakunawa {

    public enum Stage { IDLE, RISING, GONGS, SWALLOW, RETREAT, DONE }

    private static final int RISING_FRAMES = 170;
    private static final int SWALLOW_BITE_FRAME = 75;      // the moment the head hits the flagship
    private static final int SWALLOW_FRAMES = 230;

    private static final double MOON_X = 300;
    private static final double MOON_Y = 62;
    private static final double MOON_R = 16;
    private static final int FLAGSHIP_W = 100;
    private static final int FLAGSHIP_H = 50;
    private final java.awt.image.BufferedImage flagshipSprite =
            atingdagat.util.Assets.get("/atingdagat/res/ships/flagship.png", FLAGSHIP_W, FLAGSHIP_H);

    private static final double SHIP_X = 505;              // where the Kordan flagship waits
    private static final double SHIP_Y = 250;

    private static final int GONGS_HELP_FRAMES = 30 * 60;   // after 30 s of gongs the survivors ring too
    private static final int SMALL_HELP_FRAMES = 8 * 60;    // a small visit never lasts long: they help after 8 s
    private static final int RETREAT_FRAMES = 100;

    // Optional art (drawn by code when the files are missing): the head faces right.
    private final java.awt.image.BufferedImage headSprite =
            atingdagat.util.Assets.get("/atingdagat/res/monsters/bakunawa_head.png", 60, 40);
    private final java.awt.image.BufferedImage bodySprite =
            atingdagat.util.Assets.get("/atingdagat/res/monsters/bakunawa_body.png", 20, 20);
    private static final int SEGMENTS = 30;
    private static final int SEGMENT_GAP = 4;              // frames of history between body pieces

    private Stage stage = Stage.IDLE;
    private int timer = 0;
    private double noise = 0;             // the gong meter, 0 to 100
    private boolean biteReported = false;
    private double eclipse = 0;           // 0 = bright moon, 1 = fully eaten
    private boolean small = false;        // a small visit (nights 4, 6 and every third endless night): no swallow, just a dark spell
    private double drain = 0.35;          // how fast the gong noise dies away (higher = harder)
    private boolean omen = false;         // nights 3 and 5: the moon only turns red

    private double headX = MOON_X + 95;
    private double headY = -70;
    private double headAngle = Math.PI / 2;
    private double shipSink = 0;          // 0 = afloat, 1 = gone
    private final ArrayList<double[]> trail = new ArrayList<>();   // where the head has been (newest first)

    // ------------------------------------------------------------------
    // CONTROL
    // ------------------------------------------------------------------

    public void start() {
        if (stage != Stage.IDLE) {
            return;
        }
        stage = Stage.RISING;
        timer = 0;
        for (int i = 0; i < SEGMENTS * SEGMENT_GAP + 10; i++) {
            trail.add(new double[]{headX, headY});
        }
    }

    /** A small visit: the moon is eaten and the beam goes out until the gongs drive the dragon back. No flagship. */
    public void startSmall(boolean harder) {
        if (stage != Stage.IDLE) {
            return;
        }
        small = true;
        drain = harder ? 0.5 : 0.35;
        start();
    }

    /** Nights 3 and 5: only the moon turns red, as a warning. */
    public void setOmen(boolean on) {
        omen = on;
    }

    /** True while the dragon is on stage, so the night cannot end in the middle of it. */
    public boolean isBusy() {
        return stage != Stage.IDLE && stage != Stage.DONE;
    }

    /** The player taps the gong. Only counts while the gongs stage is on. */
    public void gong() {
        if (stage == Stage.GONGS) {
            noise = Math.min(100, noise + 6);
        }
    }

    /** True ONCE, on the frame the dragon bites. The world then removes the remaining ships. */
    public boolean consumeBite() {
        if (stage == Stage.SWALLOW && timer >= SWALLOW_BITE_FRAME && !biteReported) {
            biteReported = true;
            return true;
        }
        return false;
    }

    /** True while the moon is eaten: the lighthouse lamp is dark. */
    public boolean isEclipse() {
        return stage == Stage.RISING || stage == Stage.GONGS || stage == Stage.SWALLOW;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean isDone() {
        return stage == Stage.DONE;
    }

    /** Ends the event right now (used when the night is skipped). */
    public void finish() {
        stage = Stage.DONE;
        eclipse = 0;
    }

    /** Puts the dragon back to sleep (used at every dawn before night 7, so it can still rise on the last night). */
    public void reset() {
        stage = Stage.IDLE;
        timer = 0;
        noise = 0;
        eclipse = 0;
        small = false;
        drain = 0.35;
        omen = false;
        shipSink = 0;
        biteReported = false;
        headX = MOON_X + 95;
        headY = -70;
        trail.clear();
    }

    public double getNoise() {
        return noise;
    }

    /** The story line shown at the bottom of the screen ("" when there is nothing to say). */
    public String getCaption() {
        switch (stage) {
            case RISING:
                return small ? "The moon dims. The Bakunawa is hungry." : "The moon goes dark. The Bakunawa has come.";
            case GONGS:
                return "SOUND THE GONGS!  Tap SPACE as fast as you can.";
            case RETREAT:
                return "The Bakunawa sinks back into the sea. The moon returns.";
            case SWALLOW:
                return timer >= SWALLOW_BITE_FRAME
                        ? "The Bakunawa takes the Kordan flagship into the sea."
                        : "The gongs ring across the island...";
            default:
                return "";
        }
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    public void update(int frame) {
        if (stage == Stage.IDLE || stage == Stage.DONE) {
            return;
        }
        timer++;
        switch (stage) {
            case RISING: {
                double t = Math.min(1.0, timer / (double) RISING_FRAMES);
                double ease = 1 - Math.pow(1 - t, 3);
                eclipse = t;
                headX = MOON_X + 95 + 70 * Math.sin(timer * 0.05) * (1 - t);
                headY = -70 + (74 + 70) * ease;
                faceToward(MOON_X, MOON_Y, t > 0.85);
                if (timer >= RISING_FRAMES) {
                    stage = Stage.GONGS;
                    timer = 0;
                }
                break;
            }
            case GONGS:
                headX = MOON_X + 95 + 34 * Math.sin(frame * 0.03);
                headY = 74 + 7 * Math.sin(frame * 0.07);
                faceToward(MOON_X, MOON_Y, true);
                if (noise >= 100) {            // full meter: check BEFORE it can drain
                    stage = small ? Stage.RETREAT : Stage.SWALLOW;
                    timer = 0;
                } else {
                    noise = Math.max(0, noise - drain);   // the noise dies away if you stop tapping
                    if (timer > (small ? SMALL_HELP_FRAMES : GONGS_HELP_FRAMES)) {      // too slow: the survivors join in so the night cannot stall
                        noise = Math.min(100, noise + 0.5);
                    }
                }
                break;
            case RETREAT: {   // the small visit ends: it slides up out of the sky and the moon comes back
                double t = timer / (double) RETREAT_FRAMES;
                headY -= 2.4;
                headX += 0.8;
                headAngle = -Math.PI / 2;
                eclipse = Math.max(0, 1 - t * 1.4);
                if (timer >= RETREAT_FRAMES) {
                    stage = Stage.DONE;
                    eclipse = 0;
                }
                break;
            }
            default: { // SWALLOW
                if (timer <= SWALLOW_BITE_FRAME) {              // dive toward the flagship
                    double t = timer / (double) SWALLOW_BITE_FRAME;
                    double ease = t * t;
                    double startX = MOON_X + 95;
                    headX = startX + (SHIP_X - startX) * ease + 60 * Math.sin(t * Math.PI);
                    headY = 74 + (SHIP_Y - 74) * ease;
                    headAngle = angleTo(headX, headY, SHIP_X, SHIP_Y);
                } else if (timer <= SWALLOW_BITE_FRAME + 40) {   // the bite: it stays, the ship sinks
                    headX = SHIP_X + 6 * Math.sin(timer * 0.6);
                    headY = SHIP_Y - 4;
                    shipSink = Math.min(1.0, (timer - SWALLOW_BITE_FRAME) / 40.0);
                } else {                                         // it slides back into the deep, off the top
                    double t = (timer - SWALLOW_BITE_FRAME - 40) / (double) (SWALLOW_FRAMES - SWALLOW_BITE_FRAME - 40);
                    headX = SHIP_X - 120 * t + 40 * Math.sin(t * 6);
                    headY = SHIP_Y - (SHIP_Y + 150) * t * t;
                    headAngle = angleTo(headX, headY, headX - 30, headY - 90);
                    shipSink = 1.0;
                    eclipse = Math.max(0, 1 - t * 1.6);           // the moon comes back
                }
                if (timer >= SWALLOW_FRAMES) {
                    stage = Stage.DONE;
                    eclipse = 0;
                }
                break;
            }
        }
        trail.add(0, new double[]{headX, headY});
        while (trail.size() > SEGMENTS * SEGMENT_GAP + 12) {
            trail.remove(trail.size() - 1);
        }
    }

    private void faceToward(double x, double y, boolean smooth) {
        double want = angleTo(headX, headY, x, y);
        if (!smooth) {
            // while rising it faces the way it moves (down)
            want = Math.PI / 2;
        }
        headAngle += Beam.angleDiff(want, headAngle) * 0.12;
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY) {
        return Math.atan2(toY - fromY, toX - fromX);
    }

    // ------------------------------------------------------------------
    // DRAW (after the darkness, so the dragon is seen)
    // ------------------------------------------------------------------

    public void draw(Graphics2D g, int frame) {
        if (stage == Stage.IDLE && omen) {   // the warning: a blood-red moon, nothing more
            g.setColor(new Color(120, 10, 20, 30));
            g.fillRect(0, 0, Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT);
            double saved = eclipse;
            eclipse = 0.35;
            drawMoon(g);
            eclipse = saved;
            return;
        }
        if (stage == Stage.IDLE || stage == Stage.DONE) {
            return;
        }
        // The sky goes a sick red during the eclipse
        g.setColor(new Color(120, 10, 20, (int) (46 * eclipse)));
        g.fillRect(0, 0, Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT);

        drawMoon(g);
        if (!small) {
            drawFlagship(g, frame);
        }
        drawBody(g);
        drawHead(g);
    }

    private void drawMoon(Graphics2D g) {
        int x = (int) MOON_X;
        int y = (int) MOON_Y;
        int r = (int) MOON_R;
        g.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), (float) (r * 2.4),
                new float[]{0f, 1f}, new Color[]{new Color(255, 245, 200, 90), new Color(255, 245, 200, 0)}));
        g.fillOval(x - r * 2, y - r * 2, r * 4, r * 4);
        g.setColor(new Color(240, 238, 215));
        g.fillOval(x - r, y - r, r * 2, r * 2);
        g.setColor(new Color(200, 196, 170));
        g.fillOval(x - 6, y - 4, 5, 5);
        g.fillOval(x + 3, y + 3, 4, 4);
        // The shadow slides over the moon from the right
        if (eclipse > 0) {
            java.awt.Shape old = g.getClip();
            g.setClip(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
            double shift = (1 - eclipse) * (r * 2.2);
            g.setColor(new Color(60, 14, 22));
            g.fillOval((int) (x - r + shift), y - r, r * 2, r * 2);
            g.setClip(old);
            if (eclipse > 0.95) {   // a thin red ring is all that is left
                g.setColor(new Color(200, 60, 50, 160));
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(x - r, y - r, r * 2, r * 2);
                g.setStroke(new BasicStroke(1f));
            }
        }
    }

    /** The Kordan flagship: a big dark hull with a red flag. It waits at sea until the dragon takes it. */
    private void drawFlagship(Graphics2D g, int frame) {
        double enter = (stage == Stage.RISING) ? Math.min(1.0, timer / (double) RISING_FRAMES) : 1.0;
        if (shipSink >= 1.0) {
            return;
        }
        double x = SHIP_X + (1 - enter) * 160;
        double y = SHIP_Y + shipSink * 14;
        java.awt.Graphics2D gg = (java.awt.Graphics2D) g.create();
        gg.translate(x, y);
        gg.rotate(shipSink * 0.5);
        float alpha = (float) (1.0 - shipSink * 0.85);
        gg.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
        // The red glow of its lamps
        gg.setPaint(new RadialGradientPaint(new Point2D.Double(0, 0), 44f, new float[]{0f, 1f},
                new Color[]{new Color(255, 70, 50, 70), new Color(255, 70, 50, 0)}));
        gg.fillOval(-44, -44, 88, 88);
        if (flagshipSprite != null) {                  // Jaron's art, drawn from its centre
            gg.drawImage(flagshipSprite, -FLAGSHIP_W / 2, -FLAGSHIP_H / 2, null);
            gg.dispose();
            return;
        }
        Polygon hull = new Polygon(new int[]{-36, 30, 42, 30, -36}, new int[]{-9, -11, 0, 11, 9}, 5);
        gg.setColor(new Color(36, 38, 48));
        gg.fillPolygon(hull);
        gg.setColor(new Color(70, 74, 88));
        gg.fillRect(-14, -6, 24, 12);
        gg.setColor(new Color(90, 94, 110));
        gg.fillRect(-4, -4, 8, 8);
        gg.setColor(new Color(200, 40, 40));          // the Dominion flag
        gg.fillRect(-22, -2, 5, 4);
        gg.fillRect(22, -2, 5, 4);
        gg.setColor(new Color(0, 0, 0, 120));
        gg.drawPolygon(hull);
        gg.dispose();
    }

    /** The body is a chain of circles, each placed where the head was a few frames ago. */
    private void drawBody(Graphics2D g) {
        for (int i = SEGMENTS; i >= 1; i--) {
            int index = Math.min(trail.size() - 1, i * SEGMENT_GAP);
            double[] p = trail.get(index);
            double t = i / (double) SEGMENTS;
            double r = 9.5 - 6.5 * t;
            if (bodySprite != null) {
                int size = (int) Math.round(r * 2 + 2);
                g.drawImage(bodySprite, (int) (p[0] - size / 2.0), (int) (p[1] - size / 2.0), size, size, null);
                continue;
            }
            g.setColor(new Color(16, 54, 66));
            g.fill(new Ellipse2D.Double(p[0] - r - 1, p[1] - r - 1, r * 2 + 2, r * 2 + 2));
            g.setColor(i % 2 == 0 ? new Color(26, 92, 98) : new Color(34, 112, 112));
            g.fill(new Ellipse2D.Double(p[0] - r, p[1] - r, r * 2, r * 2));
            g.setColor(new Color(214, 190, 120, 150));   // pale belly scales
            g.fill(new Ellipse2D.Double(p[0] - r * 0.4, p[1] - r * 0.4, r * 0.8, r * 0.8));
            if (i % 4 == 0) {                            // a spine
                g.setColor(new Color(200, 70, 60));
                g.fill(new Ellipse2D.Double(p[0] - 1.5, p[1] - r - 2, 3, 3));
            }
        }
    }

    private void drawHead(Graphics2D g) {
        java.awt.Graphics2D gg = (java.awt.Graphics2D) g.create();
        gg.translate(headX, headY);
        gg.rotate(headAngle);
        if (headSprite != null) {   // art made for the game: faces right, centred on the head
            gg.drawImage(headSprite, -24, -20, null);
            gg.dispose();
            return;
        }
        // facing along +x. The jaw opens and closes slowly while it hovers
        double open = (stage == Stage.SWALLOW && timer > SWALLOW_BITE_FRAME) ? 0.1
                : 0.45 + 0.25 * Math.sin(timer * 0.12);
        double gap = 12 * open + 3;

        Path2D upper = new Path2D.Double();
        upper.moveTo(-8, -gap);
        upper.lineTo(18, -gap * 0.5);
        upper.lineTo(30, -3);
        upper.lineTo(18, 0);
        upper.lineTo(-8, 0);
        upper.closePath();
        Path2D lower = new Path2D.Double();
        lower.moveTo(-8, gap);
        lower.lineTo(18, gap * 0.5);
        lower.lineTo(30, 3);
        lower.lineTo(18, 0);
        lower.lineTo(-8, 0);
        lower.closePath();

        fill(gg, new Color(150, 24, 32), new Ellipse2D.Double(-2, -gap * 0.7, 26, gap * 1.4));   // the red mouth
        fill(gg, new Color(24, 84, 92), upper);
        fill(gg, new Color(24, 84, 92), lower);
        gg.setColor(new Color(245, 240, 220));                                                    // fangs
        gg.fillPolygon(new int[]{22, 26, 24}, new int[]{(int) -gap + 1, (int) -gap + 1, (int) -gap + 7}, 3);
        gg.fillPolygon(new int[]{22, 26, 24}, new int[]{(int) gap - 1, (int) gap - 1, (int) gap - 7}, 3);
        gg.setColor(new Color(214, 70, 60));                                                      // horns
        int gi = (int) Math.round(gap);
        gg.fillPolygon(new int[]{-8, -22, -10}, new int[]{-gi, -gi - 9, -gi + 3}, 3);
        gg.fillPolygon(new int[]{-8, -22, -10}, new int[]{gi, gi + 9, gi - 3}, 3);
        gg.setColor(new Color(255, 200, 60));                                                     // glowing eyes
        gg.fillOval(2, (int) -gap - 1, 5, 4);
        gg.fillOval(2, (int) gap - 3, 5, 4);
        gg.setColor(new Color(40, 10, 10));
        gg.fillRect(4, (int) -gap, 2, 2);
        gg.fillRect(4, (int) gap - 2, 2, 2);
        gg.dispose();
    }

    private static void fill(Graphics2D g, Color c, java.awt.Shape s) {
        g.setColor(c);
        g.fill(s);
    }
}
