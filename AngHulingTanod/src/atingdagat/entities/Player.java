package atingdagat.entities;

import atingdagat.core.Constants;
import atingdagat.map.TileMap;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;

/**
 * Player.java
 *
 * The tanod: the one person left on Isla Dulo. Walks on land only (never in the sea),
 * faces the way it last moved, and carries the lantern.
 *
 * Position (x, y) is where the tanod's FEET are, in game pixels.
 *
 * Art: res/player/down.png, up.png, left.png, right.png (12x16). Each may have a
 * second walking frame, down2.png and so on. Missing art is drawn with simple shapes.
 */
public class Player {

    public enum Facing {
        DOWN("down"), UP("up"), LEFT("left"), RIGHT("right");

        private final String file;

        Facing(String file) {
            this.file = file;
        }
    }

    private static final int SPRITE_W = 12;
    private static final int SPRITE_H = 16;
    private static final double FOOT_HALF_WIDTH = 4;   // the feet are a small box, so we can squeeze past things

    private final EnumMap<Facing, BufferedImage[]> frames = new EnumMap<>(Facing.class);

    private double x;
    private double y;
    private Facing facing = Facing.DOWN;
    private boolean moving = false;
    private int walkTimer = 0;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        for (Facing f : Facing.values()) {
            BufferedImage a = Assets.get("/atingdagat/res/player/" + f.file + ".png", SPRITE_W, SPRITE_H);
            BufferedImage b = Assets.get("/atingdagat/res/player/" + f.file + "2.png", SPRITE_W, SPRITE_H);
            frames.put(f, new BufferedImage[]{a, b});
        }
    }

    /**
     * Walks one frame. dx and dy are -1, 0 or 1 (the keys). X and Y are tried separately,
     * so the tanod slides along the shore instead of getting stuck on it.
     */
    public void update(TileMap map, int dx, int dy) {
        moving = dx != 0 || dy != 0;
        if (!moving) {
            walkTimer = 0;
            return;
        }
        walkTimer++;
        double length = Math.hypot(dx, dy);   // walking diagonally must not be faster
        double stepX = dx / length * Constants.PLAYER_SPEED;
        double stepY = dy / length * Constants.PLAYER_SPEED;

        if (canStand(map, x + stepX, y)) {
            x += stepX;
        }
        if (canStand(map, x, y + stepY)) {
            y += stepY;
        }
        if (Math.abs(dx) >= Math.abs(dy) && dx != 0) {
            facing = dx > 0 ? Facing.RIGHT : Facing.LEFT;
        } else {
            facing = dy > 0 ? Facing.DOWN : Facing.UP;
        }
    }

    /** Both feet must be on land. */
    private boolean canStand(TileMap map, double nx, double ny) {
        return isLandAt(map, nx - FOOT_HALF_WIDTH, ny) && isLandAt(map, nx + FOOT_HALF_WIDTH, ny);
    }

    private boolean isLandAt(TileMap map, double px, double py) {
        int col = (int) Math.floor(px / Constants.TILE_SIZE);
        int row = (int) Math.floor(py / Constants.TILE_SIZE);
        return map.isLand(row, col);
    }

    public void draw(Graphics2D g, int frameCount) {
        int px = (int) Math.round(x);
        int py = (int) Math.round(y);
        int bob = (moving && (walkTimer / 8) % 2 == 1) ? -1 : 0;   // a tiny hop makes it look like walking

        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(px - 5, py - 2, 10, 4);

        BufferedImage[] set = frames.get(facing);
        BufferedImage sprite = set[0];
        if (moving && set[1] != null && (walkTimer / 8) % 2 == 1) {
            sprite = set[1];   // second walking frame, if the artist made one
        }
        if (sprite != null) {
            g.drawImage(sprite, px - SPRITE_W / 2, py - SPRITE_H + 2 + bob, null);
            return;
        }
        drawPlaceholder(g, px, py + bob);
    }

    /** A little tanod made of shapes: salakot hat, shirt, and the lantern on the side he faces. */
    private void drawPlaceholder(Graphics2D g, int px, int py) {
        g.setColor(new Color(40, 60, 110));          // shirt
        g.fillRect(px - 4, py - 9, 8, 8);
        g.setColor(new Color(60, 50, 40));           // legs
        g.fillRect(px - 3, py - 1, 2, 2);
        g.fillRect(px + 1, py - 1, 2, 2);
        g.setColor(new Color(224, 180, 140));        // face
        g.fillRect(px - 3, py - 14, 6, 5);
        g.setColor(new Color(196, 158, 90));         // salakot (round hat)
        g.fillRect(px - 6, py - 16, 12, 3);
        g.fillRect(px - 3, py - 18, 6, 2);
        if (facing == Facing.DOWN) {
            g.setColor(new Color(40, 30, 30));
            g.fillRect(px - 2, py - 12, 1, 1);
            g.fillRect(px + 1, py - 12, 1, 1);
        }
        int lx = facing == Facing.LEFT ? px - 7 : (facing == Facing.RIGHT ? px + 5 : px + 4);
        int ly = py - 6;
        g.setColor(new Color(255, 200, 90));         // the lantern
        g.fillRect(lx, ly, 3, 4);
        g.setColor(new Color(120, 80, 40));
        g.drawRect(lx, ly, 3, 4);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Facing getFacing() {
        return facing;
    }
}
