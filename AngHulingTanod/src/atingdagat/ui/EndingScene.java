package atingdagat.ui;

import atingdagat.core.Constants;
import atingdagat.core.GameWorld;
import atingdagat.map.TileMap;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

/**
 * EndingScene.java
 *
 * What happens when the player survives night 7. It is a short story told over the island:
 *
 *   1. The sea is dark and quiet. The Kordan fleet is gone.
 *   2. Tiny lights appear across the water: the other islands answering the radio.
 *   3. A voice says "May natitira pa kami." You were never the last tanod.
 *   4. Bangkas (fishing boats) with lanterns come in from every side, and the sun rises.
 *   5. A helicopter arrives over the post.
 *   6. The title card.
 *
 * Everything is a timeline: one frame counter, and each thing starts at its own frame.
 * ENTER skips ahead to the title card.
 */
public class EndingScene {

    private static final int FINAL_CARD = 1860;   // the frame where the title card appears (about 31 seconds)

    private static final Font CAPTION = new Font("SansSerif", Font.BOLD, 13);
    private static final Font TITLE = new Font("SansSerif", Font.BOLD, 30);

    /** One line of the story: shown from "from" to "to" (in frames). */
    private static class Line {
        final int from;
        final int to;
        final String text;

        Line(int from, int to, String text) {
            this.from = from;
            this.to = to;
            this.text = text;
        }
    }

    private static final Line[] STORY = {
        new Line(0, 190, "Dawn of the eighth day."),
        new Line(190, 430, "The sea is quiet. The Kordan fleet is gone."),
        new Line(430, 700, "Then, across the dark water... lights."),
        new Line(700, 950, "A voice on the radio:  \"May natitira pa kami.\""),
        new Line(950, 1200, "\"May natitira pa kami.\"   \"May natitira pa kami.\""),
        new Line(1200, 1480, "Bangkas from every island. They heard you."),
        new Line(1480, FINAL_CARD, "And above the sea, the sound you waited seven nights for: rotors."),
    };

    private static class Boat {
        double x;
        double y;
        final double targetX;
        final double targetY;
        final int appear;
        final double speed;
        double angle;

        Boat(double startX, double startY, double targetX, double targetY, int appear, double speed) {
            this.x = startX;
            this.y = startY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.appear = appear;
            this.speed = speed;
            this.angle = Math.atan2(targetY - startY, targetX - startX);
        }
    }

    private final ArrayList<double[]> lights = new ArrayList<>();   // x, y, frame it appears, flicker offset
    private final ArrayList<Boat> boats = new ArrayList<>();
    private final double postX;
    private final double postY;
    private double heliX = Constants.INTERNAL_WIDTH + 60;
    private double heliY = 80;
    private int frames = 0;

    private final java.util.List<atingdagat.entities.Survivor> crew;   // the survivors who joined you (can be empty)

    public EndingScene(TileMap map, double postX, double postY, java.util.List<atingdagat.entities.Survivor> crew) {
        this.crew = crew;
        this.postX = postX;
        this.postY = postY;
        Random random = new Random(77);   // the same sea every time

        // Lights far out at sea (the other islands)
        int guard = 0;
        while (lights.size() < 44 && guard++ < 4000) {
            int col = random.nextInt(Constants.MAP_COLS);
            int row = 2 + random.nextInt(Constants.MAP_ROWS - 4);
            if (map.isWater(row, col) && map.getDepth(row, col) >= 3) {
                double x = col * Constants.TILE_SIZE + random.nextInt(Constants.TILE_SIZE);
                double y = row * Constants.TILE_SIZE + random.nextInt(Constants.TILE_SIZE);
                lights.add(new double[]{x, y, 460 + lights.size() * 17, random.nextDouble() * 6});
            }
        }

        // Bangkas: each sails from beyond the screen edge to a spot in the shallows around the island
        double centerX = 330;
        double centerY = 175;
        guard = 0;
        while (boats.size() < 16 && guard++ < 6000) {
            int col = random.nextInt(Constants.MAP_COLS);
            int row = 2 + random.nextInt(Constants.MAP_ROWS - 4);
            int depth = map.isWater(row, col) ? map.getDepth(row, col) : -1;
            if (depth < 2 || depth > 3) {
                continue;
            }
            double tx = col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            double ty = row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
            boolean crowded = false;
            for (Boat b : boats) {
                if (Math.hypot(b.targetX - tx, b.targetY - ty) < 34) {
                    crowded = true;
                }
            }
            if (crowded) {
                continue;
            }
            double dx = tx - centerX;
            double dy = ty - centerY;
            double len = Math.max(1, Math.hypot(dx, dy));
            double startX = tx + dx / len * 360;    // just outside the screen, straight out from the island
            double startY = ty + dy / len * 360;
            boats.add(new Boat(startX, startY, tx, ty, 1100 + boats.size() * 20, 1.1 + random.nextDouble() * 0.35));
        }
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    /** Moves the story one frame and sets the sky (the sun comes up between frames 1250 and 1800). */
    public void update(GameWorld world) {
        advance();
        double sun = Math.max(0, Math.min(1, (frames - 1250) / 550.0));
        world.setForcedDarkness(1.0 - sun);
    }

    private void advance() {
        frames++;
        for (Boat b : boats) {
            if (frames >= b.appear && Math.hypot(b.targetX - b.x, b.targetY - b.y) > 1.5) {
                b.x += Math.cos(b.angle) * b.speed;
                b.y += Math.sin(b.angle) * b.speed;
            }
        }
        if (frames > 1500) {   // the helicopter flies in from the right and hovers above the post
            double hoverX = postX + 6;
            double hoverY = postY - 46;
            double dx = hoverX - heliX;
            double dy = hoverY - heliY;
            double dist = Math.hypot(dx, dy);
            if (dist > 1) {
                double speed = Math.min(2.4, dist * 0.03 + 0.25);   // it slows down as it arrives
                heliX += dx / dist * speed;
                heliY += dy / dist * speed;
            }
        }
    }

    /** ENTER during the story: run the rest of it at once and show the title card. */
    public void skip() {
        while (frames < FINAL_CARD) {
            advance();
        }
    }

    public boolean isFinished() {
        return frames >= FINAL_CARD;
    }

    // ------------------------------------------------------------------
    // DRAW (on top of the world, which is drawn first)
    // ------------------------------------------------------------------

    public void draw(Graphics2D g) {
        double night = Math.max(0, Math.min(1, 1.0 - (frames - 1250) / 550.0));   // 1 = dark, 0 = full sun

        for (atingdagat.entities.Survivor sv : crew) {   // the survivors stand at the post and watch the sea
            sv.draw(g, frames);
        }
        drawLights(g, night);
        drawBoats(g, night);
        if (frames > 1500) {
            drawHelicopter(g);
        }
        if (isFinished()) {
            drawTitleCard(g);
        } else {
            drawCaption(g);
        }
    }

    private void drawLights(Graphics2D g, double night) {
        for (double[] l : lights) {
            double age = frames - l[2];
            if (age < 0) {
                continue;
            }
            double grow = Math.min(1.0, age / 40.0);
            double flicker = 0.8 + 0.2 * Math.sin(frames * 0.08 + l[3]);
            double strength = (0.3 + 0.7 * night) * grow * flicker;
            float r = (float) (9 * grow + 2);
            g.setPaint(new RadialGradientPaint(new Point2D.Double(l[0], l[1]), r, new float[]{0f, 1f},
                    new Color[]{new Color(255, 230, 150, (int) (170 * strength)), new Color(255, 230, 150, 0)}));
            g.fill(new Ellipse2D.Double(l[0] - r, l[1] - r, r * 2, r * 2));
            g.setColor(new Color(255, 248, 210, (int) (255 * Math.min(1.0, strength + 0.2))));
            g.fillRect((int) l[0], (int) l[1], 2, 2);
        }
    }

    /** A bangka: a slim hull with two bamboo outriggers, and a lantern at the bow. */
    private void drawBoats(Graphics2D g, double night) {
        for (Boat b : boats) {
            if (frames < b.appear) {
                continue;
            }
            AffineTransform old = g.getTransform();
            g.translate(b.x, b.y);
            g.rotate(b.angle);
            g.setColor(new Color(0, 0, 0, 50));
            g.fillOval(-9, -2, 20, 7);
            g.setColor(new Color(120, 86, 52));                       // outriggers
            g.setStroke(new BasicStroke(1f));
            g.drawLine(-6, -6, 6, -6);
            g.drawLine(-6, 6, 6, 6);
            g.drawLine(-3, -6, -3, 6);
            g.drawLine(3, -6, 3, 6);
            g.setColor(new Color(176, 120, 64));                      // hull
            g.fillOval(-10, -2, 20, 5);
            g.setColor(new Color(90, 58, 34));
            g.drawOval(-10, -2, 20, 5);
            g.setColor(new Color(224, 90, 70));                       // a small stripe
            g.fillRect(-2, -1, 5, 3);
            g.setTransform(old);

            // The lantern at the bow glows brighter in the dark
            double lx = b.x + Math.cos(b.angle) * 9;
            double ly = b.y + Math.sin(b.angle) * 9;
            float r = 8f;
            g.setPaint(new RadialGradientPaint(new Point2D.Double(lx, ly), r, new float[]{0f, 1f},
                    new Color[]{new Color(255, 215, 120, (int) (60 + 150 * night)), new Color(255, 215, 120, 0)}));
            g.fill(new Ellipse2D.Double(lx - r, ly - r, r * 2, r * 2));
            g.setColor(new Color(255, 236, 170));
            g.fillRect((int) lx - 1, (int) ly - 1, 3, 3);
        }
    }

    /** A helicopter seen from above: body, tail, a spinning rotor, and a spotlight on the post. */
    private void drawHelicopter(Graphics2D g) {
        double wobble = Math.sin(frames * 0.05) * 1.5;
        double x = heliX;
        double y = heliY + wobble;

        // Spotlight down onto the post
        g.setPaint(new java.awt.GradientPaint((float) x, (float) y, new Color(255, 250, 200, 110),
                (float) postX, (float) postY, new Color(255, 250, 200, 20)));
        g.fillPolygon(new int[]{(int) x - 3, (int) x + 3, (int) postX + 26, (int) postX - 26},
                new int[]{(int) y, (int) y, (int) postY + 10, (int) postY + 10}, 4);

        // Its shadow on the ground
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval((int) x - 10, (int) y + 26, 24, 8);

        AffineTransform old = g.getTransform();
        g.translate(x, y);
        // Tail and tail rotor (the helicopter faces left)
        g.setColor(new Color(150, 158, 172));
        g.fillRect(2, -1, 24, 3);
        g.setColor(new Color(110, 116, 130));
        g.fillRect(24, -4, 3, 9);
        // Body
        g.setColor(new Color(214, 220, 232));
        g.fillOval(-12, -6, 24, 12);
        g.setColor(new Color(70, 110, 150));
        g.fillOval(-12, -4, 9, 8);                 // the cockpit glass
        g.setColor(new Color(220, 60, 60));
        g.fillRect(2, -1, 5, 3);                    // a red stripe
        // Skids
        g.setColor(new Color(60, 64, 76));
        g.drawLine(-9, -8, 8, -8);
        g.drawLine(-9, 8, 8, 8);
        // The main rotor: a faint disc and two blades that turn
        g.setColor(new Color(255, 255, 255, 38));
        g.fill(new Ellipse2D.Double(-22, -22, 44, 44));
        double a = frames * 0.9;
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(40, 44, 54, 210));
        g.drawLine((int) (Math.cos(a) * 22), (int) (Math.sin(a) * 22), (int) (-Math.cos(a) * 22), (int) (-Math.sin(a) * 22));
        g.drawLine((int) (Math.cos(a + Math.PI / 2) * 22), (int) (Math.sin(a + Math.PI / 2) * 22),
                (int) (-Math.cos(a + Math.PI / 2) * 22), (int) (-Math.sin(a + Math.PI / 2) * 22));
        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(40, 44, 54));
        g.fillOval(-2, -2, 4, 4);
        g.setTransform(old);
    }

    private void drawCaption(Graphics2D g) {
        for (Line line : STORY) {
            if (frames < line.from || frames >= line.to) {
                continue;
            }
            double fade = Math.min(1.0, Math.min((frames - line.from) / 30.0, (line.to - frames) / 30.0));
            g.setFont(CAPTION);
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(line.text) + 28;
            int x = (Constants.INTERNAL_WIDTH - w) / 2;
            int y = 300;
            g.setColor(new Color(6, 8, 18, (int) (190 * fade)));
            g.fillRoundRect(x, y - 18, w, 30, 10, 10);
            g.setColor(new Color(255, 232, 170, (int) (255 * fade)));
            g.drawString(line.text, x + 14, y + 2);
        }
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g.setColor(new Color(255, 255, 255, 110));
        g.drawString("ENTER - skip", Constants.INTERNAL_WIDTH - 66, Constants.INTERNAL_HEIGHT - 8);
    }

    private void drawTitleCard(Graphics2D g) {
        double fade = Math.min(1.0, (frames - FINAL_CARD) / 60.0);
        g.setColor(new Color(4, 6, 16, (int) (205 * fade)));
        g.fillRect(0, 0, Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT);
        centered(g, "MAY NATITIRA PA TAYO.", 150, TITLE, new Color(255, 232, 140, (int) (255 * fade)));
        centered(g, "You were never the last tanod.", 182, new Font("SansSerif", Font.PLAIN, 14),
                new Color(255, 255, 255, (int) (230 * fade)));
        if ((frames / 30) % 2 == 0 && fade >= 1.0) {
            centered(g, "Press ENTER", 232, new Font("SansSerif", Font.BOLD, 13), new Color(210, 215, 230));
        }
    }

    private void centered(Graphics2D g, String text, int y, Font font, Color color) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(color);
        g.drawString(text, (Constants.INTERNAL_WIDTH - fm.stringWidth(text)) / 2, y);
    }
}
