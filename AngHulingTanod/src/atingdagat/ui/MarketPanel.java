package atingdagat.ui;

import atingdagat.core.Constants;
import atingdagat.core.GameWorld;
import atingdagat.defense.Mine;
import atingdagat.defense.Net;
import atingdagat.economy.ResourceType;
import atingdagat.entities.Radio;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * MarketPanel.java
 *
 * The WORKSHOP: a menu on the left of the screen, like a shop. Every thing you can build or
 * upgrade is one row. A row shows its key, its name, what it costs, how much of that you
 * already have (a progress bar that fills up), and its level. You can CLICK a row, or
 * use its key. Press B to hide or show the panel. It is only shown in the daytime.
 *
 * It only reads the world, and tells the world what to do when a row is clicked.
 * (Same idea as Hud: drawing and clicking live here, the rules live in GameWorld.)
 */
public class MarketPanel {

    /** Everything on sale. An enum keeps each row's fixed facts in one place. */
    private enum Offer {
        NET("1", "Net", ResourceType.BAMBOO, Net.COST_BAMBOO, "Ships stop and cut it. Place on shallow water."),
        MINE("2", "Mine", ResourceType.NICKEL, Mine.COST_NICKEL, "Blows up ships that come near. Place on water close to land."),
        TORCH("3", "Torch", ResourceType.BAMBOO, Constants.TORCH_COST_BAMBOO, "Lights the shore all night. Place on free land."),
        REPAIR("R", "Repair", null, Constants.REPAIR_FISH_COST, "Eat fish to mend the post and boost the radio."),
        REWIRE("C", "Rewire", ResourceType.COPPER, Constants.REWIRE_COST_COPPER, "Rewire the radio for a stronger signal."),
        ARMOR("V", "Armor", ResourceType.CHROMITE, Constants.ARMOR_COST_CHROMITE, "Armor the post: more maximum health."),
        LAMP("G", "Lamp", ResourceType.GOLD, Constants.LAMP_COST_GOLD, "Upgrade the lamp: a wider and longer beam.");

        private final String key;
        private final String label;
        private final ResourceType resource;   // null means "meals of fish"
        private final int cost;
        private final String description;

        Offer(String key, String label, ResourceType resource, int cost, String description) {
            this.key = key;
            this.label = label;
            this.resource = resource;
            this.cost = cost;
            this.description = description;
        }
    }

    private static final int X = 4;
    private static final int Y = 56;
    private static final int W = 140;
    private static final int HEADER = 14;
    private static final int ROW = 24;
    private static final int TIP_HEIGHT = 36;

    private static final Font TINY = new Font("SansSerif", Font.PLAIN, 8);
    private static final Font NAME = new Font("SansSerif", Font.BOLD, 10);
    private static final Font KEY = new Font("SansSerif", Font.BOLD, 9);

    private boolean open = true;

    public void toggle() {
        open = !open;
    }

    /** The panel (or just its small tab) is only there in the day. */
    private boolean visible(GameWorld world) {
        return !world.getCycle().isNight();
    }

    private int height() {
        return open ? HEADER + Offer.values().length * ROW + TIP_HEIGHT : HEADER;
    }

    private boolean inside(int x, int y) {
        return x >= X && x <= X + W && y >= Y && y <= Y + height();
    }

    private int rowAt(int x, int y) {
        if (!open || x < X || x > X + W) {
            return -1;
        }
        int index = (y - Y - HEADER) / ROW;
        if (y < Y + HEADER || index < 0 || index >= Offer.values().length) {
            return -1;
        }
        return index;
    }

    // ------------------------------------------------------------------
    // CLICKING
    // ------------------------------------------------------------------

    /** Returns true if the click was on the panel (so the world must NOT also treat it as a click on the map). */
    public boolean click(GameWorld world, int x, int y) {
        if (!visible(world) || !inside(x, y)) {
            return false;
        }
        if (y < Y + HEADER) {
            toggle();   // the header is a button too
            return true;
        }
        int index = rowAt(x, y);
        if (index >= 0) {
            use(world, Offer.values()[index]);
        }
        return true;
    }

    private void use(GameWorld world, Offer offer) {
        switch (offer) {
            case NET:
                world.toggleTool(GameWorld.Tool.NET);
                break;
            case MINE:
                world.toggleTool(GameWorld.Tool.MINE);
                break;
            case TORCH:
                world.toggleTool(GameWorld.Tool.TORCH);
                break;
            case REPAIR:
                world.repair();
                break;
            case REWIRE:
                world.rewire();
                break;
            case ARMOR:
                world.armorPost();
                break;
            default:
                world.upgradeLamp();
                break;
        }
    }

    // ------------------------------------------------------------------
    // WHAT EACH ROW SHOWS
    // ------------------------------------------------------------------

    private int have(GameWorld world, Offer offer) {
        return offer.resource == null ? world.getInventory().getMeals() : world.getInventory().get(offer.resource);
    }

    private String unitName(Offer offer) {
        return offer.resource == null ? "meals" : offer.resource.getLabel().toLowerCase();
    }

    /** Fully built, fully upgraded, or nothing left to fix. */
    private boolean maxed(GameWorld world, Offer offer) {
        switch (offer) {
            case TORCH:
                return world.getTorchCount() >= Constants.TORCH_MAX;
            case REPAIR:
                return world.getPost().getHealth() >= world.getPost().getMaxHealth()
                        && world.getRadio().getSignal() >= Radio.MAX_SIGNAL;
            case REWIRE:
                return world.getRadio().getSignal() >= Radio.MAX_SIGNAL;
            case ARMOR:
                return world.getPost().getArmorLevel() >= Constants.ARMOR_MAX_LEVEL;
            case LAMP:
                return world.getLampLevel() >= Constants.LAMP_MAX_LEVEL;
            default:
                return false;
        }
    }

    /** The small note on the right of the name: how many you have built, a level, or a health number. */
    private String status(GameWorld world, Offer offer) {
        switch (offer) {
            case TORCH:
                return world.getTorchCount() + "/" + Constants.TORCH_MAX + " built";
            case REPAIR:
                return "Post " + world.getPost().getHealth() + "/" + world.getPost().getMaxHealth();
            case REWIRE:
                return "Radio " + world.getRadio().getSignal();
            case ARMOR:
                return "Lv " + world.getPost().getArmorLevel() + "/" + Constants.ARMOR_MAX_LEVEL;
            case LAMP:
                return "Lv " + world.getLampLevel() + "/" + Constants.LAMP_MAX_LEVEL;
            default:
                return "";
        }
    }

    private boolean selected(GameWorld world, Offer offer) {
        switch (offer) {
            case NET:
                return world.getTool() == GameWorld.Tool.NET;
            case MINE:
                return world.getTool() == GameWorld.Tool.MINE;
            case TORCH:
                return world.getTool() == GameWorld.Tool.TORCH;
            default:
                return false;
        }
    }

    // ------------------------------------------------------------------
    // DRAWING
    // ------------------------------------------------------------------

    public void draw(Graphics2D g, GameWorld world, int mouseX, int mouseY) {
        if (!visible(world)) {
            return;
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Header (also the hide/show button)
        g.setColor(new Color(20, 28, 48, 215));
        g.fillRoundRect(X, Y, W, HEADER, 6, 6);
        g.setFont(KEY);
        g.setColor(new Color(255, 225, 140));
        g.drawString("WORKSHOP", X + 6, Y + 10);
        g.setFont(TINY);
        g.setColor(new Color(200, 210, 230));
        String hide = open ? "B hide" : "B show";
        g.drawString(hide, X + W - g.getFontMetrics().stringWidth(hide) - 6, Y + 10);
        if (!open) {
            return;
        }

        int hover = rowAt(mouseX, mouseY);
        Offer[] offers = Offer.values();
        for (int i = 0; i < offers.length; i++) {
            drawRow(g, world, offers[i], Y + HEADER + i * ROW, i == hover);
        }
        drawTip(g, hover >= 0 ? offers[hover].description : "Click a row (or press its key). Then click the map to place it.");
    }

    private void drawRow(Graphics2D g, GameWorld world, Offer offer, int y, boolean hover) {
        int have = have(world, offer);
        boolean maxed = maxed(world, offer);
        boolean affordable = have >= offer.cost;
        boolean on = selected(world, offer);

        g.setColor(on ? new Color(70, 62, 24, 225) : hover ? new Color(44, 58, 92, 225) : new Color(20, 28, 48, 205));
        g.fillRect(X, y, W, ROW);
        if (on) {
            g.setColor(new Color(255, 215, 90));
            g.drawRect(X, y, W - 1, ROW - 1);
        }

        // Key chip
        g.setColor(new Color(235, 235, 245));
        g.fillRoundRect(X + 4, y + 4, 12, 12, 4, 4);
        g.setFont(KEY);
        g.setColor(new Color(30, 30, 50));
        g.drawString(offer.key, X + 4 + (12 - g.getFontMetrics().stringWidth(offer.key)) / 2, y + 13);

        // Name and status
        g.setFont(NAME);
        g.setColor(maxed ? new Color(150, 160, 170) : Color.WHITE);
        g.drawString(offer.label, X + 21, y + 12);
        String status = status(world, offer);
        if (!status.isEmpty()) {
            g.setFont(TINY);
            g.setColor(maxed ? new Color(255, 215, 90) : new Color(190, 205, 225));
            g.drawString(maxed ? "MAX" : status, X + W - g.getFontMetrics().stringWidth(maxed ? "MAX" : status) - 5, y + 11);
        }

        // Requirement: a bar that fills as you collect, and the numbers
        int barX = X + 21;
        int barY = y + 16;
        int barW = 56;
        double ratio = Math.min(1.0, have / (double) offer.cost);
        g.setColor(new Color(255, 255, 255, 45));
        g.fillRect(barX, barY, barW, 4);
        g.setColor(maxed ? new Color(120, 125, 135) : affordable ? new Color(110, 225, 125) : new Color(240, 165, 80));
        g.fillRect(barX, barY, (int) (barW * ratio), 4);
        g.setColor(new Color(0, 0, 0, 150));
        g.drawRect(barX, barY, barW, 4);

        g.setFont(TINY);
        g.setColor(affordable ? new Color(170, 240, 180) : new Color(255, 170, 140));
        g.drawString(have + "/" + offer.cost + " " + unitName(offer), barX + barW + 5, y + 21);
    }

    /** The help box at the bottom of the panel (shows the row under the mouse). */
    private void drawTip(Graphics2D g, String text) {
        int y = Y + HEADER + Offer.values().length * ROW;
        g.setColor(new Color(12, 16, 30, 225));
        g.fillRoundRect(X, y, W, TIP_HEIGHT, 6, 6);
        g.setFont(TINY);
        g.setColor(new Color(215, 225, 240));
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        String line = "";
        int ty = y + 11;
        for (String word : words) {
            String tryLine = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(tryLine) > W - 12 && !line.isEmpty()) {
                g.drawString(line, X + 6, ty);
                ty += 10;
                line = word;
            } else {
                line = tryLine;
            }
        }
        g.drawString(line, X + 6, ty);
    }
}
