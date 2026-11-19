package atingdagat.ui;

import atingdagat.core.Constants;
import atingdagat.core.DayCycle;
import atingdagat.core.GameWorld;
import atingdagat.core.Phase;
import atingdagat.defense.Mine;
import atingdagat.defense.Net;
import atingdagat.economy.ResourceType;
import atingdagat.entities.Radio;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import atingdagat.util.Assets;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Hud.java
 *
 * The information layer on top of the game: which night and phase it is, what
 * you carry, the post and radio bars, and a hint line for the current action.
 * It only reads from GameWorld. It never changes the game.
 */
public class Hud {

    private static final Font SMALL = new Font("SansSerif", Font.PLAIN, 9);
    private static final Font BOLD = new Font("SansSerif", Font.BOLD, 11);
    private static final Font BANNER = new Font("SansSerif", Font.BOLD, 30);

    public void draw(Graphics2D g, GameWorld world) {
        DayCycle cycle = world.getCycle();

        // Top strip
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, Constants.INTERNAL_WIDTH, 24);

        drawClock(g, cycle, world);
        drawResources(g, world);
        drawBar(g, 392, "POST", world.getPost().getHealth(), world.getPost().getMaxHealth(), new Color(120, 220, 120), new Color(235, 90, 80));
        drawBar(g, 530, "RADIO", world.getRadio().getSignal(), Radio.MAX_SIGNAL, new Color(110, 190, 245), new Color(235, 90, 80));

        drawHint(g, world);
        drawBanner(g, world);
    }

    private void drawClock(Graphics2D g, DayCycle cycle, GameWorld world) {
        g.setFont(BOLD);
        g.setColor(cycle.isNight() ? new Color(160, 190, 255) : new Color(255, 235, 160));
        g.drawString(cycle.getPhase().getLabel() + " " + cycle.getNight() + "/" + Constants.TOTAL_NIGHTS, 8, 12);

        // Under the name: a thin bar of how much of the phase has passed, or ships left at night.
        if (cycle.getPhase() == Phase.NIGHT) {
            g.setFont(SMALL);
            g.setColor(Color.WHITE);
            g.drawString("Ships left: " + world.getShipsLeftTonight(), 8, 21);
        } else {
            g.setColor(new Color(255, 255, 255, 60));
            g.fillRect(8, 16, 70, 4);
            g.setColor(new Color(255, 225, 130));
            g.fillRect(8, 16, (int) (70 * cycle.progress()), 4);
        }
    }

    /** Bamboo, fish (counted in meals), and the four ores. Each has an icon, or a colored dot if there is no art yet. */
    private void drawResources(Graphics2D g, GameWorld world) {
        ResourceType[] shown = {ResourceType.BAMBOO, ResourceType.TAMBAN, ResourceType.NICKEL,
            ResourceType.COPPER, ResourceType.CHROMITE, ResourceType.GOLD};
        int x = 104;
        for (ResourceType type : shown) {
            boolean isFish = type.getCategory() == ResourceType.Category.FISH;
            String iconPath = isFish ? "/atingdagat/res/ui/fish.png" : type.getIconPath();
            BufferedImage icon = Assets.get(iconPath, 10, 10);
            if (icon != null) {
                g.drawImage(icon, x, 7, null);
            } else {
                g.setColor(isFish ? new Color(200, 215, 225) : type.getColor());
                g.fillOval(x, 7, 10, 10);
                g.setColor(new Color(0, 0, 0, 120));
                g.drawOval(x, 7, 10, 10);
            }
            int count = isFish ? world.getInventory().getMeals() : world.getInventory().get(type);
            g.setFont(BOLD);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(count), x + 13, 16);
            x += 46;
        }
    }

    private void drawBar(Graphics2D g, int x, String label, int value, int max, Color good, Color low) {
        int w = 90;
        double ratio = value / (double) max;
        g.setFont(SMALL);
        g.setColor(Color.WHITE);
        g.drawString(label + " " + value + "/" + max, x, 10);
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRect(x, 14, w, 6);
        g.setColor(ratio > 0.34 ? good : low);
        g.fillRect(x, 14, (int) (w * ratio), 6);
        g.setColor(new Color(0, 0, 0, 140));
        g.drawRect(x, 14, w, 6);
    }

    private void drawHint(Graphics2D g, GameWorld world) {
        String hint;
        if (world.getCycle().isNight()) {
            hint = "Move the mouse to aim the beam.  Click a LIT ship to fire.";
        } else if (world.getTool() == GameWorld.Tool.NET) {
            hint = "NET: click shallow water (" + Net.COST_BAMBOO + " bamboo).  Right-click or 1 = stop.";
        } else if (world.getTool() == GameWorld.Tool.MINE) {
            hint = "MINE: click water near the island (" + Mine.COST_NICKEL + " nickel). It blows up ships that come close.  Right-click or 2 = stop.";
        } else {
            hint = "HOLD the mouse on bamboo, ore or fish.  1 net  2 mine  R repair  C rewire  V armor  G lamp";
        }
        g.setFont(SMALL);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, Constants.INTERNAL_HEIGHT - 14, Constants.INTERNAL_WIDTH, 14);
        g.setColor(Color.WHITE);
        g.drawString(hint, 8, Constants.INTERNAL_HEIGHT - 4);
        g.drawString("P pause", Constants.INTERNAL_WIDTH - 44, Constants.INTERNAL_HEIGHT - 4);
    }

    private void drawBanner(Graphics2D g, GameWorld world) {
        double fade = world.getBannerFade();
        if (fade <= 0 || world.getBannerText().isEmpty()) {
            return;
        }
        g.setFont(BANNER);
        FontMetrics fm = g.getFontMetrics();
        int x = (Constants.INTERNAL_WIDTH - fm.stringWidth(world.getBannerText())) / 2;
        g.setColor(new Color(0, 0, 0, (int) (140 * fade)));
        g.drawString(world.getBannerText(), x + 2, 82);
        g.setColor(new Color(255, 245, 200, (int) (255 * fade)));
        g.drawString(world.getBannerText(), x, 80);
    }
}
