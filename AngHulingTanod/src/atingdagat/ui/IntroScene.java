package atingdagat.ui;

import atingdagat.core.Constants;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * IntroScene.java
 *
 * The comic that plays when the game starts, before the main menu. Six pictures tell the story:
 * the Philippines is falling, Isla Dulo is untouched, the Kordans find it, the last bangka leaves,
 * the old spirits still watch, and the title card.
 *
 * Each panel is a PNG in res/intro (panel1.png to panel6.png) with NO text in it: this class draws the
 * captions, so a typo is a one-line fix. If a picture is missing, a dark blue background is drawn instead and
 * the caption still shows, so the scene never crashes.
 *
 * Controls: ENTER or SPACE goes to the next panel, ESC skips the whole comic. A panel also moves on by itself
 * after a few seconds.
 */
public class IntroScene {

    private static final int PANEL_FRAMES = 420;   // 7 seconds per panel if nobody presses anything
    private static final int FADE_FRAMES = 40;     // each panel fades in
    private static final double ZOOM = 0.06;       // the slow zoom over a panel (6%)

    private static final Font CAPTION = new Font("SansSerif", Font.BOLD, 12);
    private static final Font LOCAL = new Font("SansSerif", Font.ITALIC, 11);
    private static final Font TITLE = new Font("SansSerif", Font.BOLD, 34);
    private static final Font HINT = new Font("SansSerif", Font.PLAIN, 9);

    // The English caption and the Filipino line for each panel.
    private static final String[] ENGLISH = {
        "The Kordan Dominion has swept across the Philippines. One by one, the islands have fallen.",
        "All but one. Isla Dulo, at the edge of the map, still has its bamboo, its ore, its fish.",
        "Then a lone scout saw the lighthouse light, and the Kordans knew they had found it.",
        "The people were sent away on the last bangka. One tanod stayed behind.",
        "But Isla Dulo is not alone. The old spirits still watch: the manananggal, the Bakunawa, and the stone of Lapu-Lapu.",
        "Seven nights until the relief fleet arrives. Hold the light."
    };
    private static final String[] FILIPINO = {
        "Isa-isang bumagsak ang mga isla.",
        "Ang huling isla na may natitirang yaman.",
        "Natagpuan na nila ang isla.",
        "May isang tanod na nanatili.",
        "Hindi nag-iisa ang isla. Binabantayan ito ng mga sinaunang kuwento.",
        "Pitong gabi. Ipaglaban ang liwanag."
    };

    private final BufferedImage[] panels = new BufferedImage[ENGLISH.length];   // null where the file is missing
    private int index = 0;
    private int timer = 0;
    private boolean finished = false;

    public IntroScene() {
        for (int i = 0; i < panels.length; i++) {
            panels[i] = load("/atingdagat/res/intro/panel" + (i + 1) + ".png");
        }
    }

    /** Reads one picture from inside the project (so it also works from the JAR). Null if it is not there. */
    private BufferedImage load(String path) {
        try (InputStream in = IntroScene.class.getResourceAsStream(path)) {
            if (in == null) {
                System.out.println("Intro panel not found (using placeholder): " + path);
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            System.out.println("Could not read " + path + ": " + e.getMessage());
            return null;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    /** Called once per frame. */
    public void update() {
        if (finished) {
            return;
        }
        timer++;
        if (timer >= PANEL_FRAMES) {
            next();
        }
    }

    /** Go to the next panel; after the last one the comic is finished. */
    public void next() {
        index++;
        timer = 0;
        if (index >= panels.length) {
            finished = true;
        }
    }

    /** Skip the whole comic. */
    public void skip() {
        finished = true;
    }

    public void draw(Graphics2D g) {
        if (finished || index >= panels.length) {
            return;
        }
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // 1. The picture, slowly zooming in, fading in at the start of each panel.
        double fade = Math.min(1.0, timer / (double) FADE_FRAMES);
        java.awt.Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) fade));
        BufferedImage pic = panels[index];
        if (pic == null) {
            g.setColor(new Color(20, 30, 55));
            g.fillRect(0, 0, Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT);
        } else {
            double zoom = 1.0 + ZOOM * timer / PANEL_FRAMES;
            int w = pic.getWidth();
            int h = pic.getHeight();
            int cropW = (int) (w / zoom);
            int cropH = (int) (h / zoom);
            int sx = (w - cropW) / 2;
            int sy = (h - cropH) / 2;
            g.drawImage(pic, 0, 0, Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT, sx, sy, sx + cropW, sy + cropH, null);
        }
        g.setComposite(old);

        // 2. The title on the last panel.
        if (index == panels.length - 1) {
            g.setFont(TITLE);
            drawCentered(g, "ANG HULING TANOD", 52, new Color(255, 225, 140, (int) (255 * fade)));
        }

        // 3. The caption on a dark band at the bottom.
        g.setColor(new Color(0, 0, 0, (int) (150 * fade)));
        g.fillRect(0, 292, Constants.INTERNAL_WIDTH, 68);
        g.setFont(CAPTION);
        int y = 309;
        ArrayList<String> lines = wrap(g, ENGLISH[index], 600);
        for (String line : lines) {
            drawCentered(g, line, y, new Color(245, 245, 235, (int) (255 * fade)));
            y += 15;
        }
        g.setFont(LOCAL);
        drawCentered(g, FILIPINO[index], y + 4, new Color(255, 215, 120, (int) (255 * fade)));

        // 4. A small hint, and which panel we are on.
        g.setFont(HINT);
        g.setColor(new Color(200, 200, 200, 150));
        String hint = (index + 1) + "/" + panels.length + "     ENTER next     ESC skip";
        g.drawString(hint, Constants.INTERNAL_WIDTH - g.getFontMetrics().stringWidth(hint) - 6, 354);
    }

    private void drawCentered(Graphics2D g, String text, int y, Color color) {
        FontMetrics fm = g.getFontMetrics();
        int x = (Constants.INTERNAL_WIDTH - fm.stringWidth(text)) / 2;
        g.setColor(new Color(0, 0, 0, Math.min(255, color.getAlpha())));
        g.drawString(text, x + 1, y + 1);   // a soft shadow so the text reads on any picture
        g.setColor(color);
        g.drawString(text, x, y);
    }

    /** Splits a sentence into lines that fit in maxWidth pixels. */
    private ArrayList<String> wrap(Graphics2D g, String text, int maxWidth) {
        ArrayList<String> lines = new ArrayList<>();
        FontMetrics fm = g.getFontMetrics();
        String current = "";
        for (String word : text.split(" ")) {
            String attempt = current.isEmpty() ? word : current + " " + word;
            if (fm.stringWidth(attempt) > maxWidth && !current.isEmpty()) {
                lines.add(current);
                current = word;
            } else {
                current = attempt;
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        return lines;
    }
}
