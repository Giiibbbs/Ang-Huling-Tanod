package atingdagat.economy;

import atingdagat.core.Constants;
import atingdagat.map.TileMap;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * ResourceNode.java
 *
 * A spot on the LAND you can harvest: a bamboo grove or an ore vein. It holds a limited number of
 * units. Bamboo grows back slowly. Ore NEVER grows back, so every vein is a choice.
 * You collect by HOLDING the mouse on it (GameWorld counts the hold).
 * Spots are read from data/nodes.txt, one per line: TYPE column row units.
 *
 * Implements Harvestable, the same promise a swimming FishSchool keeps.
 */
public class ResourceNode implements Harvestable {

    private static final int DEFAULT_UNITS = 4;
    private static final double CLICK_RADIUS = 13;
    private static final int TREE_W = 20;
    private static final int TREE_H = 28;

    private final ResourceType type;
    private final BufferedImage sprite;
    private final int col;
    private final int row;
    private final int maxUnits;
    private int remaining;
    private int regrowTimer = 0;

    public ResourceNode(ResourceType type, int col, int row, int units) {
        this.type = type;
        this.col = col;
        this.row = row;
        this.maxUnits = units;
        this.remaining = units;
        this.sprite = loadSprite(type);
    }

    /** The art for this spot. If there is no picture for this exact ore, we recolor the shared ore picture. */
    private static BufferedImage loadSprite(ResourceType type) {
        if (type == ResourceType.TREE) {
            return Assets.get(type.getSpritePath(), TREE_W, TREE_H);   // trees are taller than a tile
        }
        BufferedImage own = Assets.get(type.getSpritePath(), 16, 16);
        if (own != null || type.getCategory() != ResourceType.Category.ORE) {
            return own;
        }
        return Assets.tint(Assets.get("/atingdagat/res/nodes/ore.png", 16, 16), type.getColor(), 0.55);
    }

    public void update() {
        if (remaining < maxUnits && type.getRegrowFrames() > 0) {
            regrowTimer++;
            if (regrowTimer >= type.getRegrowFrames()) {
                regrowTimer = 0;
                remaining++;
            }
        }
    }

    @Override
    public boolean isNear(double px, double py) {
        double dx = px - getX();
        double dy = py - (type == ResourceType.TREE ? getY() - 8 : getY());   // click anywhere on the leaves
        return dx * dx + dy * dy <= CLICK_RADIUS * CLICK_RADIUS;
    }

    @Override
    public boolean isAvailable() {
        return remaining > 0;
    }

    /** Takes one unit and returns how many items it gives. */
    @Override
    public int harvest() {
        if (remaining <= 0) {
            return 0;
        }
        remaining--;
        return type.getYield();
    }

    @Override
    public double getX() {
        return col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    @Override
    public double getY() {
        return row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    @Override
    public ResourceType getType() {
        return type;
    }

    public int getRemaining() {
        return remaining;
    }

    // ------------------------------------------------------------------
    // Drawing (simple shapes until Jaron's art arrives)
    // ------------------------------------------------------------------

    public void draw(Graphics2D g, int frame) {
        int x = (int) getX();
        int y = (int) getY();
        boolean empty = remaining <= 0;

        if (sprite != null) {
            java.awt.Composite old = g.getComposite();
            if (empty) {
                g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.35f));
            }
            if (type == ResourceType.TREE) {
                g.drawImage(sprite, x - TREE_W / 2, y + 6 - TREE_H, null);   // the trunk's foot is on the tile
            } else {
                g.drawImage(sprite, x - 8, y - 8, null);
            }
            g.setComposite(old);
        } else if (type == ResourceType.TREE) {
            drawTree(g, x, y, empty);
        } else if (type == ResourceType.BAMBOO) {
            drawBamboo(g, x, y, empty);
        } else {
            drawOre(g, x, y, empty);
        }

        // A little bar under the spot shows how much is left.
        int w = 14;
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(x - w / 2, y + 9, w, 3);
        g.setColor(empty ? new Color(255, 255, 255, 60) : Color.WHITE);
        g.fillRect(x - w / 2, y + 9, w * remaining / maxUnits, 3);
    }

    /** A round tree: trunk and two blobs of leaves. Chopped trees leave a stump. It grows back slowly. */
    private void drawTree(Graphics2D g, int x, int y, boolean empty) {
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x - 7, y + 2, 14, 6);                       // shadow
        g.setColor(new Color(112, 78, 44));
        g.fillRect(x - 1, y - (empty ? 1 : 6), 3, empty ? 7 : 12);   // trunk (a stump when empty)
        g.setColor(new Color(60, 40, 22));
        g.drawRect(x - 1, y - (empty ? 1 : 6), 2, empty ? 6 : 11);
        if (empty) {
            return;
        }
        int leaves = Math.max(1, Math.min(3, (remaining * 3 + maxUnits - 1) / maxUnits));   // fewer leaves as it is chopped
        g.setColor(new Color(46, 110, 52));
        g.fillOval(x - 9, y - 18, 18, 14);
        g.setColor(new Color(72, 150, 70));
        g.fillOval(x - 7, y - 22, 15, 13);
        if (leaves >= 2) {
            g.setColor(new Color(110, 190, 90));
            g.fillOval(x - 4, y - 21, 7, 5);
        }
        if (leaves >= 3) {
            g.setColor(new Color(236, 120, 70));                // a few fruits
            g.fillRect(x - 5, y - 12, 2, 2);
            g.fillRect(x + 3, y - 14, 2, 2);
        }
    }

    private void drawBamboo(Graphics2D g, int x, int y, boolean empty) {
        // The grove shows fewer stalks as it is cut, and they grow back.
        int stalks = empty ? 0 : Math.max(1, Math.min(3, (remaining * 3 + maxUnits - 1) / maxUnits));
        g.setColor(new Color(96, 168, 70));
        for (int i = 0; i < 3; i++) {
            int h = i < stalks ? 12 - Math.abs(i - 1) * 2 : 3;
            g.setColor(i < stalks ? new Color(96, 168, 70) : new Color(130, 130, 90));
            g.fillRect(x + (i - 1) * 4 - 1, y + 6 - h, 3, h);
            g.setColor(new Color(50, 100, 40));
            g.drawRect(x + (i - 1) * 4 - 1, y + 6 - h, 2, h);
        }
    }

    private void drawOre(Graphics2D g, int x, int y, boolean empty) {
        Color base = type.getColor();
        Color fill = empty ? new Color(100, 100, 106) : base;
        int[] px = {x - 7, x - 3, x + 4, x + 8, x + 5, x - 5};
        int[] py = {y + 5, y - 5, y - 7, y, y + 6, y + 7};
        g.setColor(fill);
        g.fillPolygon(px, py, 6);
        g.setColor(base.darker().darker());
        g.drawPolygon(px, py, 6);
        if (!empty) {
            g.setColor(base.brighter().brighter());   // glints
            g.fillRect(x - 2, y - 3, 2, 2);
            g.fillRect(x + 2, y + 1, 2, 2);
        }
    }

    // ------------------------------------------------------------------
    // Loading from data/nodes.txt
    // ------------------------------------------------------------------

    /** Reads the file. Spots on the wrong kind of tile, or of the wrong kind of resource, are skipped with a warning. */
    public static ArrayList<ResourceNode> load(String path, TileMap map) {
        ArrayList<ResourceNode> nodes = new ArrayList<>();
        InputStream in = ResourceNode.class.getResourceAsStream(path);
        if (in == null) {
            System.out.println("ResourceNode: file not found (" + path + "), using default spots.");
            addDefaults(nodes);
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
            addDefaults(nodes);
        }
        return nodes;
    }

    private static void addFromLine(ArrayList<ResourceNode> nodes, String line, TileMap map) {
        String[] parts = line.split("\\s+");
        try {
            ResourceType type = ResourceType.valueOf(parts[0].toUpperCase());
            int col = Integer.parseInt(parts[1]);
            int row = Integer.parseInt(parts[2]);
            int units = parts.length > 3 ? Integer.parseInt(parts[3]) : DEFAULT_UNITS;
            if (type.getCategory() == ResourceType.Category.FISH) {
                System.out.println("ResourceNode: fish swim in the sea by themselves, remove " + type + " from the file.");
            } else if (!map.isLand(row, col)) {
                System.out.println("ResourceNode: " + type + " must be on land, tile " + col + "," + row + " skipped.");
            } else if (units < 1) {
                System.out.println("ResourceNode: units must be at least 1: " + line);
            } else {
                nodes.add(new ResourceNode(type, col, row, units));
            }
        } catch (RuntimeException e) {
            System.out.println("ResourceNode: skipped a bad line: " + line);
        }
    }

    private static void addDefaults(ArrayList<ResourceNode> nodes) {
        nodes.add(new ResourceNode(ResourceType.BAMBOO, 23, 14, 6));
        nodes.add(new ResourceNode(ResourceType.NICKEL, 46, 14, 5));
        nodes.add(new ResourceNode(ResourceType.COPPER, 34, 24, 4));
    }
}
