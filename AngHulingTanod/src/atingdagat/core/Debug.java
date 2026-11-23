package atingdagat.core;

/**
 * Debug.java
 *
 * The developer keys (N skip phase, L last night, E eclipse, K crate, H monsters, J survivors) are
 * switched OFF in the real game so a player cannot skip nights by accident. F12 toggles them for
 * testing and for the demo.
 */
public final class Debug {

    private static boolean keysOn = false;

    private Debug() {
    }

    public static boolean isKeysOn() {
        return keysOn;
    }

    public static void toggle() {
        keysOn = !keysOn;
    }
}
