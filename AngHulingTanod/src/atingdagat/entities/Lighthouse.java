package atingdagat.entities;

import atingdagat.core.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * Lighthouse.java
 *
 * The tower that holds the lamp. The beam starts at the lamp. For now it is
 * drawn with simple shapes. Jaron's lighthouse sprite will replace them later.
 */
public class Lighthouse {

    private static final int TOWER_HEIGHT = 28;
    private static final int TOWER_BOTTOM_WIDTH = 12;
    private static final int TOWER_TOP_WIDTH = 8;

    private final double baseX; // centre of the bottom of the tower
    private final double baseY;

    public Lighthouse(int col, int row) {
        this.baseX = col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
        this.baseY = row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0 + 6;
    }

    public double getBaseX() {
        return baseX;
    }

    public double getBaseY() {
        return baseY;
    }

    /** The beam starts from the lamp, near the top of the tower. */
    public double getLampX() {
        return baseX;
    }

    public double getLampY() {
        return baseY - TOWER_HEIGHT + 4;
    }

    public void draw(Graphics2D g, boolean night) {
        int bx = (int) Math.round(baseX);
        int by = (int) Math.round(baseY);
        int top = by - TOWER_HEIGHT;

        // Shadow on the ground
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(bx - 9, by - 3, 18, 7);

        // Tower body (a slightly narrower top gives it a tapered look)
        Polygon body = new Polygon();
        body.addPoint(bx - TOWER_BOTTOM_WIDTH / 2, by);
        body.addPoint(bx + TOWER_BOTTOM_WIDTH / 2, by);
        body.addPoint(bx + TOWER_TOP_WIDTH / 2, top);
        body.addPoint(bx - TOWER_TOP_WIDTH / 2, top);
        g.setColor(new Color(236, 232, 220));
        g.fillPolygon(body);

        // Two red bands
        g.setColor(new Color(176, 58, 52));
        g.fillRect(bx - 5, by - 20, 10, 4);
        g.fillRect(bx - 4, by - 9, 8, 4);

        // Outline
        g.setColor(new Color(70, 60, 56));
        g.drawPolygon(body);

        // Lamp room: bright at night, pale in the day
        g.setColor(night ? new Color(255, 242, 160) : new Color(228, 220, 170));
        g.fillRect(bx - 4, top - 6, 8, 6);
        g.setColor(new Color(70, 60, 56));
        g.drawRect(bx - 4, top - 6, 8, 6);

        // Roof
        g.setColor(new Color(120, 44, 44));
        g.fillRect(bx - 5, top - 9, 10, 3);
    }
}
