package atingdagat.economy;

import atingdagat.core.Constants;
import atingdagat.map.TileMap;
import atingdagat.util.Assets;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * FishSchool.java
 *
 * A small school of one kind of fish, swimming around the sea. It wanders, stays at the depth its
 * kind likes (tuna in the deep, tamban near the shore), and disappears after a while or when it is
 * all caught. To catch it, the player must follow it with the mouse while holding the button.
 *
 * Implements Harvestable, the same promise a still ResourceNode keeps.
 */
public class FishSchool implements Harvestable {

    private static final double CATCH_RADIUS = 13;
    private static final int LIFETIME = 60 * 28;     // a school stays about 28 seconds

    private final FishKind kind;
    private final BufferedImage sprite;
    private final Random random;

    private double x;
    private double y;
    private double heading;
    private int remaining;
    private int age = 0;
    private double turn = 0;      // how fast the heading is changing right now

    public FishSchool(FishKind kind, double x, double y, Random random) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.random = random;
        this.heading = random.nextDouble() * Math.PI * 2;
        this.remaining = kind.getSchoolSize();
        this.sprite = Assets.get(kind.getResource().getFishPath(), kind.getSpriteW(), kind.getSpriteH());
    }

    /** Swims a little. Stays in water of the right depth and on the screen. */
    public void update(TileMap map) {
        age++;

        // Wander: the turning speed drifts randomly, so the path curves instead of zig-zagging.
        turn += (random.nextDouble() - 0.5) * 0.012;
        turn = Math.max(-0.04, Math.min(0.04, turn)) * 0.98;
        heading += turn;

        double nx = x + Math.cos(heading) * kind.getSpeed();
        double ny = y + Math.sin(heading) * kind.getSpeed();
        if (likesSpot(map, nx, ny)) {
            x = nx;
            y = ny;
        } else {
            heading += Math.PI / 2 + random.nextDouble() * Math.PI;   // blocked: turn away
        }
    }

    /** Is this point water at the right depth, and inside the screen? */
    private boolean likesSpot(TileMap map, double px, double py) {
        if (px < 10 || py < 32 || px > Constants.INTERNAL_WIDTH - 10 || py > Constants.INTERNAL_HEIGHT - 18) {
            return false;
        }
        int col = (int) (px / Constants.TILE_SIZE);
        int row = (int) (py / Constants.TILE_SIZE);
        if (!map.isWater(row, col)) {
            return false;
        }
        int depth = map.getDepth(row, col);
        return depth >= kind.getMinDepth() && depth <= kind.getMaxDepth();
    }

    /** True when it should be removed: all caught, or it swam away. */
    public boolean isGone() {
        return remaining <= 0 || age >= LIFETIME;
    }

    @Override
    public boolean isNear(double px, double py) {
        double dx = px - x;
        double dy = py - y;
        return dx * dx + dy * dy <= CATCH_RADIUS * CATCH_RADIUS;
    }

    @Override
    public boolean isAvailable() {
        return remaining > 0 && age < LIFETIME;
    }

    @Override
    public int harvest() {
        if (remaining <= 0) {
            return 0;
        }
        remaining--;
        return kind.getResource().getYield();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public ResourceType getType() {
        return kind.getResource();
    }

    public FishKind getKind() {
        return kind;
    }

    public int getRemaining() {
        return remaining;
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    public void draw(Graphics2D g, int frame) {
        // Fade in at the start and out at the end.
        double life = Math.min(Math.min(age / 30.0, (LIFETIME - age) / 40.0), 1.0);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0, life)));

        // A soft ripple under the school so it is easy to spot.
        g.setColor(new Color(255, 255, 255, 40));
        g.drawOval((int) x - 11, (int) y - 8, 22, 16);

        // One small fish for every catch left.
        for (int i = 0; i < remaining; i++) {
            double ox = Math.cos(i * 2.4 + 1) * 6;
            double oy = Math.sin(i * 2.4 + 1) * 5 + Math.sin(frame * 0.12 + i) * 0.8;
            drawFish(g, x + ox, y + oy);
        }
        g.setComposite(old);
    }

    private void drawFish(Graphics2D g, double fx, double fy) {
        AffineTransform saved = g.getTransform();
        g.translate(fx, fy);
        g.rotate(heading);
        int w = kind.getSpriteW();
        int h = kind.getSpriteH();
        if (sprite != null) {
            g.drawImage(sprite, -w / 2, -h / 2, null);
        } else {
            Color c = kind.getResource().getColor();
            g.setColor(c);
            g.fillOval(-w / 2, -h / 2, w - 2, h);                                    // body
            g.fillPolygon(new int[]{-w / 2 + 1, -w / 2 - 3, -w / 2 - 3}, new int[]{0, -h / 2, h / 2}, 3);   // tail
            g.setColor(c.darker().darker());
            g.drawOval(-w / 2, -h / 2, w - 2, h);
            g.fillRect(w / 2 - 5, -1, 1, 1);                                          // eye
        }
        g.setTransform(saved);
    }
}
