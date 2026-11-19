package atingdagat.entities;

/**
 * GlowSource.java
 *
 * A light that makes a round pool of light (the lantern, a torch). NightOverlay
 * reads these three numbers to cut a hole in the darkness for each one.
 */
public interface GlowSource {

    double getGlowX();

    double getGlowY();

    double getGlowRadius();
}
