package atingdagat.ui;

import atingdagat.core.Constants;
import atingdagat.core.DayCycle;
import atingdagat.core.GameWorld;
import atingdagat.core.Phase;
import atingdagat.defense.Mine;
import atingdagat.defense.Net;
import atingdagat.economy.ResourceType;
import atingdagat.entities.Post;
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
        drawBar(g, 330, "POST", world.getPost().getHealth(), Post.MAX_HEALTH, new Color(120, 220, 120), new Color(235, 90, 80));
        drawBar(g, 470, "RADIO", world.getRadio().getSignal(), Radio.MAX_SIGNAL, new Color(110, 190, 245), new Color(235, 90, 80));

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

    private void drawResources(Graphics2D g, GameWorld world) {
        int x = 110;
        for (ResourceType type : ResourceType.values()) {
            BufferedImage icon = Assets.get(type.getIconPath(), 10, 10);
            if (icon != null) {
                g.drawImage(icon, x, 6, null);
            } else {
                g.setColor(type.getColor());
                g.fillOval(x, 6, 10, 10);
                g.setColor(new Color(0, 0, 0, 120));
                g.drawOval(x, 6, 10, 10);
            }
            g.setFont(BOLD);
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(world.getInventory().get(type)), x + 14, 15);
            x += 58;
        }
    }

    private void drawBar(Graphics2D g, int x, String label, int value, int max, Color good, Color low) {
        int w = 110;
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
            hint = "MINE: click water near the island (" + Mine.COST_ORE + " ore). It blows up ships that come close.  Right-click or 2 = stop.";
        } else {
            hint = "Click fish, bamboo, ore to harvest.  1 = net  2 = mine  R = repair (" + Constants.REPAIR_FISH_COST + " fish)  N = skip";
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
