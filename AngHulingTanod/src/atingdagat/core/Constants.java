package atingdagat.core;

/**
 * Constants.java
 *
 * One place for the numbers the whole game uses. If you need to change the
 * window size, the tile size, or how many nights the game lasts, change it
 * here and every class picks it up.
 */
public final class Constants {

    // Utility class: nobody should create an object of it.
    private Constants() {
    }

    public static final String TITLE = "Ang Huling Tanod";

    // The game is drawn on a small "internal" screen of 640 x 360 pixels,
    // then scaled up to fit the real window. This keeps the art sharp and
    // lets the window be resized without breaking the game.
    public static final int INTERNAL_WIDTH = 640;
    public static final int INTERNAL_HEIGHT = 360;

    // Size of one map tile (in internal pixels). The island map will be a grid of these.
    public static final int TILE_SIZE = 16;

    // The map covers the whole internal screen: 40 columns x 23 rows of tiles
    // (22.5 rows fit, so the last row is cut in half and that is fine).
    public static final int MAP_COLS = INTERNAL_WIDTH / TILE_SIZE;
    public static final int MAP_ROWS = (INTERNAL_HEIGHT + TILE_SIZE - 1) / TILE_SIZE;

    // Where the lighthouse stands on the island (tile column and row).
    public static final int LIGHTHOUSE_COL = 19;
    public static final int LIGHTHOUSE_ROW = 10;

    // The map file, stored inside the project so it also works from the JAR.
    public static final String MAP_FILE = "/atingdagat/data/island.txt";

    // How many times per second the game updates and redraws.
    public static final int TARGET_FPS = 60;

    // The game lasts 7 nights.
    public static final int TOTAL_NIGHTS = 7;

    // The size the window opens at (the player can resize it).
    public static final int WINDOW_START_WIDTH = 1280;
    public static final int WINDOW_START_HEIGHT = 720;

    // true = smooth scaling, false = crisp pixels. Try both and keep what looks better.
    public static final boolean SMOOTH_SCALING = false;
}
