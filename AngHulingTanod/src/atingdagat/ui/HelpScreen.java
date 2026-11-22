package atingdagat.ui;

import atingdagat.core.Constants;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * HelpScreen.java
 *
 * The one-page "How to play" screen. Open it from the menu (H or F1) or during the game (F1).
 * It only draws text, so there is no state: the text lives in the arrays below, easy to edit.
 */
public class HelpScreen {

    private static final Font HEAD = new Font("SansSerif", Font.BOLD, 12);
    private static final Font BODY = new Font("SansSerif", Font.PLAIN, 10);

    private static final String[] DAY = {
        "Walk close and HOLD left click on",
        "bamboo, trees, ore and fish.",
        "Spend it in the Workshop (left):",
        "1 Net  2 Mine  3 Torch  4 Pana",
        "R repairs the post. Fish are meals.",
        "Survivors who join work for meals.",
    };

    private static final String[] NIGHT = {
        "Warships sail in from the sea.",
        "Stand at the lighthouse and hold",
        "SPACE to aim the beam with the mouse.",
        "Left click shoots a LIT ship.",
        "Nets slow ships, mines blow them up.",
        "F fires a flare. Raiders shoot your towers:",
        "hold X next to them to mend (bamboo).",
    };

    private static final String[][] KEYS = {
        {"W A S D", "walk"},
        {"Mouse", "aim and point"},
        {"Hold click", "harvest (day)"},
        {"Click", "shoot (night)"},
        {"Right click", "put the tool away"},
        {"U", "Workshop tabs"},
        {"5 to 9", "upgrade your tools"},
        {"F", "flare (night)"},
        {"Hold X", "mend nets and towers"},
        {"P / F1", "pause / this page"},
    };

    public void draw(Graphics2D g) {
        int w = Constants.INTERNAL_WIDTH;
        int h = Constants.INTERNAL_HEIGHT;
        g.setColor(new Color(6, 10, 22, 225));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 215, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        centered(g, "HOW TO PLAY", 42);

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(235, 240, 250));
        centered(g, "Survive 7 nights. Keep the post standing and the radio alive.", 68);

        column(g, 22, 100, "DAY: gather and build", DAY);
        column(g, 226, 100, "NIGHT: defend", NIGHT);
        keyColumn(g, 430, 100);

        g.setColor(new Color(255, 215, 120, 60));
        g.fillRect(22, 260, w - 44, 1);
        g.setColor(new Color(235, 240, 250));
        g.setFont(BODY);
        centered(g, "Every night has a twist, and each night brings more ships at once.", 278);
        centered(g, "Watch the post health and the radio signal. Respect the old stories (tabi-tabi po).", 294);
        g.setColor(new Color(255, 232, 140));
        g.setFont(HEAD);
        centered(g, "Press ENTER or F1 to go back", 328);
    }

    private void column(Graphics2D g, int x, int y, String title, String[] lines) {
        g.setFont(HEAD);
        g.setColor(new Color(255, 215, 120));
        g.drawString(title, x, y);
        g.setFont(BODY);
        g.setColor(new Color(225, 230, 240));
        for (int i = 0; i < lines.length; i++) {
            g.drawString(lines[i], x, y + 20 + i * 16);
        }
    }

    /** The key list: the key on the left in gold, what it does on the right. */
    private void keyColumn(Graphics2D g, int x, int y) {
        g.setFont(HEAD);
        g.setColor(new Color(255, 215, 120));
        g.drawString("KEYS", x, y);
        g.setFont(BODY);
        for (int i = 0; i < KEYS.length; i++) {
            g.setColor(new Color(255, 232, 140));
            g.drawString(KEYS[i][0], x, y + 20 + i * 16);
            g.setColor(new Color(225, 230, 240));
            g.drawString(KEYS[i][1], x + 70, y + 20 + i * 16);
        }
    }

    private void centered(Graphics2D g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, (Constants.INTERNAL_WIDTH - fm.stringWidth(text)) / 2, y);
    }
}
