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
    private GameState helpReturn = GameState.MENU;   // where the help page goes back to
    private final atingdagat.ui.HelpScreen help = new atingdagat.ui.HelpScreen();

    // The map is loaded once. A GameWorld (one run of the game) is created when you press Enter.
    private final TileMap map = new TileMap(Constants.MAP_FILE);
    private GameWorld world;               // null until the first game starts
    private final Hud hud = new Hud();
    private final MarketPanel market = new MarketPanel();
    private EndingScene ending;   // the story shown after surviving night 7

    // High scores: the table (read from a file), this run's result, and the name being typed.
    private final HighScores scores = new HighScores(Constants.SCORE_FILE);
    private ScoreEntry pendingScore;
    private final StringBuilder nameBuffer = new StringBuilder();

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
            public void keyTyped(KeyEvent e) {
                // Typing a name for the high-score table: letters, digits, space, - and _
                if (state == GameState.NAME) {
                    char c = e.getKeyChar();
                    boolean ok = Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '_';
                    if (ok && nameBuffer.length() < Constants.NAME_MAX) {
                        nameBuffer.append(c);
                    }
                }
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

    /** Shows the how-to-play page and remembers which screen to go back to. */
    private void openHelp() {
        helpReturn = state;
        state = GameState.HELP;
    }

    private void handleKeyPress(int code) {
        if (code == KeyEvent.VK_F12 || code == KeyEvent.VK_BACK_QUOTE) {
            Debug.toggle();   // the developer keys are off unless you press F12 (or ` on laptops where F12 needs Fn)
            return;
        }
        switch (state) {
            case MENU:
                if (code == KeyEvent.VK_ENTER) {
                    newGame();
                } else if (code == KeyEvent.VK_H || code == KeyEvent.VK_F1) {
                    openHelp();
                }
                break;
            case HELP:
                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_F1 || code == KeyEvent.VK_H) {
                    state = helpReturn;
                }
                break;
            case PLAY:
                if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
                    state = GameState.PAUSE;
                } else if (code == KeyEvent.VK_F1) {
                    openHelp();
                } else if (Debug.isKeysOn() && code == KeyEvent.VK_N) {
                    world.skipPhase();
                } else if (Debug.isKeysOn() && code == KeyEvent.VK_L) {
                    world.jumpToLastNight();   // test key
                } else if (Debug.isKeysOn() && code == KeyEvent.VK_E) {
                    world.testEclipse();       // test key
                } else if (Debug.isKeysOn() && code == KeyEvent.VK_K) {
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
                } else if (code == KeyEvent.VK_5) {
                    world.buyUpgrade(atingdagat.economy.Upgrades.Gear.PICKAXE);
                } else if (code == KeyEvent.VK_6) {
                    world.buyUpgrade(atingdagat.economy.Upgrades.Gear.AXE);
                } else if (code == KeyEvent.VK_7) {
                    world.buyUpgrade(atingdagat.economy.Upgrades.Gear.BOOTS);
                } else if (code == KeyEvent.VK_8) {
                    world.buyUpgrade(atingdagat.economy.Upgrades.Gear.ROD);
                } else if (code == KeyEvent.VK_9) {
                    world.buyUpgrade(atingdagat.economy.Upgrades.Gear.RIFLE);
                } else if (Debug.isKeysOn() && code == KeyEvent.VK_H) {
                    world.testMonsters();      // test key
                } else if (Debug.isKeysOn() && code == KeyEvent.VK_J) {
                    world.testSurvivors();     // test key
                } else if (code == KeyEvent.VK_F) {
                    world.fireFlare();
                } else if (code == KeyEvent.VK_U) {
                    market.switchTab();
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
                } else if (code == KeyEvent.VK_F1) {
                    openHelp();
                } else if (code == KeyEvent.VK_Q && world.getCycle().isEndless()) {
                    finishRun();   // retire from endless mode: the nights survived so far are saved
                }
                break;
            case NAME:
                if (code == KeyEvent.VK_ENTER) {
                    String name = nameBuffer.toString().trim();
                    scores.add(new ScoreEntry(name.isEmpty() ? "Tanod" : name, pendingScore.getNights(), pendingScore.getPostHealth()));
                    state = GameState.MENU;
                } else if (code == KeyEvent.VK_ESCAPE) {
                    state = GameState.MENU;   // skip: nothing is saved
                } else if (code == KeyEvent.VK_BACK_SPACE && nameBuffer.length() > 0) {
                    nameBuffer.deleteCharAt(nameBuffer.length() - 1);
                }
                break;
            case GAME_OVER:
                if (code == KeyEvent.VK_ENTER) {
                    finishRun();
                }
                break;
            case VICTORY:
                if (code == KeyEvent.VK_E && ending != null && ending.isFinished()) {
                    world.startEndless();   // keep fighting: night 8 and beyond
                    ending = null;
                    state = GameState.PLAY;
                } else if (code == KeyEvent.VK_ENTER) {
                    if (ending != null && !ending.isFinished()) {
                        ending.skip();   // jump to the title card
                    } else {
                        finishRun();
                    }
                }
                break;
        }
    }

    /** The run is over: if it made the table, ask for a name; otherwise go back to the menu. */
    private void finishRun() {
        pendingScore = new ScoreEntry("", world.getNightsSurvived(), world.getPost().getHealth());
        if (scores.qualifies(pendingScore)) {
            nameBuffer.setLength(0);
            state = GameState.NAME;
        } else {
            state = GameState.MENU;
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
        world.setRepairHeld(keys[KeyEvent.VK_X]);
        world.update(mouseX, mouseY);

        // Did the run end?
        if (world.getOutcome() == GameWorld.Outcome.LOST) {
            state = GameState.GAME_OVER;
        } else if (world.getOutcome() == GameWorld.Outcome.WON) {
            ending = new EndingScene(map, world.getPost().getX(), world.getPost().getY(), world.getSurvivors(), world.getVoices());
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
        if (state == GameState.HELP) {   // the page over whatever was on screen before
            if (helpReturn == GameState.MENU || world == null) {
                map.draw(g, frameCount);
            } else {
                world.draw(g, frameCount);
            }
            help.draw(g);
            return;
        }
        if (state == GameState.MENU || world == null) {
            map.draw(g, frameCount);
            dim(g, 140);
            drawMenu(g);
            drawDebugNote(g);
            return;
        }

        // Every other screen shows the world underneath.
        double[] shake = world.getShakeOffset();   // a small shake when something explodes
        g.translate(shake[0], shake[1]);
        world.draw(g, frameCount);
        g.translate(-shake[0], -shake[1]);
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
                drawCenteredText(g, "P - resume     F1 - how to play     M - main menu", 185, 14, Color.LIGHT_GRAY);
                if (world.getCycle().isEndless()) {
                    drawCenteredText(g, "Q - end this run and save the score", 210, 12, new Color(255, 215, 120));
                }
                break;
            case GAME_OVER:
                dim(g, 170);
                drawCenteredText(g, "GAME OVER", 150, 32, Color.RED);
                drawCenteredText(g, world.getEndReason(), 178, 13, Color.WHITE);
                drawCenteredText(g, "You reached night " + world.getCycle().getNight() + (world.getCycle().isEndless() ? " in endless mode" : " of " + Constants.TOTAL_NIGHTS), 198, 12, Color.LIGHT_GRAY);
                drawCenteredText(g, "Press ENTER", 226, 14, Color.LIGHT_GRAY);
                break;
            case NAME:
                dim(g, 185);
                drawCenteredText(g, "NEW HIGH SCORE", 130, 28, Color.YELLOW);
                drawCenteredText(g, "Survived " + pendingScore.getNightsText()
                        + " nights   |   Post " + pendingScore.getPostHealth() + "/" + world.getPost().getMaxHealth(), 158, 13, Color.WHITE);
                drawCenteredText(g, "Your name:", 192, 12, Color.LIGHT_GRAY);
                String shown = nameBuffer + ((frameCount / 30) % 2 == 0 ? "_" : " ");
                drawCenteredText(g, shown, 216, 20, new Color(255, 235, 160));
                drawCenteredText(g, "ENTER - save     ESC - skip", 248, 11, Color.LIGHT_GRAY);
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
        drawDebugNote(g);
    }

    /** A reminder that the developer keys are live. */
    private void drawDebugNote(Graphics2D g) {
        if (Debug.isKeysOn()) {
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.setColor(new Color(255, 120, 120));
            g.drawString("DEBUG KEYS ON  (F12 or ` to turn off)", 6, Constants.INTERNAL_HEIGHT - 4);
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
        drawCenteredText(g, "H - How to play", 218, 11, new Color(200, 205, 220));
        drawCenteredText(g, "May natitira pa tayo.", 242, 11, new Color(255, 235, 160));
        java.util.List<ScoreEntry> top = scores.getTop();
        if (!top.isEmpty()) {   // the high-score table: best runs first
            drawCenteredText(g, "TOP TANODS", 268, 11, new Color(255, 215, 120));
            for (int i = 0; i < top.size(); i++) {
                ScoreEntry e = top.get(i);
                String line = (i + 1) + ".  " + e.getName() + "   " + e.getNightsText() + " nights   Post " + e.getPostHealth();
                drawCenteredText(g, line, 282 + i * 13, 10, new Color(215, 220, 235));
            }
        }
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
