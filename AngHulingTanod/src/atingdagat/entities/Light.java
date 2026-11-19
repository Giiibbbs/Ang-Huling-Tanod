package atingdagat.entities;

/**
 * Light.java
 *
 * "Anything that can light a spot of the night." The lighthouse beam, the tanod's
 * lantern and a torch post all promise this one method, so a ship only has to ask
 * "am I lit?" and never cares WHICH light found it. (An interface again, like Defense.)
 */
public interface Light {

    /** True if this light shines on the point (x, y). */
    boolean lights(double x, double y);
}
