package atingdagat.economy;

/**
 * Harvestable.java
 *
 * An interface for "something you can collect by holding the mouse on it".
 * A ResourceNode (bamboo, ore) sits still. A FishSchool swims. They are very
 * different inside, but GameWorld treats them the same through this interface.
 */
public interface Harvestable {

    /** Is this point (the mouse) close enough to collect from it? */
    boolean isNear(double x, double y);

    ResourceType getType();

    /** False when there is nothing left to take right now. */
    boolean isAvailable();

    /** How long the mouse must be held on it (in frames). Most things use the number in their ResourceType. */
    default int getHoldFrames() {
        return getType().getHoldFrames();
    }

    /** Takes one portion and returns how many items the player gets. */
    int harvest();

    double getX();

    double getY();
}
