package atingdagat.defense;

import atingdagat.core.Constants;
import atingdagat.entities.Warship;
import atingdagat.util.Assets;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * LapuLapuStone.java
 *
 * A stone statue of Lapu-Lapu on the northeast islet. In our story the chieftain who stopped
 * the invaders was turned to stone, not killed. He does nothing yet: he stands there and the
 * story tells you he is only sleeping. It is a Defense only so the world already knows how to
 * update and draw it (ships cannot reach the islet, so it never takes damage).
 *
 * If the art file exists (res/monsters/lapulapu_stone.png, drawn at 28x48) the game draws it, otherwise
 * it draws a grey statue with a spear in code.
 */
public class LapuLapuStone implements Defense {

    private static final String SPRITE_PATH = "/atingdagat/res/monsters/lapulapu_stone.png";
    private final BufferedImage sprite = Assets.get(SPRITE_PATH, 28, 48);

    private final int col;
    private final int row;

    public LapuLapuStone(int col, int row) {
        this.col = col;
        this.row = row;
    }

    public double getX() {
        return col * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    public double getY() {
        return row * Constants.TILE_SIZE + Constants.TILE_SIZE / 2.0;
    }

    @Override
    public void update(List<Warship> ships) {
        // A statue does nothing. Yet.
    }

    @Override
    public boolean occupies(int c, int r) {
        return c == col && r == row;
    }

    @Override
    public boolean blocksShips() {
        return false;
    }

    @Override
    public void damage(int amount) {
        // Stone. Nothing hurts it.
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

    @Override
    public void draw(Graphics2D g, int frame) {
        int x = (int) getX();
        int y = (int) getY() + 5;
        g.setColor(new Color(0, 0, 0, 45));
        g.fillOval(x - 14, y - 10, 28, 9);
        if (sprite != null) {
            g.drawImage(sprite, x - 14, y - 52, null);
            return;
        }
        Color stone = new Color(150, 152, 158);
        Color dark = new Color(100, 102, 110);
        g.setColor(dark);
        g.fillRect(x - 9, y - 6, 18, 6);                  // base
        g.setColor(stone);
        g.fillRect(x - 5, y - 26, 10, 20);                // body
        g.fillOval(x - 5, y - 35, 10, 10);                // head
        g.setColor(dark);
        g.fillRect(x - 5, y - 20, 10, 2);                 // sash
        g.setColor(new Color(120, 90, 60));
        g.drawLine(x + 8, y - 40, x + 8, y - 6);          // spear shaft
        g.setColor(stone);
        g.fillRect(x + 7, y - 43, 3, 4);                  // spear tip
        g.setColor(new Color(60, 110, 60, 140));          // moss
        g.fillRect(x - 5, y - 10, 4, 3);
        g.fillRect(x + 1, y - 28, 3, 2);
    }
}
