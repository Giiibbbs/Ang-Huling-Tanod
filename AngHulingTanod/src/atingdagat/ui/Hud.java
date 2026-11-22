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
        drawEvent(g, world);
        drawBanner(g, world);
    }

    // The day dial: a little window on the sky. The sun crosses it by day, sinks at dusk, the moon
    // shows at night, and the sun rises at dawn. The ring around it fills as the phase passes.
    private static final int DIAL_X = 25;
    private static final int DIAL_Y = 31;
    private static final int DIAL_R = 19;

    private void drawClock(Graphics2D g, DayCycle cycle, GameWorld world) {
        Phase phase = cycle.getPhase();
        double p = cycle.progress();
        double dark = cycle.getDarkness();
        int cx = DIAL_X;
        int cy = DIAL_Y;
        int r = DIAL_R;

        java.awt.Shape oldClip = g.getClip();
        g.setClip(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));

        // Sky: blue by day, navy by night, with an orange glow at dusk and dawn
        Color sky = mix(new Color(112, 188, 238), new Color(14, 20, 50), dark);
        if (phase == Phase.DUSK || phase == Phase.DAWN) {
            sky = mix(sky, new Color(244, 140, 84), Math.sin(p * Math.PI) * 0.7);
        }
        g.setColor(sky);
        g.fillRect(cx - r, cy - r, r * 2, r * 2);

        // Stars come out as it gets dark
        if (dark > 0.3) {
            g.setColor(new Color(255, 255, 255, (int) (200 * dark)));
            g.fillRect(cx - 11, cy - 12, 1, 1);
            g.fillRect(cx + 9, cy - 8, 1, 1);
            g.fillRect(cx - 5, cy - 4, 1, 1);
            g.fillRect(cx + 4, cy - 14, 1, 1);
        }

        // The sun or the moon
        if (phase == Phase.NIGHT) {
            drawMoon(g, cx + 1, cy - 8, sky);
        } else {
            double sx;
            double sy;
            if (phase == Phase.DAY) {            // left to right over the top
                double angle = Math.PI * (1 - p);
                sx = cx + 13 * Math.cos(angle);
                sy = cy - 13 * Math.sin(angle);
            } else if (phase == Phase.DUSK) {    // sinks on the right
                sx = cx + 13;
                sy = cy + p * 12;
            } else {                             // dawn: rises on the left
                sx = cx - 13;
                sy = cy + (1 - p) * 12;
            }
            g.setColor(new Color(255, 245, 160, 90));
            g.fillOval((int) sx - 6, (int) sy - 6, 12, 12);
            g.setColor(new Color(255, 214, 70));
            g.fillOval((int) sx - 4, (int) sy - 4, 8, 8);
        }

        // Ground (hides the sun below the horizon)
        g.setColor(mix(new Color(52, 120, 76), new Color(14, 30, 30), dark));
        g.fillRect(cx - r, cy + 1, r * 2, r);
        g.setColor(new Color(0, 0, 0, 80));
        g.drawLine(cx - r, cy + 1, cx + r, cy + 1);
        g.setClip(oldClip);

        // The ring: how much of this phase has passed (at night: how many ships are still coming)
        double fraction;
        Color ringColor;
        if (phase == Phase.NIGHT) {
            int total = Math.max(1, world.getShipsTotalTonight());
            fraction = world.getShipsLeftTonight() / (double) total;
            ringColor = new Color(150, 180, 255);
        } else {
            fraction = p;
            ringColor = phase == Phase.DAY ? new Color(255, 218, 100) : new Color(255, 150, 90);
        }
        g.setStroke(new java.awt.BasicStroke(3f));
        g.setColor(new Color(0, 0, 0, 150));
        g.drawOval(cx - r, cy - r, r * 2, r * 2);
        g.setColor(ringColor);
        g.drawArc(cx - r, cy - r, r * 2, r * 2, 90, (int) (-360 * Math.min(1.0, fraction)));
        g.setStroke(new java.awt.BasicStroke(1f));

        // Words next to the dial
        g.setFont(BOLD);
        g.setColor(cycle.isNight() ? new Color(160, 190, 255) : new Color(255, 235, 160));
        g.drawString(phase.getLabel() + " " + cycle.getNight() + "/" + Constants.TOTAL_NIGHTS, 50, 12);
        g.setFont(SMALL);
        g.setColor(Color.WHITE);
        if (phase == Phase.NIGHT) {
            g.drawString("Ships left: " + world.getShipsLeftTonight(), 50, 22);
        } else {
            int secs = cycle.getSecondsLeft();
            String until = phase == Phase.DAY ? "dusk" : phase == Phase.DUSK ? "night" : "day";
            g.drawString(secs / 60 + ":" + (secs % 60 < 10 ? "0" : "") + secs % 60 + " to " + until, 50, 22);
        }
    }

    private void drawMoon(Graphics2D g, int x, int y, Color sky) {
        g.setColor(new Color(235, 238, 255));
        g.fillOval(x - 5, y - 5, 10, 10);
        g.setColor(sky);                       // a second circle bites a crescent out of the first
        g.fillOval(x - 2, y - 7, 9, 9);
    }

    private Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** Bamboo, fish (counted in meals), and the four ores. Each has an icon, or a colored dot if there is no art yet. */
    private void drawResources(Graphics2D g, GameWorld world) {
        ResourceType[] shown = {ResourceType.BAMBOO, ResourceType.TAMBAN, ResourceType.NICKEL,
            ResourceType.COPPER, ResourceType.CHROMITE, ResourceType.GOLD};
        int x = 122;
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
            hint = "Click a LIT ship to fire.  SPACE at the lighthouse steers the beam.  F flare.  X mends.";
        } else if (world.getTool() == GameWorld.Tool.NET) {
            hint = "NET: click shallow water (" + Net.COST_BAMBOO + " bamboo).  Right-click or 1 = stop.";
        } else if (world.getTool() == GameWorld.Tool.MINE) {
            hint = "MINE: click water near the island (" + Mine.COST_NICKEL + " nickel). It blows up ships that come close.  Right-click or 2 = stop.";
        } else if (world.getTool() == GameWorld.Tool.TORCH) {
            hint = "TORCH: click free land (" + Constants.TORCH_COST_BAMBOO + " bamboo). It lights the shore all night.  Right-click or 3 = stop.";
        } else {
            hint = "WASD walk.  HOLD the mouse on bamboo, ore or fish.  Click the WORKSHOP (left) to build and upgrade.  B hides it.";
        }
        g.setFont(SMALL);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, Constants.INTERNAL_HEIGHT - 14, Constants.INTERNAL_WIDTH, 14);
        g.setColor(Color.WHITE);
        g.drawString(hint, 8, Constants.INTERNAL_HEIGHT - 4);
        drawGadgets(g, world);
        g.drawString("P pause", Constants.INTERNAL_WIDTH - 44, Constants.INTERNAL_HEIGHT - 4);
    }

    /** The flare cooldown (night) and the "hold X to mend" prompt when a damaged defense is in reach. */
    private void drawGadgets(Graphics2D g, GameWorld world) {
        g.setFont(SMALL);
        int y = Constants.INTERNAL_HEIGHT - 20;
        if (world.getCycle().isNight()) {
            int s = world.getFlareSecondsLeft();
            g.setColor(s == 0 ? new Color(255, 232, 140) : new Color(190, 190, 200));
            g.drawString(s == 0 ? "F: flare ready" : "F: flare in " + s + "s", Constants.INTERNAL_WIDTH - 92, y);
        }
        atingdagat.defense.Durable near = world.findRepairTarget();
        if (near != null) {
            g.setColor(new Color(150, 240, 150));
            g.drawString("Hold X: mend (" + near.repairCostPerPoint() + " bamboo)", 8, y);
        }
    }

    /** The story line and the gong meter of the Bakunawa event. */
    private void drawEvent(Graphics2D g, GameWorld world) {
        atingdagat.entities.Bakunawa dragon = world.getDragon();
        String caption = dragon.getCaption();
        if (caption.isEmpty()) {
            drawStory(g, world);
            return;
        }
        int cx = Constants.INTERNAL_WIDTH / 2;
        int y = 292;
        g.setFont(BOLD);
        FontMetrics fm = g.getFontMetrics();
        int w = Math.max(fm.stringWidth(caption) + 24, 250);
        g.setColor(new Color(10, 4, 8, 200));
        g.fillRoundRect(cx - w / 2, y - 14, w, 42, 8, 8);
        g.setColor(new Color(255, 215, 120));
        g.drawString(caption, cx - fm.stringWidth(caption) / 2, y);

        if (dragon.getStage() == atingdagat.entities.Bakunawa.Stage.GONGS) {   // the noise meter
            int bw = 200;
            int bx = cx - bw / 2;
            double fill = dragon.getNoise() / 100.0;
            g.setColor(new Color(255, 255, 255, 50));
            g.fillRect(bx, y + 8, bw, 10);
            g.setColor(fill > 0.66 ? new Color(255, 215, 90) : fill > 0.33 ? new Color(255, 160, 70) : new Color(230, 90, 70));
            g.fillRect(bx, y + 8, (int) (bw * fill), 10);
            g.setColor(new Color(0, 0, 0, 170));
            g.drawRect(bx, y + 8, bw, 10);
            g.setFont(SMALL);
            g.setColor(Color.WHITE);
            g.drawString("NOISE", bx - 34, y + 17);
        }
    }

    /** The short story box when a bangka lands: who they are, and what they say. */
    private void drawStory(Graphics2D g, GameWorld world) {
        String title = world.getStoryTitle();
        if (title.isEmpty()) {
            return;
        }
        String text = world.getStoryText();
        double fade = world.getStoryFade();
        int cx = Constants.INTERNAL_WIDTH / 2;
        int y = 296;
        g.setFont(BOLD);
        FontMetrics fm = g.getFontMetrics();
        int w = Math.max(fm.stringWidth(title), fm.stringWidth(text)) + 28;
        g.setColor(new Color(10, 14, 28, (int) (205 * fade)));
        g.fillRoundRect(cx - w / 2, y - 14, w, 36, 8, 8);
        g.setColor(new Color(255, 215, 120, (int) (255 * fade)));
        g.drawString(title, cx - fm.stringWidth(title) / 2, y);
        g.setColor(new Color(235, 240, 250, (int) (255 * fade)));
        g.drawString(text, cx - fm.stringWidth(text) / 2, y + 15);
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
