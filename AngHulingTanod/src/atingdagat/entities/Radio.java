package atingdagat.entities;

/**
 * Radio.java
 *
 * The signal that tells the world "May natitira pa tayo". It starts strong.
 * Every ship that lands weakens it. If it goes silent, the island loses.
 */
public class Radio {

    public static final int MAX_SIGNAL = 100;
    private int signal = MAX_SIGNAL;

    public void weaken(int amount) {
        signal = Math.max(0, signal - amount);
    }

    public void restore(int amount) {
        signal = Math.min(MAX_SIGNAL, signal + amount);
    }

    public int getSignal() {
        return signal;
    }

    public boolean isSilent() {
        return signal <= 0;
    }
}
