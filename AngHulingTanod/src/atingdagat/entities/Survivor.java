package atingdagat.entities;

import atingdagat.core.Constants;
import atingdagat.map.TileMap;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Survivor.java
 *
 * A person who escaped the Kordan blockade by bangka and joined the post. Survivor is
 * ABSTRACT: you can never write "new Survivor()", because "a survivor" is an idea, and each
 * real survivor has a job. Ka Inday heals, Migo keeps the radio strong, Tonyo shoots.
 *
 * What every survivor shares lives here (walking home, drawing the body). What is different
 * is one method, act(). The world calls act() on each survivor every frame and does not care
 * who is who. That is polymorphism, the same idea as Warship and Defense.
 */
public abstract class Survivor {

    protected double x;
    protected double y;
    private final double homeX;
    private final double homeY;
    private final String name;
    private final String role;
    private final BufferedImage sprite;   // optional art: /atingdagat/res/survivors/<name>.png (18 x 22)
    private boolean facingLeft = false;

    protected Survivor(String name, String role, double startX, double startY, double homeX, double homeY) {
        this.name = name;
        this.role = role;
        this.x = startX;
        this.y = startY;
        this.homeX = homeX;
        this.homeY = homeY;
        this.sprite = Assets.get("/atingdagat/res/survivors/" + name.toLowerCase().replace(" ", "") + ".png", 18, 22);
    }

    /** This survivor's job, done once per frame. The Post and the Radio are what Ka Inday and Migo help. */
    public abstract void act(Post post, Radio radio, List<Warship> ships, int frame);

    /** Does having this survivor make supply crates come faster? (Only Migo says yes.) */
    public boolean boostsCrates() {
        return false;
    }

    /** The colour of the shirt, so each person looks different even without art. */
    protected abstract Color getShirt();

    /** A small extra drawn over the body: a headband, a headset, a rifle. */
    protected abstract void drawExtra(Graphics2D g, int px, int py, int frame);

    /** The title line shown when the bangka lands. */
    public abstract String getArrivalTitle();

    /** What the survivor says when they land. */
    public abstract String getArrivalLine();

    /** Walks toward home. Stops at home. Never steps into the sea. */
    public void update(TileMap map) {
        double dx = homeX - x;
        double dy = homeY - y;
        double dist = Math.hypot(dx, dy);
        if (dist <= Constants.SURVIVOR_WALK_SPEED) {
            x = homeX;
            y = homeY;
            return;
        }
        double nx = x + dx / dist * Constants.SURVIVOR_WALK_SPEED;
        double ny = y + dy / dist * Constants.SURVIVOR_WALK_SPEED;
        int col = (int) (nx / Constants.TILE_SIZE);
        int row = (int) (ny / Constants.TILE_SIZE);
        if (map.isLand(row, col)) {
            facingLeft = dx < 0;
            x = nx;
            y = ny;
        } else {
            x = homeX;   // the way is blocked by water: just arrive
            y = homeY;
        }
    }

    public boolean isHome() {
        return x == homeX && y == homeY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public void draw(Graphics2D g, int frame) {
        int px = (int) x;
        int py = (int) y;
        boolean walking = !isHome();
        int bob = walking ? ((frame / 8) % 2) : 0;
        g.setColor(new Color(0, 0, 0, 55));
        g.fillOval(px - 5, py - 2, 10, 4);
        if (sprite != null) {
            g.drawImage(sprite, px - 9, py - 20 - bob, null);
        } else {
            g.setColor(new Color(60, 50, 50));                   // legs
            g.fillRect(px - 3, py - 6 - bob, 2, 6);
            g.fillRect(px + 1, py - 6 - bob, 2, 6);
            g.setColor(getShirt());                              // body
            g.fillRect(px - 4, py - 14 - bob, 8, 9);
            g.setColor(new Color(236, 192, 150));                // head
            g.fillRect(px - 3, py - 20 - bob, 6, 6);
            g.setColor(new Color(40, 28, 24));                   // hair
            g.fillRect(px - 3, py - 21 - bob, 6, 2);
            g.setColor(new Color(20, 14, 14));
            g.drawRect(px - 4, py - 14 - bob, 7, 8);             // outline
            int eye = facingLeft ? -2 : 1;
            g.fillRect(px + eye, py - 17 - bob, 1, 1);
        }
        drawExtra(g, px, py - bob, frame);
    }
}
