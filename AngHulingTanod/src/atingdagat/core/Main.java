package atingdagat.core;

import javax.swing.SwingUtilities;

/**
 * Main.java
 *
 * The starting point of the program. In NetBeans, set this class as the
 * "Main Class" in Project Properties > Run, so the runnable JAR knows where to start.
 */
public class Main {

    public static void main(String[] args) {
        // Swing windows must be created on the Event Dispatch Thread.
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
            frame.startGame();
        });
    }
}
