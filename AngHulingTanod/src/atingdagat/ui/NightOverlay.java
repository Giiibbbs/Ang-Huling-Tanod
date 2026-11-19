package atingdagat.ui;

import atingdagat.core.Constants;
import atingdagat.entities.Beam;
import atingdagat.entities.GlowSource;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * NightOverlay.java
 *
 * Makes the screen dark at night, except where the beam and the lamp shine.
 *
 * How: every frame it paints a dark layer on a see-through image, then ERASES
 * a beam-shaped hole from that layer. The layer is then drawn over the game, so
 * the world shows through only where the light is.
 */
public class NightOverlay {

    // How dark a full night is. Alpha 232 of 255 still lets a hint of the island show.
    private static final int NIGHT_ALPHA = 232;

    private final BufferedImage layer =
            new BufferedImage(Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);

    /** darkness: 0 = full day (nothing drawn), 1 = full night. Dusk and dawn are in between. */
    public void draw(Graphics2D g, Beam beam, double darkness, List<? extends GlowSource> glows) {
        if (darkness <= 0.01) {
            return;
        }
        Graphics2D d = layer.createGraphics();

        // 1. Fill the whole layer with darkness.
        d.setComposite(AlphaComposite.Src);
        d.setColor(new Color(5, 9, 28, (int) (NIGHT_ALPHA * darkness)));
        d.fillRect(0, 0, Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT);

        // 2. Erase light from it. DST_OUT means "remove darkness where we paint".
        d.setComposite(AlphaComposite.DstOut);

        // Several cones, wide and faint to narrow and strong, so the edge of the beam is soft.
        eraseCone(d, beam, 1.8, 0.14);
        eraseCone(d, beam, 1.5, 0.20);
        eraseCone(d, beam, 1.25, 0.28);
        eraseCone(d, beam, 1.0, 0.75);

        // The lamp always lights a circle around itself.
        eraseGlow(d, beam);
        // The lantern and every torch light a round pool too.
        for (GlowSource glow : glows) {
            erasePool(d, glow.getGlowX(), glow.getGlowY(), glow.getGlowRadius());
        }
        d.dispose();

        // 3. Put the darkness on top of the game.
        g.drawImage(layer, 0, 0, null);
    }

    /** Erases a cone. "widen" makes it wider than the real beam, "strength" is how much it erases. */
    private void eraseCone(Graphics2D d, Beam beam, double widen, double strength) {
        double length = beam.getLength();
        double half = Math.toDegrees(beam.getHalfAngle()) * widen;

        // Java's arc angles run counter-clockwise, but our screen angles run clockwise, so we flip the sign.
        Arc2D cone = new Arc2D.Double(beam.getOriginX() - length, beam.getOriginY() - length,
                length * 2, length * 2, -Math.toDegrees(beam.getAngle()) - half, half * 2, Arc2D.PIE);

        // The light fades with distance: strong near the lamp, nothing at the end.
        d.setPaint(new RadialGradientPaint(new Point2D.Double(beam.getOriginX(), beam.getOriginY()),
                (float) length, new float[]{0f, 0.5f, 0.9f, 1f},
                new Color[]{
                    new Color(0, 0, 0, (int) (255 * strength)),
                    new Color(0, 0, 0, (int) (190 * strength)),
                    new Color(0, 0, 0, (int) (20 * strength)),
                    new Color(0, 0, 0, 0)}));
        d.fill(cone);
    }

    private void eraseGlow(Graphics2D d, Beam beam) {
        double r = beam.getLampGlow();
        d.setPaint(new RadialGradientPaint(new Point2D.Double(beam.getOriginX(), beam.getOriginY()),
                (float) r, new float[]{0f, 0.6f, 1f},
                new Color[]{new Color(0, 0, 0, 255), new Color(0, 0, 0, 200), new Color(0, 0, 0, 0)}));
        d.fillOval((int) (beam.getOriginX() - r), (int) (beam.getOriginY() - r), (int) (r * 2), (int) (r * 2));
    }

    private void erasePool(Graphics2D d, double x, double y, double r) {
        d.setPaint(new RadialGradientPaint(new Point2D.Double(x, y), (float) r, new float[]{0f, 0.55f, 1f},
                new Color[]{new Color(0, 0, 0, 255), new Color(0, 0, 0, 210), new Color(0, 0, 0, 0)}));
        d.fillOval((int) (x - r), (int) (y - r), (int) (r * 2), (int) (r * 2));
    }

    /** A warm tint over a pool of light, so it looks like firelight and not just "less dark". */
    public static void drawWarmth(Graphics2D g, GlowSource glow) {
        double r = glow.getGlowRadius();
        g.setPaint(new RadialGradientPaint(new Point2D.Double(glow.getGlowX(), glow.getGlowY()), (float) r,
                new float[]{0f, 1f}, new Color[]{new Color(255, 190, 100, 70), new Color(255, 190, 100, 0)}));
        g.fillOval((int) (glow.getGlowX() - r), (int) (glow.getGlowY() - r), (int) (r * 2), (int) (r * 2));
    }
}
