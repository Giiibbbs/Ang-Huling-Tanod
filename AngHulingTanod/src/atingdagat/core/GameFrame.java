package atingdagat.core;

import java.awt.Dimension;
import javax.swing.JFrame;

/**
 * GameFrame.java
 *
 * The game window. It only holds the GamePanel, where everything is drawn.
 * The window can be resized: GamePanel scales the game to fit.
 */
public class GameFrame extends JFrame {

    private final GamePanel panel;

    public GameFrame() {
        super(Constants.TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panel = new GamePanel();
        panel.setPreferredSize(new Dimension(Constants.WINDOW_START_WIDTH, Constants.WINDOW_START_HEIGHT));
        add(panel);

        setResizable(true);
        pack();
        // The window can't be made smaller than the internal screen.
        setMinimumSize(new Dimension(Constants.INTERNAL_WIDTH, Constants.INTERNAL_HEIGHT));
        setLocationRelativeTo(null); // center on the screen
    }

    /** Starts the game loop. Call this after the window is visible. */
    public void startGame() {
        panel.start();
    }
}
