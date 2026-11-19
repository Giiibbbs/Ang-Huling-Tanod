package atingdagat.economy;

import atingdagat.core.Constants;
import atingdagat.map.TileMap;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * ResourceNode.java
 *
 * One spot on the map you can harvest: a fish shoal, a bamboo grove, or an
 * ore outcrop. Each spot holds a few units. They refill every dawn.
 * Spots are read from data/nodes.txt, one per line: TYPE column row.
 */
public class ResourceNode {

    public static final int CAPACITY = 4;
    private static final int COOLDOWN = 18;       // frames between harvests, so you cannot spam
    private static final double CLICK_RADIUS = 13;

    private final ResourceType type;
    private final int col;
    private final int row;
    private int remaining = CAPACITY;
    private int cooldown = 0;

    public ResourceNode(ResourceType type, int col, int row) {
        this.type = type;
        this.col = col;
        this.row = row;
    }

    public void update() {
        if (cooldown > 0) {
            cooldown--;
        }
    }

    public boolean isNear(double px, double py) {
        double dx = px - getX();
        double dy = py - getY();
        return dx * dx + dy * dy <= CLICK_RADIUS * CLICK_RADIUS;
    }

    /** Takes one unit. Returns true if the player got something. */
    public boolean harvest() {
        if (remaining <= 0 || cooldown > 0) {
            return false;
        }
        remaining--;
        cooldown = COOLDOWN;
        return true;
    }

    public void refill() {
        remaining = CAPACITY;
    }

    public double getX() {
        return col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    public double getY() {
        return row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    public ResourceType getType() {
        return type;
    }

    public int getRemaining() {
        return remaining;
    }

    // ------------------------------------------------------------------
    // Drawing (simple shapes for now; Jaron's art can replace them)
    // ------------------------------------------------------------------

    public void draw(Graphics2D g, int frame) {
        int x = (int) getX();
        int y = (int) getY();
        boolean empty = remaining <= 0;

        switch (type) {
            case FISH:
                drawFish(g, x, y, frame, empty);
                break;
            case BAMBOO:
                drawBamboo(g, x, y, empty);
                break;
            default:
                drawOre(g, x, y, empty);
                break;
        }

        // Little dots under the node show how much is left.
        for (int i = 0; i < CAPACITY; i++) {
            g.setColor(i < remaining ? Color.WHITE : new Color(0, 0, 0, 110));
            g.fillRect(x - CAPACITY + i * 2 + i, y + 9, 2, 2);
        }
    }

    private void drawFish(Graphics2D g, int x, int y, int frame, boolean empty) {
        double wave = Math.sin(frame * 0.07 + col) * 1.5;
        g.setColor(new Color(255, 255, 255, empty ? 40 : 90));
        g.drawOval(x - 8, y - 5 + (int) wave, 16, 10);
        if (!empty) {
            g.setColor(new Color(255, 190, 90));
            g.fillOval(x - 5, y - 2 - (int) wave, 7, 4);
            g.fillPolygon(new int[]{x + 2, x + 6, x + 6}, new int[]{y - (int) wave, y - 3 - (int) wave, y + 3 - (int) wave}, 3);
            g.setColor(new Color(255, 190, 90));
            g.fillOval(x - 1, y + 3 + (int) wave, 6, 3);
        }
    }

    private void drawBamboo(Graphics2D g, int x, int y, boolean empty) {
        g.setColor(empty ? new Color(120, 120, 80) : new Color(96, 168, 70));
        for (int i = -1; i <= 1; i++) {
            int h = empty ? 3 : 12 - Math.abs(i) * 2;
            g.fillRect(x + i * 4 - 1, y + 6 - h, 3, h);
        }
        g.setColor(new Color(50, 100, 40));
        for (int i = -1; i <= 1; i++) {
            g.drawRect(x + i * 4 - 1, y + 6 - (empty ? 3 : 12 - Math.abs(i) * 2), 2, empty ? 3 : 12 - Math.abs(i) * 2);
        }
    }

    private void drawOre(Graphics2D g, int x, int y, boolean empty) {
        g.setColor(empty ? new Color(100, 100, 106) : new Color(138, 138, 150));
        g.fillPolygon(new int[]{x - 7, x - 3, x + 4, x + 8, x + 5, x - 5}, new int[]{y + 5, y - 5, y - 7, y, y + 6, y + 7}, 6);
        g.setColor(new Color(60, 60, 70));
        g.drawPolygon(new int[]{x - 7, x - 3, x + 4, x + 8, x + 5, x - 5}, new int[]{y + 5, y - 5, y - 7, y, y + 6, y + 7}, 6);
        if (!empty) {
            g.setColor(new Color(255, 220, 120));   // glints of metal
            g.fillRect(x - 2, y - 3, 2, 2);
            g.fillRect(x + 2, y + 1, 2, 2);
        }
    }

    // ------------------------------------------------------------------
    // Loading from data/nodes.txt
    // ------------------------------------------------------------------

    /** Reads the file. Spots on the wrong kind of tile (fish on land, bamboo in the sea) are skipped with a warning. */
    public static ArrayList<ResourceNode> load(String path, TileMap map) {
        ArrayList<ResourceNode> nodes = new ArrayList<>();
        InputStream in = ResourceNode.class.getResourceAsStream(path);
        if (in == null) {
            System.out.println("ResourceNode: file not found (" + path + "), using default spots.");
            addDefaults(nodes, map);
            return nodes;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                addFromLine(nodes, line, map);
            }
        } catch (IOException e) {
            System.out.println("ResourceNode: could not read " + path + ": " + e.getMessage());
        }
        if (nodes.isEmpty()) {
            addDefaults(nodes, map);
        }
        return nodes;
    }

    private static void addFromLine(ArrayList<ResourceNode> nodes, String line, TileMap map) {
        String[] parts = line.split("\\s+");
        try {
            ResourceType type = ResourceType.valueOf(parts[0].toUpperCase());
            int col = Integer.parseInt(parts[1]);
            int row = Integer.parseInt(parts[2]);
            boolean ok = type == ResourceType.FISH ? map.isWater(row, col) : map.isLand(row, col);
            if (ok) {
                nodes.add(new ResourceNode(type, col, row));
            } else {
                System.out.println("ResourceNode: " + type + " does not fit on tile " + col + "," + row + ", skipped.");
            }
        } catch (RuntimeException e) {
            System.out.println("ResourceNode: skipped a bad line: " + line);
        }
    }

    private static void addDefaults(ArrayList<ResourceNode> nodes, TileMap map) {
        nodes.add(new ResourceNode(ResourceType.BAMBOO, 16, 9));
        nodes.add(new ResourceNode(ResourceType.ORE, 25, 9));
        nodes.add(new ResourceNode(ResourceType.FISH, 11, 10));
    }
}
