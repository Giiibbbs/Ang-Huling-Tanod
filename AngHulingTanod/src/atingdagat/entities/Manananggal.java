package atingdagat.entities;

import atingdagat.core.Constants;
import atingdagat.map.TileMap;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Polygon;
import java.util.List;

/**
 * Manananggal.java
 *
 * In the old stories she is a predator that splits in two and flies at night. In this game she is
 * a story twist: the Kordans hunted her too, and she joins you. She is a Survivor like the others
 * (the world treats her the same), but she FLIES: she ignores the sea when she moves, does not work
 * in the day for meals, and at night she swoops on the nearest ship in range, hits it, and
 * returns to her roost over the post.
 *
 * Only act() and update() and draw() differ from a walking survivor. That is the point of the abstract class.
 */
public class Manananggal extends Survivor implements GlowSource {

    private enum Mode { ROOST, SWOOP, RETURN }

    private final BufferedImage art = Assets.get("/atingdagat/res/monsters/manananggal.png", 30, 26);
    private final double roostX;
    private final double roostY;
    private Mode mode = Mode.ROOST;
    private Warship prey = null;
    private int cooldown = 0;
    private boolean facingLeft = false;

    public Manananggal(double startX, double startY, double roostX, double roostY) {
        super("Manananggal", "Night flyer", startX, startY, roostX, roostY);
        this.roostX = roostX;
        this.roostY = roostY;
    }

    @Override
    public boolean needsWage() {
        return false;
    }

    /** She flies straight to where she is going, over water or land. */
    @Override
    public void update(TileMap map) {
        double gx = roostX;
        double gy = roostY;
        if (mode == Mode.SWOOP && prey != null) {
            gx = prey.getX();
            gy = prey.getY() - 6;
        }
        double dx = gx - x;
        double dy = gy - y;
        double dist = Math.hypot(dx, dy);
        double speed = mode == Mode.SWOOP ? Constants.MANANANGGAL_SPEED * 1.6 : Constants.MANANANGGAL_SPEED;
        if (dist <= speed) {
            x = gx;
            y = gy;
        } else {
            x += dx / dist * speed;
            y += dy / dist * speed;
            facingLeft = dx < 0;
        }
    }

    @Override
    public void act(Post post, Radio radio, List<Warship> ships, int frame) {
        if (cooldown > 0) {
            cooldown--;
        }
        switch (mode) {
            case ROOST:
                if (isHome() && cooldown == 0) {
                    prey = nearestShip(ships);
                    if (prey != null) {
                        mode = Mode.SWOOP;
                    }
                }
                break;
            case SWOOP:
                if (prey == null || prey.isDead()) {
                    mode = Mode.RETURN;
                } else if (Math.hypot(prey.getX() - x, prey.getY() - 6 - y) < 8) {
                    prey.hit(Constants.MANANANGGAL_DAMAGE);
                    cooldown = Constants.MANANANGGAL_COOLDOWN;
                    mode = Mode.RETURN;
                }
                break;
            default: // RETURN
                if (isHome()) {
                    mode = Mode.ROOST;
                }
                break;
        }
    }

    private Warship nearestShip(List<Warship> ships) {
        Warship best = null;
        double bestDist = Constants.MANANANGGAL_RANGE;
        for (Warship ship : ships) {
            double d = Math.hypot(ship.getX() - roostX, ship.getY() - roostY);
            if (!ship.isDead() && d <= bestDist) {
                best = ship;
                bestDist = d;
            }
        }
        return best;
    }

    @Override
    protected Color getShirt() {
        return new Color(225, 215, 225);
    }

    @Override
    protected void drawExtra(Graphics2D g, int px, int py, int frame) {
        // not used: she draws herself in draw()
    }

    @Override
    public void draw(Graphics2D g, int frame) {
        int px = (int) x;
        int py = (int) y;
        boolean flying = mode != Mode.ROOST || !isHome();
        int bob = (int) Math.round(Math.sin(frame * 0.12) * 2);
        py += bob;
        g.setColor(new Color(0, 0, 0, 35));                       // shadow far below
        g.fillOval(px - 6, (int) roostY + 30, 12, 4);
        if (art != null) {
            g.drawImage(art, px - 15, py - 18, null);
            return;
        }
        int dir = facingLeft ? -1 : 1;
        double flap = Math.sin(frame * (flying ? 0.5 : 0.12));
        int wy = (int) Math.round(flap * 5);
        g.setColor(new Color(58, 36, 70, 235));                    // two big bat wings
        for (int side = -1; side <= 1; side += 2) {
            Polygon wing = new Polygon();
            wing.addPoint(px + side * 2, py - 10);
            wing.addPoint(px + side * 16, py - 18 - wy);
            wing.addPoint(px + side * 14, py - 8);
            wing.addPoint(px + side * 9, py - 3 + wy / 2);
            wing.addPoint(px + side * 4, py - 2);
            g.fillPolygon(wing);
        }
        g.setColor(new Color(225, 212, 222));                      // pale body
        g.fillRect(px - 3, py - 12, 6, 9);
        g.fillOval(px - 3, py - 19, 6, 7);                         // head
        g.setColor(new Color(20, 14, 22));
        g.fillRect(px - 4, py - 20, 8, 3);                         // long black hair
        g.fillRect(px - dir * 5 - 1, py - 18, 2, 14);              // streaming behind her
        g.setColor(new Color(255, 200, 220));
        g.fillRect(px + dir - 1, py - 16, 1, 1);                   // eye
        g.setColor(new Color(190, 150, 220, 110));                 // she ends in a faint wisp, nothing more
        g.fillRect(px - 2, py - 3, 4, 2);
        g.setColor(new Color(190, 150, 220, 60));
        g.fillRect(px - 1, py - 1, 2, 3);
    }

    @Override
    public String getArrivalTitle() {
        return "A wing in the dark: the Manananggal";
    }

    @Override
    public String getArrivalLine() {
        return "\"The Kordans hunted me too. Pareho tayong tinataboy ng dilim. Let me hunt them at night.\"";
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
        return 18;
    }
}
