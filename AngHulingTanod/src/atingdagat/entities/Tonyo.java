package atingdagat.entities;

import atingdagat.core.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Tonyo.java
 *
 * The barkada's best shot. He stands at the post and shoots the nearest ship in range, but like
 * you he can only hit what is LIT (a torch, the lantern or the beam must show the ship).
 */
public class Tonyo extends Survivor {

    private int cooldown = 0;
    private int shotTimer = 0;
    private double shotX;
    private double shotY;

    public Tonyo(double startX, double startY, double homeX, double homeY) {
        super("Tonyo", "Sentry", startX, startY, homeX, homeY);
    }

    @Override
    public void act(Post post, Radio radio, List<Warship> ships, int frame) {
        if (cooldown > 0) {
            cooldown--;
        }
        if (shotTimer > 0) {
            shotTimer--;
        }
        if (!isHome() || cooldown > 0) {
            return;
        }
        Warship best = null;
        double bestDist = Constants.TONYO_RANGE;
        for (Warship ship : ships) {
            double d = Math.hypot(ship.getX() - x, ship.getY() - y);
            if (ship.isLit() && !ship.isDead() && d <= bestDist) {
                best = ship;
                bestDist = d;
            }
        }
        if (best != null) {
            best.hit(1);
            cooldown = Constants.TONYO_COOLDOWN;
            shotTimer = 6;
            shotX = best.getX();
            shotY = best.getY();
        }
    }

    @Override
    protected Color getShirt() {
        return new Color(70, 110, 70);   // green jacket
    }

    @Override
    protected void drawExtra(Graphics2D g, int px, int py, int frame) {
        g.setColor(new Color(60, 44, 30));              // rifle across the body
        g.fillRect(px - 6, py - 11, 12, 2);
        g.setColor(new Color(170, 170, 180));
        g.fillRect(px + 5, py - 12, 3, 1);
        g.setColor(new Color(200, 60, 50));             // cap
        g.fillRect(px - 3, py - 22, 6, 2);
        if (shotTimer > 0) {
            g.setColor(new Color(255, 240, 160, 40 * shotTimer));
            g.drawLine(px, py - 11, (int) shotX, (int) shotY);
        }
    }

    @Override
    public String getArrivalTitle() {
        return "A bangka lands: Tonyo, the sentry";
    }

    @Override
    public String getArrivalLine() {
        return "\"Wala nang atrasan. I'll guard the shore with you.\"";
    }
}
