package atingdagat.entities;

import atingdagat.core.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import atingdagat.util.Assets;

/**
 * Post.java
 *
 * The grounded ship that is the island's last command post. It has health
 * (the island's health) and carries the radio mast. Enemy landings hurt it.
 * If its health reaches zero the game is lost.
 */
public class Post {

    public static final int MAX_HEALTH = 20;

    private static final String SPRITE_PATH = "/atingdagat/res/buildings/post.png";
    private static final int SPRITE_W = 40;
    private static final int SPRITE_H = 32;
    private final BufferedImage sprite = Assets.get(SPRITE_PATH, SPRITE_W, SPRITE_H);

    private final int col;
    private final int row;
    private int health = MAX_HEALTH;

    public Post(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public void damage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void repair(int amount) {
        health = Math.min(MAX_HEALTH, health + amount);
    }

    public int getHealth() {
        return health;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public double getX() {
        return col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    public double getY() {
        return row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    public void draw(Graphics2D g, int frame, boolean radioAlive) {
        int x = (int) getX();
        int y = (int) getY();

        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x - 15, y + 2, 30, 9);

        if (sprite != null) {
            g.drawImage(sprite, x - SPRITE_W / 2, y + 8 - SPRITE_H, null);
            boolean on = radioAlive && (frame / 30) % 2 == 0;
            if (on) {   // the blinking light still shows the post's health
                g.setColor(health > MAX_HEALTH / 3 ? new Color(120, 255, 140) : new Color(255, 80, 70));
                g.fillOval(x + 1, y - 27, 4, 4);
            }
            return;
        }

        // Beached hull
        Polygon hull = new Polygon(new int[]{x - 15, x + 14, x + 18, x + 10, x - 13},
                new int[]{y - 3, y - 3, y + 2, y + 7, y + 7}, 5);
        g.setColor(new Color(110, 96, 82));
        g.fillPolygon(hull);
        g.setColor(new Color(52, 44, 38));
        g.drawPolygon(hull);

        // Cabin
        g.setColor(new Color(170, 150, 120));
        g.fillRect(x - 6, y - 10, 12, 8);
        g.setColor(new Color(52, 44, 38));
        g.drawRect(x - 6, y - 10, 12, 8);

        // Radio mast with a blinking light (red when the health is low)
        g.setColor(new Color(60, 56, 52));
        g.drawLine(x + 2, y - 10, x + 2, y - 24);
        g.drawLine(x - 1, y - 20, x + 5, y - 20);
        boolean blink = (frame / 30) % 2 == 0;
        if (radioAlive && blink) {
            g.setColor(health > MAX_HEALTH / 3 ? new Color(120, 255, 140) : new Color(255, 80, 70));
            g.fillOval(x, y - 27, 5, 5);
        }
    }
}
