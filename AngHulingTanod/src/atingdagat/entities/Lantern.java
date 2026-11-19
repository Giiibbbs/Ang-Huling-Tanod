package atingdagat.entities;

import atingdagat.core.Constants;

/**
 * Lantern.java
 *
 * The small light the tanod carries. It lights a circle around the tanod, so ships
 * that sneak close to the shore can still be seen and shot. It has no body of its own:
 * it just follows the Player.
 */
public class Lantern implements Light, GlowSource {

    private final Player owner;

    public Lantern(Player owner) {
        this.owner = owner;
    }

    @Override
    public boolean lights(double x, double y) {
        return Math.hypot(x - getGlowX(), y - getGlowY()) <= Constants.LANTERN_RADIUS;
    }

    @Override
    public double getGlowX() {
        return owner.getX();
    }

    @Override
    public double getGlowY() {
        return owner.getY() - 6;   // held at about waist height
    }

    @Override
    public double getGlowRadius() {
        return Constants.LANTERN_RADIUS;
    }
}
