package atingdagat.core;

import atingdagat.map.TileMap;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * GamePanel.java
 *
 * The heart of the game. It does three things:
 *  1. Runs the game loop using a javax.swing.Timer (update, then repaint).
 *  2. Draws everything onto a small 640x360 image, then scales that image to
 *     fit the window (so resizing the window works).
 *  3. Keeps track of the current GameState (menu, play, pause, ...).
 *
 * Right now it only draws placeholder shapes so you can see the loop works.
 * Later, other classes (TileMap, Beam, Warship...) will be called from
 * update() and draw().
 */
public class GamePanel extends JPanel {

    // The game draws here first (the "internal screen").
    private final BufferedImage screen =
            new BufferedImage(Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);

    private final Timer timer;
    private GameState state = GameState.MENU;

    // The island and the sea.
    private final TileMap map = new TileMap(Constants.MAP_FILE);

    // Where the internal screen is placed inside the window (set every frame).
    private int drawX, drawY, drawW, drawH;

    // Placeholder: a square that moves so we can see the loop running.
    private double boxX = 100;
    private double boxSpeed = 2;

    // Keys currently held down (arrays are covered in class).
    private final boolean[] keys = new boolean[256];

    private int frameCount = 0;

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.BLACK);

        // Keyboard
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keys.length) {
                    keys[code] = true;
                }
                handleKeyPress(code);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code < keys.length) {
                    keys[code] = false;
                }
            }
        });

        // Mouse: example of converting window coordinates to game coordinates.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int gx = toGameX(e.getX());
                int gy = toGameY(e.getY());
                System.out.println("Clicked game position: " + gx + ", " + gy);
            }
        });

        // The game loop: runs about 60 times per second.
        int delay = 1000 / Constants.TARGET_FPS;
        timer = new Timer(delay, e -> {
            update();
            repaint();
        });
    }

    public void start() {
        requestFocusInWindow();
        timer.start();
    }

    // ------------------------------------------------------------------
    // INPUT
    // ------------------------------------------------------------------

    private void handleKeyPress(int code) {
        switch (state) {
            case MENU:
                if (code == KeyEvent.VK_ENTER) {
                    state = GameState.PLAY;
                }
                break;
            case PLAY:
                if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
                    state = GameState.PAUSE;
                }
                break;
            case PAUSE:
                if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
                    state = GameState.PLAY;
                } else if (code == KeyEvent.VK_M) {
                    state = GameState.MENU;
                }
                break;
            case GAME_OVER:
            case VICTORY:
                if (code == KeyEvent.VK_ENTER) {
                    state = GameState.MENU;
                }
                break;
        }
    }

    // ------------------------------------------------------------------
    // UPDATE (game logic, once per frame)
    // ------------------------------------------------------------------

    private void update() {
        frameCount++;
        if (state != GameState.PLAY) {
            return; // nothing moves in the menu or while paused
        }

        // Placeholder movement: bounce a square left and right.
        boxX += boxSpeed;
        if (boxX < 20 || boxX > Constants.INTERNAL_WIDTH - 40) {
            boxSpeed = -boxSpeed;
        }
    }

    // ------------------------------------------------------------------
    // DRAW
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // clears the panel to black

        // 1. Draw the game onto the small internal screen.
        Graphics2D g2 = screen.createGraphics();
        drawGame(g2);
        g2.dispose();

        // 2. Work out the biggest size that fits the window with the same shape.
        double scale = Math.min((double) getWidth() / Constants.INTERNAL_WIDTH,
                                (double) getHeight() / Constants.INTERNAL_HEIGHT);
        drawW = (int) (Constants.INTERNAL_WIDTH * scale);
        drawH = (int) (Constants.INTERNAL_HEIGHT * scale);
        drawX = (getWidth() - drawW) / 2;   // black bars on the sides if needed
        drawY = (getHeight() - drawH) / 2;

        // 3. Draw the internal screen scaled up.
        Graphics2D out = (Graphics2D) g;
        Object hint = Constants.SMOOTH_SCALING
                ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
                : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        out.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        out.drawImage(screen, drawX, drawY, drawW, drawH, null);
    }

    /** Draws one frame in internal 640x360 coordinates. */
    private void drawGame(Graphics2D g) {
        // The island and sea are drawn first, behind everything else.
        map.draw(g, frameCount);

        switch (state) {
            case MENU:
                dim(g, 140);
                drawMenu(g);
                break;
            case PLAY:
                drawPlay(g);
                break;
            case PAUSE:
                drawPlay(g);
                dim(g, 120);
                drawCenteredText(g, "PAUSED", 150, 32, Color.WHITE);
                drawCenteredText(g, "P - resume     M - main menu", 185, 14, Color.LIGHT_GRAY);
                break;
            case GAME_OVER:
                dim(g, 170);
                drawCenteredText(g, "GAME OVER", 160, 32, Color.RED);
                drawCenteredText(g, "Press ENTER", 195, 14, Color.LIGHT_GRAY);
                break;
            case VICTORY:
                dim(g, 150);
                drawCenteredText(g, "MAY NATITIRA PA TAYO", 160, 28, Color.YELLOW);
                drawCenteredText(g, "Press ENTER", 195, 14, Color.LIGHT_GRAY);
                break;
        }
    }

    /** Darkens the whole screen so text on top is easy to read. */
    private void dim(Graphics2D g, int alpha) {
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT);
    }

    private void drawMenu(Graphics2D g) {
        drawCenteredText(g, Constants.TITLE, 130, 24, Color.WHITE);
        drawCenteredText(g, "Press ENTER to start", 200, 14, Color.LIGHT_GRAY);
    }

    private void drawPlay(Graphics2D g) {
        // Placeholder for a ship: a moving box (the skiff will replace it).
        g.setColor(Color.ORANGE);
        g.fillRect((int) boxX, 60, 20, 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Night 1 / " + Constants.TOTAL_NIGHTS + "   (P = pause)   frame " + frameCount, 8, 14);
    }

    private void drawCenteredText(Graphics2D g, String text, int y, int size, Color color) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int x = (Constants.INTERNAL_WIDTH - fm.stringWidth(text)) / 2;
        g.setColor(color);
        g.drawString(text, x, y);
    }

    // ------------------------------------------------------------------
    // MOUSE HELPERS: convert window pixels to internal-screen pixels
    // ------------------------------------------------------------------

    public int toGameX(int windowX) {
        return (int) ((windowX - drawX) * (double) Constants.INTERNAL_WIDTH / drawW);
    }

    public int toGameY(int windowY) {
        return (int) ((windowY - drawY) * (double) Constants.INTERNAL_HEIGHT / drawH);
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState newState) {
        this.state = newState;
    }
}
