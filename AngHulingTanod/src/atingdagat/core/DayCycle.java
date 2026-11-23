package atingdagat.core;

/**
 * DayCycle.java
 *
 * The clock of the game. It knows which night we are on and which Phase we
 * are in (DAY, DUSK, NIGHT, DAWN), and how dark the sky is.
 *
 * DAY, DUSK and DAWN end by themselves after a set time. NIGHT has no timer:
 * it ends when the world says so (all the ships of that night are gone).
 */
public class DayCycle {

    private int night = 1;
    private Phase phase = Phase.DAY;
    private int timer = 0;           // frames spent in this phase
    private boolean finished = false; // true when night 7 has ended
    private boolean endless = false;  // true after the player chose to keep fighting past night 7

    /** Call once per frame. Returns the new Phase if it just changed, otherwise null. */
    public Phase update() {
        if (phase == Phase.NIGHT || finished) {
            return null;
        }
        timer++;
        if (timer >= length(phase)) {
            return advance();
        }
        return null;
    }

    /** Moves to the next phase right now (the timer or the N key calls this). */
    public Phase advance() {
        timer = 0;
        switch (phase) {
            case DAY:
                phase = Phase.DUSK;
                break;
            case DUSK:
                phase = Phase.NIGHT;
                break;
            case NIGHT:
                phase = Phase.DAWN;
                if (night >= Constants.TOTAL_NIGHTS && !endless) {
                    finished = true;   // the last night is over: the player has won
                }
                break;
            default: // DAWN
                phase = Phase.DAY;
                night++;
                break;
        }
        return phase;
    }

    private int length(Phase p) {
        switch (p) {
            case DAY:
                return Constants.DAY_FRAMES;
            case DUSK:
                return Constants.DUSK_FRAMES;
            case DAWN:
                return Constants.DAWN_FRAMES;
            default:
                return Integer.MAX_VALUE;
        }
    }

    /** 0 = bright day, 1 = full night. Dusk fades in, dawn fades out. */
    public double getDarkness() {
        switch (phase) {
            case DUSK:
                return progress();
            case NIGHT:
                return 1.0;
            case DAWN:
                return 1.0 - progress();
            default:
                return 0.0;
        }
    }

    /** How far through the current phase we are, 0 to 1. Night has no timer, so it is always 1. */
    public double progress() {
        if (phase == Phase.NIGHT) {
            return 1.0;
        }
        return Math.min(1.0, timer / (double) length(phase));
    }

    /** Test key: jump to the start of a given night. */
    public void setNight(int newNight) {
        night = newNight;
        phase = Phase.DAY;
        timer = 0;
        finished = false;
    }

    /** Whole seconds left in this phase (0 at night, which has no timer). */
    public int getSecondsLeft() {
        if (phase == Phase.NIGHT || finished) {
            return 0;
        }
        return (int) Math.ceil((length(phase) - timer) / (double) Constants.TARGET_FPS);
    }

    public Phase getPhase() {
        return phase;
    }

    public int getNight() {
        return night;
    }

    public boolean isNight() {
        return phase == Phase.NIGHT;
    }

    /** After the win: keep going. The next day is night 8's day, and the game never "finishes" again. */
    public void continueEndless() {
        endless = true;
        finished = false;
        phase = Phase.DAY;
        night = Constants.ENDLESS_FIRST_NIGHT;
        timer = 0;
    }

    public boolean isEndless() {
        return endless;
    }

    public boolean isFinished() {
        return finished;
    }
}
