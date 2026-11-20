package atingdagat.entities;

import atingdagat.core.Constants;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Migo.java
 *
 * The radio nerd. He keeps the signal climbing, and a strong signal brings supply crates
 * faster (the world asks every survivor boostsCrates(), and Migo says yes).
 */
public class Migo extends Survivor {

    private int timer = 0;

    public Migo(double startX, double startY, double homeX, double homeY) {
        super("Migo", "Radio tech", startX, startY, homeX, homeY);
    }

    @Override
    public void act(Post post, Radio radio, List<Warship> ships, int frame) {
        if (!isHome() || radio.isSilent()) {
            return;
        }
        timer++;
        if (timer >= Constants.MIGO_SIGNAL_FRAMES) {
            timer = 0;
            radio.restore(1);
        }
    }

    @Override
    public boolean boostsCrates() {
        return true;
    }

    @Override
    protected Color getShirt() {
        return new Color(80, 130, 205);   // blue polo
    }

    @Override
    protected void drawExtra(Graphics2D g, int px, int py, int frame) {
        g.setColor(new Color(30, 30, 38));              // headset
        g.fillRect(px - 4, py - 19, 1, 4);
        g.fillRect(px + 3, py - 19, 1, 4);
        g.fillRect(px - 3, py - 21, 6, 1);
        if ((frame / 20) % 2 == 0) {                    // a blinking antenna light on his bag
            g.setColor(new Color(120, 255, 150));
            g.fillRect(px + 4, py - 13, 1, 1);
        }
    }

    @Override
    public String getArrivalTitle() {
        return "A bangka lands: Migo, the radio nerd";
    }

    @Override
    public String getArrivalLine() {
        return "\"I heard your signal from three islands away. Let me boost it.\"";
    }
}
