package atingdagat.core;

import atingdagat.map.TileMap;
import atingdagat.ui.EndingScene;
import atingdagat.ui.Hud;
import atingdagat.ui.MarketPanel;
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
 * The run itself (clock, ships, nets, resources...) lives in GameWorld. This
 * class creates a new GameWorld when a game starts and forwards input to it.
 */
public class GamePanel extends JPanel {

    // The game draws here first (the "internal screen").
    private final BufferedImage screen =
            new BufferedImage(Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);

    private final Timer timer;
    private GameState state = GameState.MENU;

    // The map is loaded once. A GameWorld (one run of the game) is created when you press Enter.
    private final TileMap map = new TileMap(Constants.MAP_FILE);
    private GameWorld world;               // null until the first game starts
    private final Hud hud = new Hud();
    private final MarketPanel market = new MarketPanel();
    private EndingScene ending;   // the story shown after surviving night 7

    // Where the mouse is, in game (640x360) coordinates.
    private int mouseX = Constants.INTERNAL_WIDTH / 2;
    private int mouseY = 40;

    // Where the internal screen is placed inside the window (set every frame).
    private int drawX, drawY, drawW, drawH;

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

        // Mouse: moving it aims the beam. We convert window coordinates to game coordinates.
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = toGameX(e.getX());
                mouseY = toGameY(e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = toGameX(e.getX());
                mouseY = toGameY(e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (world != null) {
                    world.release();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                mouseX = toGameX(e.getX());
                mouseY = toGameY(e.getY());
                if (state == GameState.PLAY && world != null) {
                    if (e.getButton() == MouseEvent.BUTTON1 && market.click(world, mouseX, mouseY)) {
                        return;   // the click was on the workshop panel, not on the map
                    }
                    world.click(mouseX, mouseY, e.getButton());
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

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
                    newGame();
                }
                break;
            case PLAY:
                if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
                    state = GameState.PAUSE;
                } else if (code == KeyEvent.VK_N) {
                    world.skipPhase();
                } else if (code == KeyEvent.VK_L) {
                    world.jumpToLastNight();   // test key
                } else if (code == KeyEvent.VK_E) {
                    world.testEclipse();       // test key
                } else if (code == KeyEvent.VK_K) {
                    world.testCrate();         // test key
                } else if (code == KeyEvent.VK_SPACE) {
                    world.gong();
                } else if (code == KeyEvent.VK_1) {
                    world.toggleTool(GameWorld.Tool.NET);
                } else if (code == KeyEvent.VK_2) {
                    world.toggleTool(GameWorld.Tool.MINE);
                } else if (code == KeyEvent.VK_3) {
                    world.toggleTool(GameWorld.Tool.TORCH);
                } else if (code == KeyEvent.VK_4) {
                    world.toggleTool(GameWorld.Tool.PANA);
                } else if (code == KeyEvent.VK_J) {
                    world.testSurvivors();     // test key
                } else if (code == KeyEvent.VK_B) {
                    market.toggle();
                } else if (code == KeyEvent.VK_R) {
                    world.repair();
                } else if (code == KeyEvent.VK_C) {
                    world.rewire();
                } else if (code == KeyEvent.VK_V) {
                    world.armorPost();
                } else if (code == KeyEvent.VK_G) {
                    world.upgradeLamp();
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
                if (code == KeyEvent.VK_ENTER) {
                    state = GameState.MENU;
                }
                break;
            case VICTORY:
                if (code == KeyEvent.VK_ENTER) {
                    if (ending != null && !ending.isFinished()) {
                        ending.skip();   // jump to the title card
                    } else {
                        state = GameState.MENU;
                    }
                }
                break;
        }
    }

    // ------------------------------------------------------------------
    // UPDATE (game logic, once per frame)
    // ------------------------------------------------------------------

    private void update() {
        frameCount++;
        if (state == GameState.VICTORY && ending != null) {
            ending.update(world);
        }
        if (state != GameState.PLAY) {
            return; // nothing moves in the menu or while paused
        }

        // The walking keys: WASD or the arrow keys.
        int moveX = (keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT] ? 1 : 0)
                - (keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT] ? 1 : 0);
        int moveY = (keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN] ? 1 : 0)
                - (keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP] ? 1 : 0);
        world.setInput(moveX, moveY, keys[KeyEvent.VK_SPACE]);
        world.update(mouseX, mouseY);

        // Did the run end?
        if (world.getOutcome() == GameWorld.Outcome.LOST) {
            state = GameState.GAME_OVER;
        } else if (world.getOutcome() == GameWorld.Outcome.WON) {
            ending = new EndingScene(map, world.getPost().getX(), world.getPost().getY(), world.getSurvivors());
            state = GameState.VICTORY;
        }
    }

    /** Starts a fresh run from night 1. */
    private void newGame() {
        world = new GameWorld(map);
        state = GameState.PLAY;
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
        if (state == GameState.MENU || world == null) {
            map.draw(g, frameCount);
            dim(g, 140);
            drawMenu(g);
            return;
        }

        // Every other screen shows the world underneath.
        world.draw(g, frameCount);
        if (state == GameState.VICTORY && ending != null) {
            ending.draw(g);   // the ending story has no HUD
            return;
        }
        hud.draw(g, world);
        market.draw(g, world, mouseX, mouseY);

        switch (state) {
            case PAUSE:
                dim(g, 120);
                drawCenteredText(g, "PAUSED", 150, 32, Color.WHITE);
                drawCenteredText(g, "P - resume     M - main menu", 185, 14, Color.LIGHT_GRAY);
                break;
            case GAME_OVER:
                dim(g, 170);
                drawCenteredText(g, "GAME OVER", 150, 32, Color.RED);
                drawCenteredText(g, world.getEndReason(), 178, 13, Color.WHITE);
                drawCenteredText(g, "You reached night " + world.getCycle().getNight() + " of " + Constants.TOTAL_NIGHTS, 198, 12, Color.LIGHT_GRAY);
                drawCenteredText(g, "Press ENTER", 226, 14, Color.LIGHT_GRAY);
                break;
            case VICTORY:
                dim(g, 150);
                drawCenteredText(g, "MAY NATITIRA PA TAYO", 150, 28, Color.YELLOW);
                drawCenteredText(g, world.getEndReason(), 178, 13, Color.WHITE);
                drawCenteredText(g, "Press ENTER", 216, 14, Color.LIGHT_GRAY);
                break;
            default:
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
        drawCenteredText(g, "May natitira pa tayo.", 224, 11, new Color(255, 235, 160));
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
        if (drawW == 0) {
            return 0; // nothing has been drawn yet
        }
        return (int) ((windowX - drawX) * (double) Constants.INTERNAL_WIDTH / drawW);
    }

    public int toGameY(int windowY) {
        if (drawH == 0) {
            return 0;
        }
        return (int) ((windowY - drawY) * (double) Constants.INTERNAL_HEIGHT / drawH);
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState newState) {
        this.state = newState;
    }
}
