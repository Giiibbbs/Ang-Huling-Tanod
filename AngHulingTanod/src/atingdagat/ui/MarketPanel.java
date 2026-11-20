package atingdagat.ui;

import atingdagat.core.Constants;
import atingdagat.core.GameWorld;
import atingdagat.defense.Mine;
import atingdagat.defense.Net;
import atingdagat.economy.ResourceType;
import atingdagat.economy.Upgrades;
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
        PANA("4", "Pana tower", ResourceType.BAMBOO, Constants.PANA_COST_BAMBOO, "A bamboo arrow tower. Shoots ships even in the dark. Place on free land."),
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
    private static final int TABBAR = 12;
    private static final int ROW = 24;
    private static final int TIP_HEIGHT = 36;
    private static final int CREW_HEIGHT = 16;

    private static final Font TINY = new Font("SansSerif", Font.PLAIN, 8);
    private static final Font NAME = new Font("SansSerif", Font.BOLD, 10);
    private static final Font KEY = new Font("SansSerif", Font.BOLD, 9);

    private boolean open = true;
    private boolean upgradesTab = false;   // false: BUILD tab, true: UPGRADES tab

    /** Key U (or a click on a tab): switch between BUILD and UPGRADES. */
    public void switchTab() {
        upgradesTab = !upgradesTab;
    }

    private int rowCount() {
        return upgradesTab ? Upgrades.Gear.values().length : Offer.values().length;
    }

    /** Top of the first row. */
    private int rowsTop() {
        return Y + HEADER + TABBAR;
    }

    public void toggle() {
        open = !open;
    }

    /** The panel (or just its small tab) is only there in the day. */
    private boolean visible(GameWorld world) {
        return !world.getCycle().isNight();
    }

    private int height() {
        return open ? HEADER + TABBAR + rowCount() * ROW + CREW_HEIGHT + TIP_HEIGHT : HEADER;
    }

    private boolean inside(int x, int y) {
        return x >= X && x <= X + W && y >= Y && y <= Y + height();
    }

    private int rowAt(int x, int y) {
        if (!open || x < X || x > X + W) {
            return -1;
        }
        int index = (y - rowsTop()) / ROW;
        if (y < rowsTop() || index < 0 || index >= rowCount()) {
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
        if (y < rowsTop()) {   // the tab bar
            upgradesTab = x > X + W / 2;
            return true;
        }
        int index = rowAt(x, y);
        if (index >= 0) {
            if (upgradesTab) {
                world.buyUpgrade(Upgrades.Gear.values()[index]);
            } else {
                use(world, Offer.values()[index]);
            }
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
            case PANA:
                world.toggleTool(GameWorld.Tool.PANA);
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
            case PANA:
                return world.getPanaCount() >= Constants.PANA_MAX;
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
            case PANA:
                return world.getPanaCount() + "/" + Constants.PANA_MAX + " built";
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
            case PANA:
                return world.getTool() == GameWorld.Tool.PANA;
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

        drawTabs(g);
        int hover = rowAt(mouseX, mouseY);
        String tip;
        if (upgradesTab) {
            Upgrades.Gear[] gear = Upgrades.Gear.values();
            for (int i = 0; i < gear.length; i++) {
                drawGearRow(g, world, gear[i], rowsTop() + i * ROW, i == hover);
            }
            tip = hover >= 0 ? gear[hover].getDescription() : "Click a tool (or press 5-8) to buy its next level. Bamboo, and a little ore from level 2.";
        } else {
            Offer[] offers = Offer.values();
            for (int i = 0; i < offers.length; i++) {
                drawRow(g, world, offers[i], rowsTop() + i * ROW, i == hover);
            }
            tip = hover >= 0 ? offers[hover].description : "Click a row (or press its key). Then click the map to place it.";
        }
        drawCrew(g, world);
        drawTip(g, tip);
    }

    private void drawTabs(Graphics2D g) {
        String[] names = {"BUILD", "UPGRADES  (U)"};
        for (int i = 0; i < 2; i++) {
            boolean active = (i == 1) == upgradesTab;
            int tx = X + i * (W / 2);
            g.setColor(active ? new Color(70, 62, 24, 235) : new Color(30, 38, 62, 215));
            g.fillRect(tx, Y + HEADER, W / 2, TABBAR);
            g.setFont(TINY);
            g.setColor(active ? new Color(255, 225, 140) : new Color(150, 165, 190));
            g.drawString(names[i], tx + 6, Y + HEADER + 9);
        }
    }

    /** One tool in the Upgrades tab: its level, and what the next level costs. */
    private void drawGearRow(Graphics2D g, GameWorld world, Upgrades.Gear gear, int y, boolean hover) {
        Upgrades up = world.getUpgrades();
        boolean maxed = up.isMaxed(gear);
        int bamboo = world.getInventory().get(ResourceType.BAMBOO);
        int ore = world.getInventory().get(gear.getOre());
        boolean bambooOk = bamboo >= up.bambooCost(gear);
        boolean oreOk = ore >= up.oreCost(gear);

        g.setColor(hover ? new Color(44, 58, 92, 225) : new Color(20, 28, 48, 205));
        g.fillRect(X, y, W, ROW);

        g.setColor(new Color(235, 235, 245));
        g.fillRoundRect(X + 4, y + 4, 12, 12, 4, 4);
        g.setFont(KEY);
        g.setColor(new Color(30, 30, 50));
        g.drawString(gear.getKey(), X + 4 + (12 - g.getFontMetrics().stringWidth(gear.getKey())) / 2, y + 13);

        g.setFont(NAME);
        g.setColor(maxed ? new Color(150, 160, 170) : Color.WHITE);
        g.drawString(gear.getLabel(), X + 21, y + 12);

        // Three pips show the level
        for (int i = 0; i < Upgrades.MAX_LEVEL; i++) {
            g.setColor(i < up.getLevel(gear) ? new Color(255, 215, 90) : new Color(255, 255, 255, 50));
            g.fillRect(X + W - 5 - (Upgrades.MAX_LEVEL - i) * 8, y + 5, 6, 6);
        }
        if (maxed) {
            g.setFont(TINY);
            g.setColor(new Color(255, 215, 90));
            g.drawString("MAX", X + 21 + 62, y + 21);
            return;
        }
        // Cost of the next level: bamboo, then ore if the level needs it
        g.setFont(TINY);
        String b = "Bamboo " + bamboo + "/" + up.bambooCost(gear);
        g.setColor(bambooOk ? new Color(170, 240, 180) : new Color(255, 170, 140));
        g.drawString(b, X + 21, y + 21);
        if (up.oreCost(gear) > 0) {
            int ox = X + 21 + g.getFontMetrics().stringWidth(b) + 6;
            g.setColor(oreOk ? new Color(170, 240, 180) : new Color(255, 170, 140));
            g.drawString("+ " + gear.getOre().getLabel() + " " + ore + "/" + up.oreCost(gear), ox, y + 21);
        }
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

    /** A strip showing which survivors have arrived: a lit chip with the name, or a dark "?" chip. */
    private void drawCrew(Graphics2D g, GameWorld world) {
        int y = rowsTop() + rowCount() * ROW;
        g.setColor(new Color(20, 28, 48, 205));
        g.fillRect(X, y, W, CREW_HEIGHT);
        String[] names = {"Ka Inday", "Migo", "Tonyo"};
        int chipW = (W - 8) / 3;
        g.setFont(TINY);
        for (int i = 0; i < names.length; i++) {
            boolean here = false;
            boolean paid = false;
            for (atingdagat.entities.Survivor sv : world.getSurvivors()) {
                if (sv.getName().equals(names[i])) {
                    here = true;
                    paid = sv.isPaid();
                }
            }
            int cx = X + 4 + i * chipW;
            g.setColor(here ? new Color(70, 110, 70, 230) : new Color(40, 46, 64, 230));
            g.fillRoundRect(cx, y + 3, chipW - 2, 11, 4, 4);
            g.setColor(here ? new Color(220, 250, 220) : new Color(130, 140, 160));
            String t = here ? names[i] : "? night " + (2 * (i + 1));
            g.drawString(t, cx + 3, y + 11);
            if (here) {   // green dot: paid and working today. red: no meals, resting.
                g.setColor(paid ? new Color(120, 255, 130) : new Color(255, 110, 90));
                g.fillOval(cx + chipW - 9, y + 6, 4, 4);
            }
        }
    }

    /** The help box at the bottom of the panel (shows the row under the mouse). */
    private void drawTip(Graphics2D g, String text) {
        int y = rowsTop() + rowCount() * ROW + CREW_HEIGHT;
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
