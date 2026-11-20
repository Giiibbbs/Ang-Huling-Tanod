package atingdagat.map;

import atingdagat.core.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * TileMap.java
 *
 * The island and the sea, stored as a grid (2D array) of tiles.
 *
 * What it does:
 *  1. Loads the map from a text file (one character per tile).
 *  2. Works out how deep each water tile is (distance from the shore).
 *  3. Draws land, shaded water, a shimmer, and foam along the shore.
 *
 * Map file characters:
 *     ~  water
 *     :  sand (beach)
 *     #  grass
 *
 * If the file is missing or broken, a default island is built in code, so the
 * game never crashes because of a map problem.
 */
public class TileMap {

    public static final char WATER = '~';
    public static final char SAND = ':';
    public static final char GRASS = '#';

    // Water further than this many tiles from land is drawn as "deepest". (9 tiles of 10 px is about 6 old tiles of 16 px.)
    private static final int MAX_DEPTH = 9;

    private final int cols = Constants.MAP_COLS;
    private final int rows = Constants.MAP_ROWS;

    private final char[][] tiles = new char[rows][cols]; // what each tile is
    private final int[][] depth = new int[rows][cols];   // distance to land (0 on land)

    /** Loads the map from a file inside the project (works inside the JAR too). */
    public TileMap(String resourcePath) {
        if (!load(resourcePath)) {
            buildDefaultIsland();
        }
        computeDepth();
    }

    // ------------------------------------------------------------------
    // LOADING (file I/O with exception handling)
    // ------------------------------------------------------------------

    private boolean load(String resourcePath) {
        try (InputStream in = TileMap.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                System.err.println("Map file not found: " + resourcePath + " (using default island)");
                return false;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            fillWithWater();

            String line;
            int row = 0;
            while ((line = reader.readLine()) != null && row < rows) {
                // Copy only what fits. Short lines are padded with water, long lines are cut.
                for (int col = 0; col < cols && col < line.length(); col++) {
                    char c = line.charAt(col);
                    if (c == SAND || c == GRASS) {
                        tiles[row][col] = c;
                    }
                }
                row++;
            }

            if (!hasLand()) {
                System.err.println("Map file has no land (using default island)");
                return false;
            }
            return true;

        } catch (IOException e) {
            System.err.println("Could not read map file: " + e.getMessage() + " (using default island)");
            return false;
        }
    }

    private void fillWithWater() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = WATER;
            }
        }
    }

    private boolean hasLand() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (tiles[r][c] != WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Backup island: an oval with a sand ring. Used only if the file can't be loaded. */
    private void buildDefaultIsland() {
        fillWithWater();
        double cx = cols / 2.0;
        double cy = rows / 2.0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double dx = (c - cx) / 9.0;
                double dy = (r - cy) / 5.5;
                double d = dx * dx + dy * dy;
                if (d <= 1.0) {
                    tiles[r][c] = (d > 0.7) ? SAND : GRASS;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // DEPTH: how far is each water tile from the shore?
    // ------------------------------------------------------------------

    /**
     * Ripples outward from the land. Land is depth 0. Water touching land is
     * depth 1, water touching depth 1 is depth 2, and so on. Only loops and
     * arrays are needed.
     */
    private void computeDepth() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                depth[r][c] = isLand(r, c) ? 0 : -1; // -1 = not reached yet
            }
        }
        for (int d = 1; d <= MAX_DEPTH; d++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (depth[r][c] == -1 && touchesDepth(r, c, d - 1)) {
                        depth[r][c] = d;
                    }
                }
            }
        }
        // Anything the ripple never reached is open deep sea.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (depth[r][c] == -1) {
                    depth[r][c] = MAX_DEPTH;
                }
            }
        }
    }

    private boolean touchesDepth(int r, int c, int wanted) {
        return depthAt(r - 1, c) == wanted || depthAt(r + 1, c) == wanted
                || depthAt(r, c - 1) == wanted || depthAt(r, c + 1) == wanted;
    }

    private int depthAt(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            return -2; // outside the map matches nothing
        }
        return depth[r][c];
    }

    // ------------------------------------------------------------------
    // QUESTIONS other classes can ask (ships, defenses, pathing will use these)
    // ------------------------------------------------------------------

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public char getTile(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return WATER; // beyond the edge is sea
        }
        return tiles[row][col];
    }

    public boolean isLand(int row, int col) {
        return getTile(row, col) != WATER;
    }

    public boolean isWater(int row, int col) {
        return !isLand(row, col);
    }

    /**
     * Distance from the shore, in depth units of Constants.DEPTH_UNIT_PX pixels (0 = land).
     * We count in tiles inside the map, then convert, so "nets go 3 deep" means the same number of
     * pixels whatever the tile size is.
     */
    public int getDepth(int row, int col) {
        int d = depthAt(row, col);
        if (d < 0) {
            d = MAX_DEPTH;
        }
        return (d * Constants.TILE_SIZE + Constants.DEPTH_UNIT_PX - 1) / Constants.DEPTH_UNIT_PX;
    }

    // ------------------------------------------------------------------
    // DRAWING
    // ------------------------------------------------------------------

    /** Draws the whole map. "frame" is a counter that makes the water move. */
    public void draw(Graphics2D g, int frame) {
        int ts = Constants.TILE_SIZE;

        // Pass 1: the tiles themselves.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * ts;
                int y = r * ts;
                if (isLand(r, c)) {
                    drawLand(g, r, c, x, y, ts);
                } else {
                    drawWater(g, r, c, x, y, ts, frame);
                }
            }
        }

        // Pass 2: foam on top, so it never gets covered by a neighbor tile.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (isWater(r, c)) {
                    drawFoam(g, r, c, c * ts, r * ts, ts, frame);
                }
            }
        }
    }

    private void drawLand(Graphics2D g, int r, int c, int x, int y, int ts) {
        int h = hash(c, r);
        int shade = (h % 3) * 5 - 5; // a small, steady variation so land isn't flat

        if (getTile(r, c) == SAND) {
            g.setColor(new Color(clamp(224 + shade), clamp(203 + shade), clamp(142 + shade)));
            g.fillRect(x, y, ts, ts);
            if (h % 6 == 0) { // a few darker grains
                g.setColor(new Color(196, 172, 112));
                g.fillRect(x + (h >> 3) % (ts - 2), y + (h >> 6) % (ts - 2), 2, 1);
            }
        } else {
            g.setColor(new Color(clamp(78 + shade), clamp(154 + shade), clamp(76 + shade)));
            g.fillRect(x, y, ts, ts);
            if (h % 5 == 0) { // small tufts of grass
                g.setColor(new Color(52, 120, 56));
                g.fillRect(x + (h >> 3) % (ts - 2), y + (h >> 6) % (ts - 2), 2, 2);
            }
        }
    }

    private void drawWater(Graphics2D g, int r, int c, int x, int y, int ts, int frame) {
        // Shallow (near land) is light teal, deep is dark blue.
        double t = (double) (Math.min(depth[r][c], MAX_DEPTH) - 1) / (MAX_DEPTH - 1);
        int red = lerp(70, 14, t);
        int green = lerp(176, 50, t);
        int blue = lerp(192, 102, t);
        g.setColor(new Color(red, green, blue));
        g.fillRect(x, y, ts, ts);

        // Shimmer: small light dashes that fade in and out.
        int h = hash(c, r);
        if (h % 4 == 0) {
            int phase = (frame / 24 + h) % 6;
            if (phase < 3) {
                g.setColor(new Color(255, 255, 255, 36 + phase * 22));
                g.fillRect(x + (h >> 3) % (ts - 4), y + (h >> 5) % ts, 4, 1);
            }
        }
    }

    private void drawFoam(Graphics2D g, int r, int c, int x, int y, int ts, int frame) {
        boolean up = isLand(r - 1, c);
        boolean down = isLand(r + 1, c);
        boolean left = isLand(r, c - 1);
        boolean right = isLand(r, c + 1);

        boolean upLeft = isLand(r - 1, c - 1);
        boolean upRight = isLand(r - 1, c + 1);
        boolean downLeft = isLand(r + 1, c - 1);
        boolean downRight = isLand(r + 1, c + 1);

        boolean touchesLand = up || down || left || right
                || upLeft || upRight || downLeft || downRight;
        if (!touchesLand) {
            return;
        }

        // The foam breathes in and out like a small wave. The sine makes it smooth.
        double wave = Math.sin(frame * 0.06 + (r + c) * 0.5);
        int w = 1 + (int) Math.round(1.5 * (wave + 1) / 2 * 2); // 1 to 4 pixels wide (tiles are small now)
        int alpha = 120 + (int) (70 * wave);
        g.setColor(new Color(255, 255, 255, alpha));

        if (up) {
            g.fillRect(x, y, ts, w);
        }
        if (down) {
            g.fillRect(x, y + ts - w, ts, w);
        }
        if (left) {
            g.fillRect(x, y, w, ts);
        }
        if (right) {
            g.fillRect(x + ts - w, y, w, ts);
        }

        // Corners: land only touches diagonally, so draw a small square there.
        if (upLeft && !up && !left) {
            g.fillRect(x, y, w, w);
        }
        if (upRight && !up && !right) {
            g.fillRect(x + ts - w, y, w, w);
        }
        if (downLeft && !down && !left) {
            g.fillRect(x, y + ts - w, w, w);
        }
        if (downRight && !down && !right) {
            g.fillRect(x + ts - w, y + ts - w, w, w);
        }
    }

    // ------------------------------------------------------------------
    // SMALL HELPERS
    // ------------------------------------------------------------------

    /** The same tile always gets the same number, so the "random" look never flickers. */
    private static int hash(int c, int r) {
        int h = c * 374761393 + r * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return (h ^ (h >> 16)) & 0x7FFFFFFF;
    }

    private static int lerp(int a, int b, double t) {
        return (int) Math.round(a + (b - a) * t);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
