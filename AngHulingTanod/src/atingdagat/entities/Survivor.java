package atingdagat.entities;

import atingdagat.core.Constants;
import atingdagat.economy.ResourceNode;
import atingdagat.economy.ResourceType;
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

    // Day work. The walking goal is home unless there is a job.
    private enum Job { IDLE, GOING, CHOPPING, CARRYING }
    private double destX;
    private double destY;
    private Job job = Job.IDLE;
    private ResourceNode target = null;
    private int chopTimer = 0;
    private int carrying = 0;
    private ResourceType carryType = ResourceType.BAMBOO;
    private ResourceType delivered = ResourceType.BAMBOO;
    private int loadsToday = 0;
    private boolean paid = false;

    protected Survivor(String name, String role, double startX, double startY, double homeX, double homeY) {
        this.name = name;
        this.role = role;
        this.x = startX;
        this.y = startY;
        this.homeX = homeX;
        this.homeY = homeY;
        this.destX = homeX;
        this.destY = homeY;
        this.sprite = Assets.get("/atingdagat/res/survivors/" + name.toLowerCase().replace(" ", "") + ".png", 18, 22);
    }

    /** This survivor's job, done once per frame. The Post and the Radio are what Ka Inday and Migo help. */
    public abstract void act(Post post, Radio radio, List<Warship> ships, int frame);

    /** Does this survivor work in the day for a wage of meals? (The manananggal does not.) */
    public boolean needsWage() {
        return true;
    }

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

    /** Walks toward the current goal (home, or the tree being worked). Stops there. Never steps into the sea. */
    public void update(TileMap map) {
        double dx = destX - x;
        double dy = destY - y;
        double dist = Math.hypot(dx, dy);
        if (dist <= Constants.SURVIVOR_WALK_SPEED) {
            x = destX;
            y = destY;
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
            x = destX;   // the way is blocked by water: just arrive
            y = destY;
        }
    }

    // ------------------------------------------------------------------
    // DAY WORK: chop a tree or bamboo, carry it home, again (up to the daily quota)
    // ------------------------------------------------------------------

    /** The start of a day: pay (or not) and reset the quota. */
    public void startDay(boolean wagePaid) {
        paid = wagePaid;
        loadsToday = 0;
    }

    /** What the last delivered load was (bamboo from a narra, fruit from a mango). */
    public ResourceType getDeliveredType() {
        return delivered;
    }

    /** Called at the start of every night. Most survivors do not care. */
    public void newNight() {
    }

    public boolean isPaid() {
        return paid;
    }

    public boolean isChopping() {
        return job == Job.CHOPPING;
    }

    /** Is there a straight walk over land between these two points? (Checked every few pixels.) */
    private static boolean landBetween(TileMap map, double x1, double y1, double x2, double y2) {
        double dist = Math.hypot(x2 - x1, y2 - y1);
        int steps = (int) (dist / 3) + 1;
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int col = (int) ((x1 + (x2 - x1) * t) / Constants.TILE_SIZE);
            int row = (int) ((y1 + (y2 - y1) * t) / Constants.TILE_SIZE);
            if (!map.isLand(row, col)) {
                return false;
            }
        }
        return true;
    }

    /** Looks for the nearest tree or bamboo that nobody else is working and that can be walked to over land. */
    private ResourceNode findJob(TileMap map, List<ResourceNode> nodes, List<Survivor> crew, double postX, double postY) {
        ResourceNode best = null;
        double bestDist = Double.MAX_VALUE;
        for (ResourceNode node : nodes) {
            ResourceType type = node.getType();
            if ((!type.isTree() && type != ResourceType.BAMBOO) || !node.isAvailable()) {
                continue;
            }
            if (Math.hypot(node.getX() - postX, node.getY() - postY) > Constants.WORKER_SEARCH) {
                continue;
            }
            boolean taken = false;
            for (Survivor other : crew) {
                if (other != this && other.target == node) {
                    taken = true;
                }
            }
            double d = Math.hypot(node.getX() - x, node.getY() - y);
            if (!taken && d < bestDist
                    && landBetween(map, x, y, node.getX(), node.getY())
                    && landBetween(map, node.getX(), node.getY(), homeX, homeY)) {
                best = node;
                bestDist = d;
            }
        }
        return best;
    }

    /**
     * Called every frame by the world. "onShift" is true in the day for a survivor who was paid.
     * Returns the bamboo delivered this frame (usually 0, and 3 when a load arrives home).
     */
    public int work(boolean onShift, TileMap map, List<ResourceNode> nodes, List<Survivor> crew, double postX, double postY) {
        if (!needsWage()) {
            return 0;
        }
        boolean working = onShift && paid;
        switch (job) {
            case IDLE:
                if (working && loadsToday < Constants.WORKER_QUOTA && isHome()) {
                    target = findJob(map, nodes, crew, postX, postY);
                    if (target != null) {
                        job = Job.GOING;
                        destX = target.getX();
                        destY = target.getY();
                    }
                } else if (!working) {
                    destX = homeX;
                    destY = homeY;
                }
                break;
            case GOING:
                if (!working || !target.isAvailable()) {
                    giveUp();
                } else if (Math.hypot(x - destX, y - destY) < 2) {
                    job = Job.CHOPPING;
                    chopTimer = 0;
                }
                break;
            case CHOPPING:
                if (!working || !target.isAvailable()) {
                    giveUp();
                    break;
                }
                chopTimer++;
                if (chopTimer >= Constants.WORKER_CHOP_FRAMES) {
                    carrying = target.harvest();
                    carryType = target.getType().getStock();
                    loadsToday++;
                    target = null;
                    job = Job.CARRYING;
                    destX = homeX;
                    destY = homeY;
                }
                break;
            default: // CARRYING
                if (isHome()) {
                    int load = carrying;
                    delivered = carryType;
                    carrying = 0;
                    job = Job.IDLE;
                    return load;
                }
                break;
        }
        return 0;
    }

    private void giveUp() {
        target = null;
        job = Job.IDLE;
        destX = homeX;
        destY = homeY;
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
        if (job == Job.CHOPPING) {                       // an axe swing
            int swing = (frame / 10) % 2;
            g.setColor(new Color(150, 110, 70));
            g.drawLine(px + 4, py - 12, px + 8 - swing * 3, py - 18 + swing * 6);
            g.setColor(new Color(210, 215, 225));
            g.fillRect(px + 7 - swing * 3, py - 20 + swing * 6, 3, 2);
        }
        if (carrying > 0) {                              // a bundle of bamboo on the shoulder
            g.setColor(new Color(120, 190, 90));
            g.fillRect(px - 7, py - 20, 3, 12);
            g.fillRect(px - 5, py - 22, 3, 12);
            g.setColor(new Color(60, 100, 50));
            g.drawRect(px - 7, py - 20, 3, 12);
        }
    }
}
