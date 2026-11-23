package atingdagat.core;

/**
 * GameState.java
 *
 * The screens the game can be on. The game is always in exactly ONE of these.
 * GamePanel looks at the current state to decide what to update and what to draw.
 */
public enum GameState {
    MENU,       // title screen
    PLAY,       // the game itself (day and night happen inside this state)
    HELP,       // the how-to-play page (the game is frozen behind it)
    PAUSE,      // game frozen, with a "paused" overlay
    NAME,       // typing a name for the high-score table
    GAME_OVER,  // the player lost
    VICTORY     // the player survived night 7 and the relief fleet arrived
}
