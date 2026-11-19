package atingdagat.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Popup.java
 *
 * A small floating text like "+1 Fish" that rises and fades. It tells the
 * player "that worked" without a sound (sounds come later).
 */
public class Popup {

    private static final int LIFE = 55;
    private static final Font FONT = new Font("SansSerif", Font.BOLD, 10);

    private final double x;
    private double y;
    private final String text;
    private final Color color;
    private int age = 0;

    public Popup(double x, double y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
    }

    public void update() {
        age++;
        y -= 0.4;
    }

    public boolean isDone() {
        return age >= LIFE;
    }

    public void draw(Graphics2D g) {
        int alpha = (int) (255 * (1.0 - age / (double) LIFE));
        g.setFont(FONT);
        int w = g.getFontMetrics().stringWidth(text);
        g.setColor(new Color(0, 0, 0, alpha / 2));
        g.drawString(text, (int) x - w / 2 + 1, (int) y + 1);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.drawString(text, (int) x - w / 2, (int) y);
    }
}
